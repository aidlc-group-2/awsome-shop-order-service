package com.awsome.shop.order.application.impl.client;

import com.awsome.shop.order.application.api.client.PointsClient;
import com.awsome.shop.order.common.enums.OrderErrorCode;
import com.awsome.shop.order.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * 积分服务客户端实现（FR-O2/FR-O7）
 *
 * <p>契约：points-service-api.md §3.2/§3.3 —
 * {@code POST /api/v1/private/point/deduct|refund}，请求体 {userId, amount, orderRef}。
 * orderRef 为幂等标识，余额不足返回 POINTS_001（HTTP 422）。</p>
 *
 * <p>超时按失败处理并触发 Saga 补偿（services.md §3）。</p>
 */
@Slf4j
@Component
public class PointsClientImpl implements PointsClient {

    private final String baseUrl;
    private final WebClient webClient;
    private final Duration timeout;

    public PointsClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${shop.points.base-url:http://localhost:8003}") String baseUrl,
            @Value("${shop.points.timeout:5s}") Duration timeout) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.build();
        this.timeout = timeout;
    }

    @Override
    public void deduct(Long userId, long amount, String orderRef) {
        try {
            webClient.post()
                    .uri(baseUrl + "/api/v1/private/point/deduct")
                    .bodyValue(Map.of("userId", userId, "amount", amount, "orderRef", orderRef))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(timeout)
                    .block();
            log.info("[FR-O2] 积分扣减成功 userId={} amount={} orderRef={}", userId, amount, orderRef);
        } catch (WebClientResponseException e) {
            log.error("[FR-O2] 积分扣减被拒绝 userId={} amount={} orderRef={} status={} body={}",
                    userId, amount, orderRef, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(OrderErrorCode.POINTS_DEDUCT_FAILED,
                    extractMessage(e, "积分扣减失败"));
        } catch (Exception e) {
            log.error("[FR-O2] 积分扣减调用异常 userId={} amount={} orderRef={}", userId, amount, orderRef, e);
            throw new BusinessException(OrderErrorCode.POINTS_DEDUCT_FAILED, e);
        }
    }

    @Override
    public void refund(Long userId, long amount, String orderRef) {
        webClient.post()
                .uri(baseUrl + "/api/v1/private/point/refund")
                .bodyValue(Map.of("userId", userId, "amount", amount, "orderRef", orderRef))
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .block();
        log.info("[FR-O7] 积分退回成功 userId={} amount={} orderRef={}", userId, amount, orderRef);
    }

    private String extractMessage(WebClientResponseException e, String fallback) {
        String body = e.getResponseBodyAsString();
        return body == null || body.isBlank() ? fallback : fallback + ": " + body;
    }
}
