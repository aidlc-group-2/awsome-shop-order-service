package com.awsome.shop.order.application.api.service.exchange;

import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordDTO;
import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordStatsDTO;
import com.awsome.shop.order.application.api.dto.exchange.request.CancelExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CompleteExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CreateExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.GetExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ListExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.MyExchangeListRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ShipExchangeRequest;
import com.awsome.shop.order.common.dto.PageResult;

/**
 * 积分兑换记录 应用服务接口
 */
public interface ExchangeRecordApplicationService {

    // ==================== 员工侧 ====================

    /** 创建兑换订单（FR-O1~O5：Saga 编排扣积分→预占库存→建单，虚拟即时履约） */
    ExchangeRecordDTO createExchange(CreateExchangeRequest request, Long userId);

    /** 我的订单详情（FR-O8，校验归属） */
    ExchangeRecordDTO getMyExchange(GetExchangeRecordRequest request, Long userId);

    /** 我的兑换历史（FR-O8） */
    PageResult<ExchangeRecordDTO> listMyExchanges(MyExchangeListRequest request, Long userId);

    /** 发货前取消（FR-O7：退积分 + 释放预占） */
    ExchangeRecordDTO cancelExchange(CancelExchangeRequest request, Long userId);

    // ==================== 管理员侧 ====================

    ExchangeRecordDTO get(GetExchangeRecordRequest request);

    PageResult<ExchangeRecordDTO> list(ListExchangeRecordRequest request);

    ExchangeRecordStatsDTO stats();

    /** 发货（FR-O6：库存预占转正式扣减，PENDING_SHIPMENT → SHIPPED） */
    ExchangeRecordDTO ship(ShipExchangeRequest request);

    /** 完成（US-26：SHIPPED → COMPLETED） */
    ExchangeRecordDTO complete(CompleteExchangeRequest request);
}
