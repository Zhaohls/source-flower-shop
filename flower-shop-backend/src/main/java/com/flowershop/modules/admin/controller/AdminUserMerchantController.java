package com.flowershop.modules.admin.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.admin.dto.AdminModels.MerchantAuditRequest;
import com.flowershop.modules.admin.dto.AdminModels.MerchantItemVO;
import com.flowershop.modules.admin.dto.AdminModels.PageResult;
import com.flowershop.modules.admin.dto.AdminModels.StatusRequest;
import com.flowershop.modules.admin.dto.AdminModels.UserItemVO;
import com.flowershop.modules.admin.service.AdminConsoleAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员用户与商家管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserMerchantController {

    private final AdminConsoleAppService adminConsoleAppService;

    @Operation(summary = "商家列表", description = "分页查看商家并支持按审核状态筛选")
    @GetMapping("/merchants")
    public Result<PageResult<MerchantItemVO>> merchants(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer auditStatus
    ) {
        return Result.success(adminConsoleAppService.merchants(new com.flowershop.common.model.PageQuery(pageNum, pageSize), auditStatus));
    }

    @Operation(summary = "审核商家", description = "审核通过或拒绝商家入驻申请，并记录审核备注")
    @PostMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id, @Valid @RequestBody MerchantAuditRequest request) {
        adminConsoleAppService.auditMerchant(id, request);
        return Result.success();
    }

    @Operation(summary = "启用禁用商家", description = "修改商家账号状态")
    @PatchMapping("/merchants/{id}/status")
    public Result<Void> updateMerchantStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        adminConsoleAppService.updateMerchantStatus(id, request.status());
        return Result.success();
    }

    @Operation(summary = "用户列表", description = "分页查看买家账号")
    @GetMapping("/users")
    public Result<PageResult<UserItemVO>> users(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status
    ) {
        return Result.success(adminConsoleAppService.users(new com.flowershop.common.model.PageQuery(pageNum, pageSize), keyword, status));
    }

    @Operation(summary = "启用禁用用户", description = "修改买家账号状态")
    @PatchMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        adminConsoleAppService.updateUserStatus(id, request.status());
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "逻辑删除买家账号")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminConsoleAppService.deleteUser(id);
        return Result.success();
    }
}
