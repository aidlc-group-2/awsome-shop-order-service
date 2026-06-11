package com.awsome.shop.order.application.api.client;

/**
 * 积分服务出站客户端（FR-O2/FR-O7，contract: points-service-api.md §3.2/§3.3）
 *
 * <p>以 orderRef（订单号）作为幂等标识，支持失败重试（services.md §3）。</p>
 */
public interface PointsClient {

    /**
     * 兑换下单扣减积分。余额不足或调用失败时抛出业务异常。
     *
     * @param userId   用户ID
     * @param amount   扣减金额（>0）
     * @param orderRef 关联订单号（幂等标识）
     */
    void deduct(Long userId, long amount, String orderRef);

    /**
     * 兑换取消/Saga 补偿时退回积分。
     *
     * @param userId   用户ID
     * @param amount   退回金额（>0）
     * @param orderRef 关联订单号
     */
    void refund(Long userId, long amount, String orderRef);
}
