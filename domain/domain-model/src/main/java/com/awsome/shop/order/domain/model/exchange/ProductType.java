package com.awsome.shop.order.domain.model.exchange;

/**
 * 商品类型（components.md §2：Product 含 productType）
 */
public enum ProductType {

    /** 实物商品（需配送信息，走发货流程） */
    PHYSICAL,

    /** 虚拟商品（即时履约，无配送） */
    VIRTUAL
}
