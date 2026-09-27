package com.flowershop.modules.product.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.product.dto.ProductModels.MerchantProductDetailVO;
import com.flowershop.modules.product.dto.ProductModels.MerchantProductItem;
import com.flowershop.modules.product.dto.ProductModels.ProductPageResult;
import com.flowershop.modules.product.dto.ProductModels.ProductSaveRequest;
import com.flowershop.modules.product.dto.ProductModels.ProductStatusRequest;
import com.flowershop.modules.product.service.ProductAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家商品管理")
@RequireRole({"MERCHANT"})
@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductAppService productAppService;

    @Operation(summary = "商家商品列表", description = "仅返回当前登录商家的商品")
    @GetMapping
    public Result<ProductPageResult<MerchantProductItem>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status
    ) {
        return Result.success(productAppService.getMerchantProducts(new com.flowershop.common.model.PageQuery(pageNum, pageSize), keyword, status));
    }

    @Operation(summary = "商家商品详情", description = "编辑商品时回填表单")
    @GetMapping("/{id}")
    public Result<MerchantProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productAppService.getMerchantProductDetail(id));
    }

    @Operation(summary = "新增商品", description = "新增商品时自动生成商品编号")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody ProductSaveRequest request) {
        return Result.success(Map.of("id", productAppService.createMerchantProduct(request)));
    }

    @Operation(summary = "修改商品", description = "商家只能修改自己的商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        productAppService.updateMerchantProduct(id, request);
        return Result.success();
    }

    @Operation(summary = "删除商品", description = "执行逻辑删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productAppService.deleteMerchantProduct(id);
        return Result.success();
    }

    @Operation(summary = "上架下架商品", description = "切换当前商品的展示状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusRequest request) {
        productAppService.updateMerchantStatus(id, request.status());
        return Result.success();
    }
}
