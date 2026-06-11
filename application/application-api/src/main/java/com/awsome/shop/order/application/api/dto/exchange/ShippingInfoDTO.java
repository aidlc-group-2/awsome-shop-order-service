package com.awsome.shop.order.application.api.dto.exchange;

import lombok.Data;

/**
 * 配送信息 数据传输对象（FR-O5）
 */
@Data
public class ShippingInfoDTO {

    private String recipient;
    private String address;
    private String phone;
}
