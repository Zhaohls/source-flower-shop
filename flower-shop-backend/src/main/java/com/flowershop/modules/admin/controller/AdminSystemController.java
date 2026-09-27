package com.flowershop.modules.admin.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.model.PageQuery;
import com.flowershop.common.result.Result;
import com.flowershop.modules.admin.dto.AdminModels.DictDataVO;
import com.flowershop.modules.admin.dto.AdminModels.DictTypeVO;
import com.flowershop.modules.admin.dto.AdminModels.OperationLogVO;
import com.flowershop.modules.admin.dto.AdminModels.PageResult;
import com.flowershop.modules.admin.service.AdminSystemAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "管理员系统管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemAppService adminSystemAppService;

    @Operation(summary = "操作日志列表")
    @GetMapping("/logs")
    public Result<PageResult<OperationLogVO>> logs(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return Result.success(adminSystemAppService.logs(new PageQuery(pageNum, pageSize)));
    }

    @Operation(summary = "清空操作日志")
    @DeleteMapping("/logs")
    public Result<Void> clearLogs() {
        adminSystemAppService.clearLogs();
        return Result.success();
    }

    @Operation(summary = "字典类型列表")
    @GetMapping("/dict-types")
    public Result<java.util.List<DictTypeVO>> dictTypes() {
        return Result.success(adminSystemAppService.dictTypes());
    }

    @Operation(summary = "字典数据列表")
    @GetMapping("/dict-types/{dictTypeId}/data")
    public Result<java.util.List<DictDataVO>> dictData(@PathVariable Long dictTypeId) {
        return Result.success(adminSystemAppService.dictData(dictTypeId));
    }
}
