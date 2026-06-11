package com.awsome.shop.order.application.api.client;

/**
 * 商品服务出站客户端（FR-O3/FR-O6，design: component-methods.md §4 ProductClient）
 *
 * <p>库存预占/释放/正式扣减，悲观锁防超兑由商品服务侧保证（NFR-4）。</p>
 */
public interface ProductClient {

    /**
     * 获取商品权威快照（下单核价，FR-O1）。
     *
     * <p>用于校验客户端传入的积分价与商品上下架状态，杜绝伪造低价兑换。
     * 商品不存在 / 下架 / 调用失败时抛业务异常。</p>
     *
     * @param productId 商品ID
     * @return 权威快照（积分单价、上下架状态、库存）
     */
    ProductSnapshot getSnapshot(Long productId);

    /**
     * 商品权威快照。
     */
    class ProductSnapshot {
        public Long id;
        public String name;
        public Integer pointsPrice;
        public Integer status;
        public Integer stock;
    }

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
