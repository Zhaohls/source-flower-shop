package com.flowershop.modules.admin.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.admin.dto.AdminModels.DashboardVO;
import com.flowershop.modules.admin.service.AdminConsoleAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员仪表盘")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminConsoleAppService adminConsoleAppService;

    @Operation(summary = "系统仪表盘", description = "返回全局统计数据和趋势图所需数据")
    @GetMapping
    public Result<DashboardVO> dashboard() {
        return Result.success(adminConsoleAppService.dashboard());
    }
}
