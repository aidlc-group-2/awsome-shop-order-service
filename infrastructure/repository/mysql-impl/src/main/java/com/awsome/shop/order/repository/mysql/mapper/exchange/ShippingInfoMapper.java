package com.awsome.shop.order.repository.mysql.mapper.exchange;

import com.awsome.shop.order.repository.mysql.po.exchange.ShippingInfoPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配送信息 Mapper 接口
 */
@Mapper
public interface ShippingInfoMapper extends BaseMapper<ShippingInfoPO> {
}
