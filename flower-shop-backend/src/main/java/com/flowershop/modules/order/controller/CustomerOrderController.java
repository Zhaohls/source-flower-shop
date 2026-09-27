package com.flowershop.modules.order.controller;

import com.flowershop.common.annotation.RateLimit;
import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.constant.LimitTarget;
import com.flowershop.common.result.Result;
import com.flowershop.modules.order.dto.OrderModels.CancelRequest;
import com.flowershop.modules.order.dto.OrderModels.CardTemplateVO;
import com.flowershop.modules.order.dto.OrderModels.ConfirmOrderRequest;
import com.flowershop.modules.order.dto.OrderModels.ConfirmOrderVO;
import com.flowershop.modules.order.dto.OrderModels.OrderDetailVO;
import com.flowershop.modules.order.dto.OrderModels.OrderListItemVO;
import com.flowershop.modules.order.dto.OrderModels.OrderSubmitRequest;
import com.flowershop.modules.order.dto.OrderModels.PageResult;
import com.flowershop.modules.order.dto.OrderModels.PayRequest;
import com.flowershop.modules.order.dto.OrderModels.SubmitOrderResponse;
import com.flowershop.modules.order.service.OrderAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "买家订单中心")
@RequireRole({"CUSTOMER"})
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderAppService orderAppService;

    @Operation(summary = "确认订单", description = "基于购物车项返回订单预览信息")
    @PostMapping("/orders/confirm")
    public Result<ConfirmOrderVO> confirm(@Valid @RequestBody ConfirmOrderRequest request) {
        return Result.success(orderAppService.confirmOrder(request));
    }

    @Operation(summary = "提交订单", description = "创建订单并锁定库存，返回待支付信息")
    @RateLimit(keyPrefix = "submit-order", maxCount = 10, windowSeconds = 60, target = LimitTarget.USER, message = "下单过于频繁，请稍后再试")
    @PostMapping("/orders/submit")
    public Result<SubmitOrderResponse> submit(@Valid @RequestBody OrderSubmitRequest request) {
        return Result.success(orderAppService.submitOrder(request));
    }

    @Operation(summary = "模拟支付", description = "用于毕设演示的模拟支付接口")
    @PostMapping("/orders/{orderNo}/pay")
    public Result<Void> pay(@PathVariable String orderNo, @Valid @RequestBody PayRequest request) {
        orderAppService.pay(orderNo, request);
        return Result.success();
    }

    @Operation(summary = "我的订单列表", description = "按状态分页查询当前用户的订单")
    @GetMapping("/orders")
    public Result<PageResult<OrderListItemVO>> myOrders(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status
    ) {
        return Result.success(orderAppService.myOrders(new com.flowershop.common.model.PageQuery(pageNum, pageSize), status));
    }

    @Operation(summary = "订单详情", description = "查看当前用户的订单详情")
    @GetMapping("/orders/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.success(orderAppService.myOrderDetail(orderNo));
    }

    @Operation(summary = "取消订单", description = "取消订单并回滚库存")
    @PostMapping("/orders/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo, @Valid @RequestBody CancelRequest request) {
        orderAppService.cancelMyOrder(orderNo, request.reason());
        return Result.success();
    }

    @Operation(summary = "确认收货", description = "买家确认收货后订单变为已完成")
    @PostMapping("/orders/{orderNo}/confirm-receive")
    public Result<Void> confirmReceive(@PathVariable String orderNo) {
        orderAppService.confirmReceive(orderNo);
        return Result.success();
    }

    @Operation(summary = "贺卡模板列表", description = "按场景筛选可用贺卡模板")
    @GetMapping("/card-templates")
    public Result<List<CardTemplateVO>> cardTemplates(@RequestParam(required = false) String sceneType) {
        return Result.success(orderAppService.listCardTemplates(sceneType));
    }
}
