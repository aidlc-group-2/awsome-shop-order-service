package com.awsome.shop.order.domain.model.exchange;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录 领域实体（兑换订单，FR-O1~O10）
 *
 * <p>状态机见 {@link ExchangeStatus}；状态流转方法定义在实体上（充血模型）。</p>
 */
@Data
public class ExchangeRecordEntity {

    private Long id;
    private String orderNo;
    private String requestId;
    private Long userId;
    private Long productId;
    private String productName;
    private String productDesc;
    private Integer quantity;
    private String productType;
    private String reservationId;
    private String employeeName;
    private Integer pointsCost;
    private LocalDateTime exchangeTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isVirtual() {
        return ProductType.VIRTUAL.name().equals(productType);
    }

    /** 是否可取消：仅实物且未发货（FR-O7、US-14：虚拟商品已即时履约不可取消） */
    public boolean cancellable() {
        return !isVirtual() && ExchangeStatus.PENDING_SHIPMENT.name().equals(status);
    }

    /** 是否可发货：实物且待发货（FR-O6） */
    public boolean shippable() {
        return !isVirtual() && ExchangeStatus.PENDING_SHIPMENT.name().equals(status);
    }

    /** 是否可推进为已完成：已发货的实物（US-26） */
    public boolean completable() {
        return ExchangeStatus.SHIPPED.name().equals(status);
    }

    public void markShipped() {
        this.status = ExchangeStatus.SHIPPED.name();
    }

    public void markCompleted() {
        this.status = ExchangeStatus.COMPLETED.name();
    }

    public void markCancelled() {
        this.status = ExchangeStatus.CANCELLED.name();
    }
}
