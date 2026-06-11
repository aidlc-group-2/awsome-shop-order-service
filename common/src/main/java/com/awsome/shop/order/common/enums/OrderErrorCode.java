package com.awsome.shop.order.common.enums;

/**
 * 兑换订单业务错误码（FR-O1~O10）
 *
 * <p>错误码前缀决定 HTTP 状态码映射，参见 {@link ErrorCode} 接口说明。</p>
 */
public enum OrderErrorCode implements ErrorCode {

    /** 兑换订单不存在 */
    ORDER_NOT_FOUND("NOT_FOUND_002", "兑换订单不存在"),

    /** 无权操作他人订单 */
    ORDER_NOT_OWNER("AUTHZ_002", "无权操作该兑换订单"),

    /** 发货后/虚拟商品不可取消（FR-O7） */
    CANCEL_NOT_ALLOWED("ORDER_001", "订单当前状态不可取消"),

    /** 仅待发货的实物订单可发货（FR-O6） */
    SHIP_NOT_ALLOWED("ORDER_002", "订单当前状态不可发货"),

    /** 仅已发货订单可推进为已完成 */
    COMPLETE_NOT_ALLOWED("ORDER_003", "订单当前状态不可完成"),

    /** 实物商品缺少配送信息（FR-O5） */
    SHIPPING_INFO_REQUIRED("PARAM_010", "实物商品必须填写完整配送信息（收件人/地址/联系方式）"),

    /** Saga 步骤1失败：积分扣减（FR-O2） */
    POINTS_DEDUCT_FAILED("ORDER_004", "积分扣减失败"),

    /** Saga 步骤2失败：库存预占（FR-O3） */
    STOCK_RESERVE_FAILED("ORDER_005", "库存预占失败"),

    /** 并发更新冲突（乐观锁，状态已被其他请求变更） */
    ORDER_CONCURRENT_CONFLICT("CONFLICT_002", "订单状态已被并发修改，请重试"),

    /** 下游商品信息校验失败（价格/状态/类型不一致或商品不可兑换） */
    PRODUCT_VALIDATION_FAILED("ORDER_006", "商品信息校验失败: {0}");

    private final String code;
    private final String message;

    OrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
