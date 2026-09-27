package com.flowershop.modules.admin.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.order.dto.OrderModels.OrderDetailVO;
import com.flowershop.modules.order.dto.OrderModels.OrderListItemVO;
import com.flowershop.modules.order.dto.OrderModels.PageResult;
import com.flowershop.modules.order.service.OrderAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员订单管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderAppService orderAppService;

    @Operation(summary = "全局订单列表", description = "支持按状态和商家筛选平台订单")
    @GetMapping
    public Result<PageResult<OrderListItemVO>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) Long merchantId
    ) {
        return Result.success(orderAppService.adminOrders(new com.flowershop.common.model.PageQuery(pageNum, pageSize), status, merchantId));
    }

    @Operation(summary = "订单详情", description = "查看平台任意订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.success(orderAppService.adminOrderDetail(orderNo));
    }
}
