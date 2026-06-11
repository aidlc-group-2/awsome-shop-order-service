package com.awsome.shop.order.application.impl.client;

import com.awsome.shop.order.application.api.client.ProductClient;
import com.awsome.shop.order.common.enums.OrderErrorCode;
import com.awsome.shop.order.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 商品服务客户端实现（FR-O3/FR-O6）
 *
 * <p><b>WORKAROUND（临时方案）</b>：商品服务（Unit3）侧的 private 库存接口尚未实现
 * （见 product-service-api.md「兑换服务侧的商品查询/库存扣减等内部接口尚未实现」），
 * 默认通过特性开关 {@code shop.product.stock.enabled=false} 关闭真实调用，
 * 预占返回本地生成的占位凭据，发货/释放仅记录日志。</p>
 *
 * <p>启用后调用契约（按 {scope}/{module}/{action} 约定与 component-methods.md §7）：
 * {@code POST /api/v1/private/stock/reserve|release|confirm}。
 * 搜索 {@code TODO(FR-O3)} 可定位相关占位。</p>
 */
@Slf4j
@Component
public class ProductClientImpl implements ProductClient {

    private static final String STUB_PREFIX = "STUB-";

    private final boolean stockEnabled;
    private final String baseUrl;
    private final WebClient webClient;
    private final Duration timeout;

    public ProductClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${shop.product.stock.enabled:false}") boolean stockEnabled,
            @Value("${shop.product.base-url:http://localhost:8002}") String baseUrl,
            @Value("${shop.product.timeout:5s}") Duration timeout) {
        this.stockEnabled = stockEnabled;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.build();
        this.timeout = timeout;
    }

    @Override
    public String reserveStock(Long productId, int quantity, String orderRef) {
        // TODO(FR-O3): 商品服务 private 库存接口就绪后，将 shop.product.stock.enabled 置为 true 并完成联调。
        if (!stockEnabled) {
            String stubReservationId = STUB_PREFIX + UUID.randomUUID();
            log.info("[FR-O3][WORKAROUND] 库存预占已禁用(shop.product.stock.enabled=false)，"
                    + "productId={} qty={} orderRef={} 返回占位凭据 {}", productId, quantity, orderRef, stubReservationId);
            return stubReservationId;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = webClient.post()
                    .uri(baseUrl + "/api/v1/private/stock/reserve")
                    .bodyValue(Map.of("productId", productId, "quantity", quantity, "orderRef", orderRef))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();
            String reservationId = result == null ? null
                    : String.valueOf(((Map<String, Object>) result.getOrDefault("data", Map.of())).get("reservationId"));
            if (reservationId == null || "null".equals(reservationId)) {
                throw new BusinessException(OrderErrorCode.STOCK_RESERVE_FAILED, "预占凭据为空");
            }
            log.info("[FR-O3] 库存预占成功 productId={} qty={} orderRef={} reservationId={}",
                    productId, quantity, orderRef, reservationId);
            return reservationId;
        } catch (BusinessException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("[FR-O3] 库存预占被拒绝 productId={} qty={} orderRef={} status={} body={}",
                    productId, quantity, orderRef, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(OrderErrorCode.STOCK_RESERVE_FAILED, e);
        } catch (Exception e) {
            log.error("[FR-O3] 库存预占调用异常 productId={} qty={} orderRef={}", productId, quantity, orderRef, e);
            throw new BusinessException(OrderErrorCode.STOCK_RESERVE_FAILED, e);
        }
    }

    @Override
    public void releaseStock(String reservationId) {
        if (!stockEnabled || reservationId.startsWith(STUB_PREFIX)) {
            log.info("[FR-O3][WORKAROUND] 跳过库存预占释放 reservationId={}", reservationId);
            return;
        }
        webClient.post()
                .uri(baseUrl + "/api/v1/private/stock/release")
                .bodyValue(Map.of("reservationId", reservationId))
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .block();
        log.info("[FR-O3] 库存预占释放成功 reservationId={}", reservationId);
    }

    @Override
    public void confirmDeduct(String reservationId) {
        if (!stockEnabled || reservationId.startsWith(STUB_PREFIX)) {
            log.info("[FR-O6][WORKAROUND] 跳过库存正式扣减 reservationId={}", reservationId);
            return;
        }
        webClient.post()
                .uri(baseUrl + "/api/v1/private/stock/confirm")
                .bodyValue(Map.of("reservationId", reservationId))
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .block();
        log.info("[FR-O6] 库存正式扣减成功 reservationId={}", reservationId);
    }
}
