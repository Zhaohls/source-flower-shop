package com.flowershop.modules.category.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.category.dto.CategoryModels.CategorySaveRequest;
import com.flowershop.modules.category.dto.CategoryModels.CategoryVO;
import com.flowershop.modules.category.service.CategoryAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员分类管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryAppService categoryAppService;

    @Operation(summary = "获取分类列表", description = "返回后台分类列表")
    @GetMapping({"", "/tree"})
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryAppService.list());
    }

    @Operation(summary = "新增分类", description = "管理员新增一级商品分类")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody CategorySaveRequest request) {
        return Result.success(Map.of("id", categoryAppService.create(request)));
    }

    @Operation(summary = "修改分类", description = "管理员修改商品分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        categoryAppService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除分类", description = "若分类下存在商品则禁止删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryAppService.delete(id);
        return Result.success();
    }
}
