package com.awsome.shop.order.repository.exchange;

import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordStatsEntity;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;

import java.time.LocalDateTime;

/**
 * 积分兑换记录 仓储接口
 */
public interface ExchangeRecordRepository {

    ExchangeRecordEntity getById(Long id);

    ExchangeRecordEntity getByOrderNo(String orderNo);

    /** 保存订单，回填 id */
    Long save(ExchangeRecordEntity entity);

    /** 更新状态（按实体当前 status 持久化） */
    void updateStatus(ExchangeRecordEntity entity);

    PageResult<ExchangeRecordEntity> page(int page, int size, String keyword, String status,
                                          LocalDateTime startTime, LocalDateTime endTime);

    /** 员工个人兑换历史（FR-O8） */
    PageResult<ExchangeRecordEntity> pageByUserId(Long userId, int page, int size);

    ExchangeRecordStatsEntity stats();

    void saveShippingInfo(ShippingInfoEntity shippingInfo);

    ShippingInfoEntity getShippingInfoByRecordId(Long exchangeRecordId);
}
