package com.awsome.shop.order.facade.http.controller;

import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordDTO;
import com.awsome.shop.order.application.api.dto.exchange.ExchangeRecordStatsDTO;
import com.awsome.shop.order.application.api.dto.exchange.request.CancelExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CompleteExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.CreateExchangeRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.GetExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ListExchangeRecordRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.MyExchangeListRequest;
import com.awsome.shop.order.application.api.dto.exchange.request.ShipExchangeRequest;
import com.awsome.shop.order.application.api.service.exchange.ExchangeRecordApplicationService;
import com.awsome.shop.order.common.dto.PageResult;
import com.awsome.shop.order.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分兑换记录 Controller（FR-O1~O10）
 *
 * <p>路径对齐网关契约（gateway-service-api.md §3/§4.2）：
 * 员工侧走受保护路由 {@code /api/v1/order/**}，管理端走 {@code /api/v1/order/admin/**}
 * （命中网关 admin-paths 模式 {@code /api/v1/**}{@code /admin/**}，要求 ADMIN 角色）。</p>
 *
 * <p>用户身份由网关 JWT 校验后通过 {@code X-User-Id} 请求头注入（services.md §4）。</p>
 */
@Tag(name = "ExchangeRecord", description = "积分兑换订单")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExchangeRecordController {

    private final ExchangeRecordApplicationService exchangeRecordApplicationService;

    // ==================== 员工侧（受保护，经网关 JWT 校验） ====================

    @Operation(summary = "创建兑换订单（实物需配送信息，虚拟即时履约）")
    @PostMapping("/order/exchange/create")
    public Result<ExchangeRecordDTO> create(@RequestBody @Valid CreateExchangeRequest request,
                                            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(exchangeRecordApplicationService.createExchange(request, userId));
    }

    @Operation(summary = "我的订单详情")
    @PostMapping("/order/exchange/get")
    public Result<ExchangeRecordDTO> getMine(@RequestBody @Valid GetExchangeRecordRequest request,
                                             @RequestHeader("X-User-Id") Long userId) {
        return Result.success(exchangeRecordApplicationService.getMyExchange(request, userId));
    }

    @Operation(summary = "我的兑换历史")
    @PostMapping("/order/exchange/list")
    public Result<PageResult<ExchangeRecordDTO>> listMine(@RequestBody @Valid MyExchangeListRequest request,
                                                          @RequestHeader("X-User-Id") Long userId) {
        return Result.success(exchangeRecordApplicationService.listMyExchanges(request, userId));
    }

    @Operation(summary = "取消兑换订单（发货前，退积分+释放预占）")
    @PostMapping("/order/exchange/cancel")
    public Result<ExchangeRecordDTO> cancel(@RequestBody @Valid CancelExchangeRequest request,
                                            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(exchangeRecordApplicationService.cancelExchange(request, userId));
    }

    // ==================== 管理员侧（网关校验 ADMIN 角色） ====================

    @Operation(summary = "查询兑换记录详情")
    @PostMapping("/order/admin/exchange/get")
    public Result<ExchangeRecordDTO> get(@RequestBody @Valid GetExchangeRecordRequest request) {
        return Result.success(exchangeRecordApplicationService.get(request));
    }

    @Operation(summary = "分页查询兑换记录")
    @PostMapping("/order/admin/exchange/list")
    public Result<PageResult<ExchangeRecordDTO>> list(@RequestBody @Valid ListExchangeRecordRequest request) {
        return Result.success(exchangeRecordApplicationService.list(request));
    }

    @Operation(summary = "兑换记录统计")
    @PostMapping("/order/admin/exchange/stats")
    public Result<ExchangeRecordStatsDTO> stats() {
        return Result.success(exchangeRecordApplicationService.stats());
    }

    @Operation(summary = "发货（实物订单，预占转正式扣减）")
    @PostMapping("/order/admin/exchange/ship")
    public Result<ExchangeRecordDTO> ship(@RequestBody @Valid ShipExchangeRequest request) {
        return Result.success(exchangeRecordApplicationService.ship(request));
    }

    @Operation(summary = "完成订单（已发货 → 已完成）")
    @PostMapping("/order/admin/exchange/complete")
    public Result<ExchangeRecordDTO> complete(@RequestBody @Valid CompleteExchangeRequest request) {
        return Result.success(exchangeRecordApplicationService.complete(request));
    }
}
