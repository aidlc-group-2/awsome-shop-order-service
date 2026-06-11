package com.awsome.shop.order.domain.model.exchange;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配送信息 领域实体（FR-O5，实物商品）
 */
@Data
public class ShippingInfoEntity {

    private Long id;
    private Long exchangeRecordId;
    private String recipient;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
