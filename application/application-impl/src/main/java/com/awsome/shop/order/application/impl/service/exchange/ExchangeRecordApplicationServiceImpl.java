package com.awsome.shop.order.application.impl.service.exchange;

import com.awsome.shop.order.application.api.client.PointsClient;
import com.awsome.shop.order.application.api.client.ProductClient;
import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordDTO;
import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordStatsDTO;
import com.awsome.shop.order.application.api.dto.exchange.ShippingInfoDTO;
import com.awsome.shop.order.application.api.dto.exchange.request.CancelExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CompleteExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CreateExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.GetExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ListExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.MyExchangeListRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ShipExchangeRequest;
import com.awsome.shop.order.application.api.service.exchange.ExchangeRecordApplicationService;
import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.common.enums.OrderErrorCode;
import com.awsome.shop.order.common.exception.BusinessException;
import com.awsome.shop.order.common.exception.ParameterException;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordStatsEntity;
import com.awsome.shop.order.domain.model.exchange.ProductType;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;
import com.awsome.shop.order.domain.service.exchange.ExchangeRecordDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 积分兑换记录 应用服务实现
 *
 * <p>只依赖 Domain Service 与出站 Client，不直接依赖 Repository。
 * 创建兑换委托 {@link RedemptionSagaOrchestrator} 执行（component-methods.md §4）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRecordApplicationServiceImpl implements ExchangeRecordApplicationService {

    private static final DateTimeFormatter ORDER_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ExchangeRecordDomainService exchangeRecordDomainService;
    private final RedemptionSagaOrchestrator redemptionSagaOrchestrator;
    private final PointsClient pointsClient;
    private final ProductClient productClient;

    // ==================== 员工侧 ====================

    @Override
    public ExchangeRecordDTO createExchange(CreateExchangeRequest request, Long userId) {
        boolean physical = ProductType.PHYSICAL.name().equals(request.getProductType());
        // FR-O5：实物商品必须携带完整配送信息
        if (physical && (isBlank(request.getRecipient()) || isBlank(request.getAddress()) || isBlank(request.getPhone()))) {
            throw new ParameterException(OrderErrorCode.SHIPPING_INFO_REQUIRED);
        }

        ExchangeRecordEntity entity = new ExchangeRecordEntity();
        entity.setOrderNo(generateOrderNo());
        entity.setUserId(userId);
        entity.setProductId(request.getProductId());
        entity.setProductName(request.getProductName());
        entity.setProductDesc(request.getProductDesc());
        entity.setQuantity(request.getQuantity());
        entity.setProductType(request.getProductType());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setPointsCost(request.getPointsCost() * request.getQuantity());

        ShippingInfoEntity shippingInfo = null;
        if (physical) {
            shippingInfo = new ShippingInfoEntity();
            shippingInfo.setRecipient(request.getRecipient());
            shippingInfo.setAddress(request.getAddress());
            shippingInfo.setPhone(request.getPhone());
        }

        ExchangeRecordEntity created = redemptionSagaOrchestrator.execute(entity, shippingInfo);
        log.info("[FR-O1] 兑换订单创建成功 orderNo={} userId={} productId={} status={}",
                created.getOrderNo(), userId, created.getProductId(), created.getStatus());
        return toDTO(created, shippingInfo);
    }

    @Override
    public ExchangeRecordDTO getMyExchange(GetExchangeRecordRequest request, Long userId) {
        ExchangeRecordEntity entity = exchangeRecordDomainService.getById(request.getId());
        requireOwner(entity, userId);
        return withShipping(entity);
    }

    @Override
    public PageResult<ExchangeRecordDTO> listMyExchanges(MyExchangeListRequest request, Long userId) {
        return exchangeRecordDomainService.pageByUserId(userId, request.getPage(), request.getSize())
                .convert(e -> toDTO(e, null));
    }

    @Override
    public ExchangeRecordDTO cancelExchange(CancelExchangeRequest request, Long userId) {
        ExchangeRecordEntity entity = exchangeRecordDomainService.getById(request.getId());
        requireOwner(entity, userId);

        // 先落库取消（域内状态校验），再执行外部补偿；补偿以 orderRef 幂等、失败可重试
        ExchangeRecordEntity cancelled = exchangeRecordDomainService.cancel(request.getId());
        try {
            pointsClient.refund(cancelled.getUserId(), cancelled.getPointsCost(), cancelled.getOrderNo());
        } catch (RuntimeException e) {
            log.error("[FR-O7] 取消订单的积分退回失败 orderNo={}，待重试/补偿", cancelled.getOrderNo(), e);
        }
        if (cancelled.getReservationId() != null) {
            try {
                productClient.releaseStock(cancelled.getReservationId());
            } catch (RuntimeException e) {
                log.error("[FR-O7] 取消订单的库存释放失败 orderNo={} reservationId={}，待重试/补偿",
                        cancelled.getOrderNo(), cancelled.getReservationId(), e);
            }
        }
        log.info("[FR-O7] 兑换订单已取消 orderNo={} userId={}", cancelled.getOrderNo(), userId);
        return withShipping(cancelled);
    }

    // ==================== 管理员侧 ====================

    @Override
    public ExchangeRecordDTO get(GetExchangeRecordRequest request) {
        return withShipping(exchangeRecordDomainService.getById(request.getId()));
    }

    @Override
    public PageResult<ExchangeRecordDTO> list(ListExchangeRecordRequest request) {
        return exchangeRecordDomainService.page(
                        request.getPage(), request.getSize(),
                        request.getKeyword(), request.getStatus(),
                        request.getStartTime(), request.getEndTime())
                .convert(e -> toDTO(e, null));
    }

    @Override
    public ExchangeRecordStatsDTO stats() {
        ExchangeRecordStatsEntity entity = exchangeRecordDomainService.stats();
        ExchangeRecordStatsDTO dto = new ExchangeRecordStatsDTO();
        dto.setTotalCount(entity.getTotalCount());
        dto.setPendingDeliveryCount(entity.getPendingDeliveryCount());
        dto.setCompletedCount(entity.getCompletedCount());
        dto.setTotalPointsConsumed(entity.getTotalPointsConsumed());
        return dto;
    }

    @Override
    public ExchangeRecordDTO ship(ShipExchangeRequest request) {
        ExchangeRecordEntity shipped = exchangeRecordDomainService.ship(request.getId());
        // FR-O6/US-26：发货时预占转正式扣减；失败仅告警，库存侧以凭据幂等可重试
        if (shipped.getReservationId() != null) {
            try {
                productClient.confirmDeduct(shipped.getReservationId());
            } catch (RuntimeException e) {
                log.error("[FR-O6] 发货库存扣减失败 orderNo={} reservationId={}，待重试/补偿",
                        shipped.getOrderNo(), shipped.getReservationId(), e);
            }
        }
        log.info("[FR-O6] 订单已发货 orderNo={}", shipped.getOrderNo());
        return withShipping(shipped);
    }

    @Override
    public ExchangeRecordDTO complete(CompleteExchangeRequest request) {
        ExchangeRecordEntity completed = exchangeRecordDomainService.complete(request.getId());
        log.info("[US-26] 订单已完成 orderNo={}", completed.getOrderNo());
        return withShipping(completed);
    }

    // ==================== 私有方法 ====================

    private void requireOwner(ExchangeRecordEntity entity, Long userId) {
        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_OWNER);
        }
    }

    private String generateOrderNo() {
        return "ORDER-" + LocalDate.now().format(ORDER_NO_DATE) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private ExchangeRecordDTO withShipping(ExchangeRecordEntity entity) {
        ShippingInfoEntity shippingInfo = entity.isVirtual()
                ? null : exchangeRecordDomainService.getShippingInfo(entity.getId());
        return toDTO(entity, shippingInfo);
    }

    private ExchangeRecordDTO toDTO(ExchangeRecordEntity entity, ShippingInfoEntity shippingInfo) {
        ExchangeRecordDTO dto = new ExchangeRecordDTO();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        dto.setUserId(entity.getUserId());
        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        dto.setProductDesc(entity.getProductDesc());
        dto.setQuantity(entity.getQuantity());
        dto.setProductType(entity.getProductType());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setPointsCost(entity.getPointsCost());
        dto.setExchangeTime(entity.getExchangeTime());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (shippingInfo != null) {
            ShippingInfoDTO shippingDTO = new ShippingInfoDTO();
            shippingDTO.setRecipient(shippingInfo.getRecipient());
            shippingDTO.setAddress(shippingInfo.getAddress());
            shippingDTO.setPhone(shippingInfo.getPhone());
            dto.setShippingInfo(shippingDTO);
        }
        return dto;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
