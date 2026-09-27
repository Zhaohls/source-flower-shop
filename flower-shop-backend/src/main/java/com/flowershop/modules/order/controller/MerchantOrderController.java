package com.flowershop.modules.order.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.order.dto.OrderModels.DeliveryStatusRequest;
import com.flowershop.modules.order.dto.OrderModels.MerchantRejectRequest;
import com.flowershop.modules.order.dto.OrderModels.MerchantShipRequest;
import com.flowershop.modules.order.dto.OrderModels.OrderDetailVO;
import com.flowershop.modules.order.dto.OrderModels.OrderListItemVO;
import com.flowershop.modules.order.dto.OrderModels.PageResult;
import com.flowershop.modules.order.service.OrderAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家订单管理")
@RequireRole({"MERCHANT"})
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderAppService orderAppService;

    @Operation(summary = "商家订单列表", description = "分页查看当前商家的订单")
    @GetMapping("/orders")
    public Result<PageResult<OrderListItemVO>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status
    ) {
        return Result.success(orderAppService.merchantOrders(new com.flowershop.common.model.PageQuery(pageNum, pageSize), status));
    }

    @Operation(summary = "商家订单详情", description = "查看当前商家的订单详情与配送信息")
    @GetMapping("/orders/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.success(orderAppService.merchantOrderDetail(orderNo));
    }

    @Operation(summary = "接单", description = "将已支付待接单的订单流转为备货中")
    @PostMapping("/orders/{orderNo}/accept")
    public Result<Void> accept(@PathVariable String orderNo) {
        orderAppService.acceptOrder(orderNo);
        return Result.success();
    }

    @Operation(summary = "拒单", description = "拒单后订单取消并回滚库存")
    @PostMapping("/orders/{orderNo}/reject")
    public Result<Void> reject(@PathVariable String orderNo, @Valid @RequestBody MerchantRejectRequest request) {
        orderAppService.rejectOrder(orderNo, request);
        return Result.success();
    }

    @Operation(summary = "发货", description = "填写配送员信息并将订单流转为配送中")
    @PostMapping("/orders/{orderNo}/ship")
    public Result<Void> ship(@PathVariable String orderNo, @Valid @RequestBody MerchantShipRequest request) {
        orderAppService.shipOrder(orderNo, request);
        return Result.success();
    }

    @Operation(summary = "更新配送状态", description = "更新配送状态并记录备注")
    @PatchMapping("/delivery/{deliveryId}/status")
    public Result<Void> updateDeliveryStatus(@PathVariable Long deliveryId, @Valid @RequestBody DeliveryStatusRequest request) {
        orderAppService.updateDeliveryStatus(deliveryId, request);
        return Result.success();
    }
}
