package com.flowershop.modules.product.controller;

import com.flowershop.common.result.Result;
import com.flowershop.modules.product.dto.ProductModels.CustomerHomeVO;
import com.flowershop.modules.product.dto.ProductModels.CustomerProductItem;
import com.flowershop.modules.product.dto.ProductModels.ProductDetailVO;
import com.flowershop.modules.product.dto.ProductModels.ProductPageResult;
import com.flowershop.modules.product.service.ProductAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "买家商品浏览")
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProductController {

    private final ProductAppService productAppService;

    @Operation(summary = "首页聚合数据", description = "返回轮播图、分类、热销商品、新品和公告")
    @GetMapping("/home")
    public Result<CustomerHomeVO> home() {
        return Result.success(productAppService.getCustomerHome());
    }

    @Operation(summary = "买家商品列表", description = "支持关键词、分类、价格区间和排序筛选")
    @GetMapping("/products")
    public Result<ProductPageResult<CustomerProductItem>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return Result.success(productAppService.getCustomerProducts(new com.flowershop.common.model.PageQuery(pageNum, pageSize), categoryId, keyword, sortBy, sortOrder, minPrice, maxPrice));
    }

    @Operation(summary = "商品详情", description = "返回商品详情、商家信息和评价摘要")
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productAppService.getCustomerProductDetail(id));
    }
}
