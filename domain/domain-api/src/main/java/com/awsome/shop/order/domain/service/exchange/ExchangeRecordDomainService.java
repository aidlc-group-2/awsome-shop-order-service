package com.awsome.shop.order.domain.service.exchange;

import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordStatsEntity;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;

import java.time.LocalDateTime;

/**
 * 积分兑换记录 领域服务接口
 */
public interface ExchangeRecordDomainService {

    ExchangeRecordEntity getById(Long id);

    /** 按下单幂等键查询，未找到返回 null（不抛异常）。 */
    ExchangeRecordEntity getByRequestId(String requestId);

    PageResult<ExchangeRecordEntity> page(int page, int size, String keyword, String status,
                                          LocalDateTime startTime, LocalDateTime endTime);

    PageResult<ExchangeRecordEntity> pageByUserId(Long userId, int page, int size);

    ExchangeRecordStatsEntity stats();

    /**
     * 创建兑换订单（FR-O1/O4/O5）：实物初始 PENDING_SHIPMENT，虚拟即时 COMPLETED；
     * 实物商品同时落配送信息。
     */
    ExchangeRecordEntity create(ExchangeRecordEntity entity, ShippingInfoEntity shippingInfo);

    /** 发货前取消（FR-O7），状态校验失败抛业务异常 */
    ExchangeRecordEntity cancel(Long id);

    /** 发货（FR-O6），仅待发货实物 */
    ExchangeRecordEntity ship(Long id);

    /** 完成（US-26），仅已发货 */
    ExchangeRecordEntity complete(Long id);

    ShippingInfoEntity getShippingInfo(Long exchangeRecordId);
}
