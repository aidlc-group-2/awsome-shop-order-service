package com.awsome.shop.order.application.api.dto.exchange.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 完成订单请求（US-26，管理员，已发货 → 已完成）
 */
@Data
public class CompleteExchangeRequest {

    @NotNull(message = "订单ID不能为空")
    private Long id;
}
