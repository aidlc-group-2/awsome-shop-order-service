package com.awsome.shop.order.application.api.dto.exchange.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 取消兑换订单请求（FR-O7，发货前）
 */
@Data
public class CancelExchangeRequest {

    @NotNull(message = "订单ID不能为空")
    private Long id;
}
