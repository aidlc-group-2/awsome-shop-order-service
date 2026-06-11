package com.awsome.shop.order.application.impl.service.exchange;

import com.awsome.shop.order.application.api.client.PointsClient;
import com.awsome.shop.order.application.api.client.ProductClient;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;
import com.awsome.shop.order.domain.service.exchange.ExchangeRecordDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 兑换 Saga 编排者（FR-O10，services.md §2.2 RedemptionSagaOrchestrator）
 *
 * <p>编排：1) 积分扣减 → 2) 库存预占 → 3) 创建订单；任一步失败按逆序补偿
 * （释放预占 → 退回积分）。各步以订单号 orderRef 为幂等标识，补偿可安全重试。</p>
 *
 * <p>虚拟商品（AS-2/US-10）：订单创建即 COMPLETED，并立即将预占转正式扣减。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedemptionSagaOrchestrator {

    private final PointsClient pointsClient;
    private final ProductClient productClient;
    private final ExchangeRecordDomainService exchangeRecordDomainService;

    public ExchangeRecordEntity execute(ExchangeRecordEntity entity, ShippingInfoEntity shippingInfo) {
        String orderRef = entity.getOrderNo();

        // 步骤1：积分扣减（失败直接抛出，无需补偿）
        pointsClient.deduct(entity.getUserId(), entity.getPointsCost(), orderRef);

        // 步骤2：库存预占（失败 → 补偿步骤1）
        String reservationId;
        try {
            reservationId = productClient.reserveStock(entity.getProductId(), entity.getQuantity(), orderRef);
        } catch (RuntimeException e) {
            compensatePoints(entity, orderRef);
            throw e;
        }
        entity.setReservationId(reservationId);

        // 步骤3：创建订单（失败 → 补偿步骤2、步骤1）
        ExchangeRecordEntity created;
        try {
            created = exchangeRecordDomainService.create(entity, shippingInfo);
        } catch (RuntimeException e) {
            compensateStock(reservationId);
            compensatePoints(entity, orderRef);
            throw e;
        }

        // 虚拟商品即时履约：预占转正式扣减；订单已成立，失败仅告警等待补偿（最终一致，NFR-5）
        if (created.isVirtual()) {
            try {
                productClient.confirmDeduct(reservationId);
            } catch (RuntimeException e) {
                log.error("[FR-O10] 虚拟商品即时履约的库存扣减失败 orderNo={} reservationId={}，待重试/补偿",
                        orderRef, reservationId, e);
            }
        }
        return created;
    }

    private void compensatePoints(ExchangeRecordEntity entity, String orderRef) {
        try {
            pointsClient.refund(entity.getUserId(), entity.getPointsCost(), orderRef);
        } catch (RuntimeException ce) {
            // 补偿失败仅记录：refund 以 orderRef 幂等，可由人工/重试补偿（services.md §3）
            log.error("[FR-O10] Saga 补偿-积分退回失败 orderNo={} userId={} amount={}，需人工介入",
                    orderRef, entity.getUserId(), entity.getPointsCost(), ce);
        }
    }

    private void compensateStock(String reservationId) {
        try {
            productClient.releaseStock(reservationId);
        } catch (RuntimeException ce) {
            log.error("[FR-O10] Saga 补偿-库存释放失败 reservationId={}，需人工介入", reservationId, ce);
        }
    }
}
