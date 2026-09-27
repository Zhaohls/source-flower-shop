package com.flowershop.modules.order.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.order.dto.OrderModels.WorkspaceVO;
import com.flowershop.modules.order.service.MerchantWorkspaceAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家工作台")
@RequireRole({"MERCHANT"})
@RestController
@RequestMapping("/api/merchant/workspace")
@RequiredArgsConstructor
public class MerchantWorkspaceController {

    private final MerchantWorkspaceAppService merchantWorkspaceAppService;

    @Operation(summary = "工作台数据聚合", description = "返回今日订单、营收、待处理事项和近7日趋势")
    @GetMapping
    public Result<WorkspaceVO> workspace() {
        return Result.success(merchantWorkspaceAppService.getWorkspace());
    }
}
