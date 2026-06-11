package com.awsome.shop.order.domain.model.exchange;

/**
 * 兑换订单状态机（components.md §4）
 *
 * <p>实物：PENDING_SHIPMENT → SHIPPED → COMPLETED，发货前可 CANCELLED；
 * 虚拟：创建即 COMPLETED（即时履约，不可取消）。</p>
 *
 * <p>设计中的 CREATED/SUCCESS 为同步流程中的瞬时态（下单自动成功，FR-O4），不落库。</p>
 */
public enum ExchangeStatus {

    /** 待发货（实物初始态） */
    PENDING_SHIPMENT,

    /** 已发货 */
    SHIPPED,

    /** 已完成（实物签收 / 虚拟即时履约） */
    COMPLETED,

    /** 已取消（仅发货前） */
    CANCELLED;

    public static ExchangeStatus of(String value) {
        return ExchangeStatus.valueOf(value);
    }
}
