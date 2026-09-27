package com.flowershop.modules.product.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.product.dto.ProductModels.AdminProductItem;
import com.flowershop.modules.product.dto.ProductModels.ProductPageResult;
import com.flowershop.modules.product.dto.ProductModels.ProductSaveRequest;
import com.flowershop.modules.product.dto.ProductModels.ProductStatusRequest;
import com.flowershop.modules.product.service.ProductAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员商品管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductAppService productAppService;

    @Operation(summary = "全局商品列表", description = "支持按照商家、状态和关键词筛选")
    @GetMapping
    public Result<ProductPageResult<AdminProductItem>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long merchantId,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String keyword
    ) {
        return Result.success(productAppService.getAdminProducts(new com.flowershop.common.model.PageQuery(pageNum, pageSize), merchantId, status, keyword));
    }

    @Operation(summary = "修改商品", description = "管理员可修改全平台商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        productAppService.updateAdminProduct(id, request);
        return Result.success();
    }

    @Operation(summary = "删除商品", description = "管理员可删除任意商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productAppService.deleteAdminProduct(id);
        return Result.success();
    }

    @Operation(summary = "强制上下架", description = "管理员可强制修改商品状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusRequest request) {
        productAppService.updateAdminStatus(id, request.status());
        return Result.success();
    }
}
