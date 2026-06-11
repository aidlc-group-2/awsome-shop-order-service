package com.awsome.shop.order.application.api.dto.exchange.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建兑换订单请求（FR-O1/O5）
 *
 * <p>商品快照字段（名称/描述/积分价）由调用方传入，与 exchange_record 去范式化表结构一致；
 * 实物商品（PHYSICAL）必须携带配送信息，由应用层校验。</p>
 */
@Data
public class CreateExchangeRequest {

    /** 下单幂等键（客户端生成，重复提交去重）。为空时退化为无幂等保护。 */
    private String requestId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    private String productDesc;

    @NotNull(message = "兑换数量不能为空")
    @Min(value = 1, message = "兑换数量最小为 1")
    private Integer quantity;

    @NotBlank(message = "商品类型不能为空")
    @Pattern(regexp = "PHYSICAL|VIRTUAL", message = "商品类型必须为 PHYSICAL 或 VIRTUAL")
    private String productType;

    @NotNull(message = "消耗积分不能为空")
    @Min(value = 1, message = "消耗积分必须大于 0")
    private Integer pointsCost;

    @NotBlank(message = "兑换员工姓名不能为空")
    private String employeeName;

    /** 收件人（实物必填） */
    private String recipient;

    /** 配送地址（实物必填） */
    private String address;

    /** 联系方式（实物必填） */
    private String phone;
}
