package com.awsome.shop.order.repository.mysql.impl.exchange;

import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordEntity;
import com.awsome.shop.order.domain.model.exchange.ExchangeRecordStatsEntity;
import com.awsome.shop.order.domain.model.exchange.ShippingInfoEntity;
import com.awsome.shop.order.repository.exchange.ExchangeRecordRepository;
import com.awsome.shop.order.repository.mysql.mapper.exchange.ExchangeRecordMapper;
import com.awsome.shop.order.repository.mysql.mapper.exchange.ShippingInfoMapper;
import com.awsome.shop.order.repository.mysql.po.exchange.ExchangeRecordPO;
import com.awsome.shop.order.repository.mysql.po.exchange.ExchangeRecordStatsPO;
import com.awsome.shop.order.repository.mysql.po.exchange.ShippingInfoPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 积分兑换记录 仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ExchangeRecordRepositoryImpl implements ExchangeRecordRepository {

    private final ExchangeRecordMapper exchangeRecordMapper;
    private final ShippingInfoMapper shippingInfoMapper;

    @Override
    public ExchangeRecordEntity getById(Long id) {
        ExchangeRecordPO po = exchangeRecordMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public ExchangeRecordEntity getByOrderNo(String orderNo) {
        ExchangeRecordPO po = exchangeRecordMapper.selectOne(
                new LambdaQueryWrapper<ExchangeRecordPO>().eq(ExchangeRecordPO::getOrderNo, orderNo));
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(ExchangeRecordEntity entity) {
        ExchangeRecordPO po = toPO(entity);
        exchangeRecordMapper.insert(po);
        entity.setId(po.getId());
        return po.getId();
    }

    @Override
    public void updateStatus(ExchangeRecordEntity entity) {
        ExchangeRecordPO po = exchangeRecordMapper.selectById(entity.getId());
        po.setStatus(entity.getStatus());
        exchangeRecordMapper.updateById(po);
    }

    @Override
    public PageResult<ExchangeRecordEntity> page(int page, int size, String keyword, String status,
                                                  LocalDateTime startTime, LocalDateTime endTime) {
        IPage<ExchangeRecordPO> result = exchangeRecordMapper.selectPage(
                new Page<>(page, size), keyword, status, startTime, endTime);
        return toPageResult(result);
    }

    @Override
    public PageResult<ExchangeRecordEntity> pageByUserId(Long userId, int page, int size) {
        IPage<ExchangeRecordPO> result = exchangeRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ExchangeRecordPO>()
                        .eq(ExchangeRecordPO::getUserId, userId)
                        .orderByDesc(ExchangeRecordPO::getExchangeTime));
        return toPageResult(result);
    }

    @Override
    public ExchangeRecordStatsEntity stats() {
        ExchangeRecordStatsPO po = exchangeRecordMapper.selectStats();
        ExchangeRecordStatsEntity entity = new ExchangeRecordStatsEntity();
        entity.setTotalCount(po.getTotalCount());
        entity.setPendingDeliveryCount(po.getPendingDeliveryCount());
        entity.setCompletedCount(po.getCompletedCount());
        entity.setTotalPointsConsumed(po.getTotalPointsConsumed());
        return entity;
    }

    @Override
    public void saveShippingInfo(ShippingInfoEntity shippingInfo) {
        ShippingInfoPO po = new ShippingInfoPO();
        po.setExchangeRecordId(shippingInfo.getExchangeRecordId());
        po.setRecipient(shippingInfo.getRecipient());
        po.setAddress(shippingInfo.getAddress());
        po.setPhone(shippingInfo.getPhone());
        shippingInfoMapper.insert(po);
        shippingInfo.setId(po.getId());
    }

    @Override
    public ShippingInfoEntity getShippingInfoByRecordId(Long exchangeRecordId) {
        ShippingInfoPO po = shippingInfoMapper.selectOne(
                new LambdaQueryWrapper<ShippingInfoPO>()
                        .eq(ShippingInfoPO::getExchangeRecordId, exchangeRecordId));
        if (po == null) {
            return null;
        }
        ShippingInfoEntity entity = new ShippingInfoEntity();
        entity.setId(po.getId());
        entity.setExchangeRecordId(po.getExchangeRecordId());
        entity.setRecipient(po.getRecipient());
        entity.setAddress(po.getAddress());
        entity.setPhone(po.getPhone());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PageResult<ExchangeRecordEntity> toPageResult(IPage<ExchangeRecordPO> result) {
        PageResult<ExchangeRecordEntity> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    private ExchangeRecordEntity toEntity(ExchangeRecordPO po) {
        ExchangeRecordEntity entity = new ExchangeRecordEntity();
        entity.setId(po.getId());
        entity.setOrderNo(po.getOrderNo());
        entity.setUserId(po.getUserId());
        entity.setProductId(po.getProductId());
        entity.setProductName(po.getProductName());
        entity.setProductDesc(po.getProductDesc());
        entity.setQuantity(po.getQuantity());
        entity.setProductType(po.getProductType());
        entity.setReservationId(po.getReservationId());
        entity.setEmployeeName(po.getEmployeeName());
        entity.setPointsCost(po.getPointsCost());
        entity.setExchangeTime(po.getExchangeTime());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private ExchangeRecordPO toPO(ExchangeRecordEntity entity) {
        ExchangeRecordPO po = new ExchangeRecordPO();
        po.setId(entity.getId());
        po.setOrderNo(entity.getOrderNo());
        po.setUserId(entity.getUserId());
        po.setProductId(entity.getProductId());
        po.setProductName(entity.getProductName());
        po.setProductDesc(entity.getProductDesc());
        po.setQuantity(entity.getQuantity());
        po.setProductType(entity.getProductType());
        po.setReservationId(entity.getReservationId());
        po.setEmployeeName(entity.getEmployeeName());
        po.setPointsCost(entity.getPointsCost());
        po.setExchangeTime(entity.getExchangeTime());
        po.setStatus(entity.getStatus());
        return po;
    }
}
