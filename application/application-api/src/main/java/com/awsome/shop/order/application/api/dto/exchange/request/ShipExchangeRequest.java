package com.awsome.shop.order.application.api.dto.exchange.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发货请求（FR-O6，管理员，实物订单）
 */
@Data
public class ShipExchangeRequest {

    @NotNull(message = "订单ID不能为空")
    private Long id;
}
