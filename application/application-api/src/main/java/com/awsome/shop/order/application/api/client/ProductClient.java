package com.awsome.shop.order.application.api.client;

/**
 * 商品服务出站客户端（FR-O3/FR-O6，design: component-methods.md §4 ProductClient）
 *
 * <p>库存预占/释放/正式扣减，悲观锁防超兑由商品服务侧保证（NFR-4）。</p>
 */
public interface ProductClient {

    /**
     * 兑换下单时预占库存（非立即扣减，FR-O3）。
     *
     * @param productId 商品ID
     * @param quantity  数量
     * @param orderRef  关联订单号（幂等标识）
     * @return 预占凭据 reservationId（补偿释放/发货扣减用）
     */
    String reserveStock(Long productId, int quantity, String orderRef);

    /**
     * 释放预占（取消/Saga 补偿）。
     *
     * @param reservationId 预占凭据
     */
    void releaseStock(String reservationId);

    /**
     * 发货时预占转正式扣减（FR-O6）。
     *
     * @param reservationId 预占凭据
     */
    void confirmDeduct(String reservationId);
}
