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

    /** 按下单幂等键查询（重复提交去重）。requestId 为空时返回 null。 */
    ExchangeRecordEntity getByRequestId(String requestId);

    /** 保存订单，回填 id */
    Long save(ExchangeRecordEntity entity);

    /**
     * 条件更新状态（CAS）：仅当数据库当前状态等于 expectedStatus 时才更新为实体的新状态。
     *
     * @param entity         携带新状态与 id 的实体
     * @param expectedStatus 期望的原状态
     * @return 受影响行数；0 表示状态已被并发修改，调用方须中止后续外部调用
     */
    int updateStatus(ExchangeRecordEntity entity, String expectedStatus);

    PageResult<ExchangeRecordEntity> page(int page, int size, String keyword, String status,
                                          LocalDateTime startTime, LocalDateTime endTime);

    /** 员工个人兑换历史（FR-O8） */
    PageResult<ExchangeRecordEntity> pageByUserId(Long userId, int page, int size);

    ExchangeRecordStatsEntity stats();

    void saveShippingInfo(ShippingInfoEntity shippingInfo);

    ShippingInfoEntity getShippingInfoByRecordId(Long exchangeRecordId);
}
