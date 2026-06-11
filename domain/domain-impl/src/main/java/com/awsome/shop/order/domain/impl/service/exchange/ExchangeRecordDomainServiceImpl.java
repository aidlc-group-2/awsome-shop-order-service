package com.awsome.shop.order.domain.impl.service.exchange;

import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.common.enums.OrderErrorCode;
import com.awsome.shop.order.common.exception.BusinessException;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordStatsEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeStatus;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;
import com.awsome.shop.order.domain.service.exchange.ExchangeRecordDomainService;
import com.awsome.shop.order.repository.exchange.ExchangeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 积分兑换记录 领域服务实现
 */
@Service
@RequiredArgsConstructor
public class ExchangeRecordDomainServiceImpl implements ExchangeRecordDomainService {

    private final ExchangeRecordRepository exchangeRecordRepository;

    @Override
    public ExchangeRecordEntity getById(Long id) {
        ExchangeRecordEntity entity = exchangeRecordRepository.getById(id);
        if (entity == null) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public PageResult<ExchangeRecordEntity> page(int page, int size, String keyword, String status,
                                                  LocalDateTime startTime, LocalDateTime endTime) {
        return exchangeRecordRepository.page(page, size, keyword, status, startTime, endTime);
    }

    @Override
    public PageResult<ExchangeRecordEntity> pageByUserId(Long userId, int page, int size) {
        return exchangeRecordRepository.pageByUserId(userId, page, size);
    }

    @Override
    public ExchangeRecordStatsEntity stats() {
        return exchangeRecordRepository.stats();
    }

    @Override
    @Transactional
    public ExchangeRecordEntity create(ExchangeRecordEntity entity, ShippingInfoEntity shippingInfo) {
        entity.setExchangeTime(LocalDateTime.now());
        // FR-O4 下单自动成功：实物进入待发货，虚拟即时履约完成（components.md §4 状态机）
        entity.setStatus(entity.isVirtual()
                ? ExchangeStatus.COMPLETED.name()
                : ExchangeStatus.PENDING_SHIPMENT.name());
        exchangeRecordRepository.save(entity);
        if (!entity.isVirtual()) {
            shippingInfo.setExchangeRecordId(entity.getId());
            exchangeRecordRepository.saveShippingInfo(shippingInfo);
        }
        return entity;
    }

    @Override
    @Transactional
    public ExchangeRecordEntity cancel(Long id) {
        ExchangeRecordEntity entity = getById(id);
        if (!entity.cancellable()) {
            throw new BusinessException(OrderErrorCode.CANCEL_NOT_ALLOWED);
        }
        entity.markCancelled();
        exchangeRecordRepository.updateStatus(entity);
        return entity;
    }

    @Override
    @Transactional
    public ExchangeRecordEntity ship(Long id) {
        ExchangeRecordEntity entity = getById(id);
        if (!entity.shippable()) {
            throw new BusinessException(OrderErrorCode.SHIP_NOT_ALLOWED);
        }
        entity.markShipped();
        exchangeRecordRepository.updateStatus(entity);
        return entity;
    }

    @Override
    @Transactional
    public ExchangeRecordEntity complete(Long id) {
        ExchangeRecordEntity entity = getById(id);
        if (!entity.completable()) {
            throw new BusinessException(OrderErrorCode.COMPLETE_NOT_ALLOWED);
        }
        entity.markCompleted();
        exchangeRecordRepository.updateStatus(entity);
        return entity;
    }

    @Override
    public ShippingInfoEntity getShippingInfo(Long exchangeRecordId) {
        return exchangeRecordRepository.getShippingInfoByRecordId(exchangeRecordId);
    }
}
