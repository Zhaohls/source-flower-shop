package com.flowershop.modules.review.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.review.dto.ReviewModels.PageResult;
import com.flowershop.modules.review.dto.ReviewModels.ProductReviewVO;
import com.flowershop.modules.review.dto.ReviewModels.ReviewSubmitRequest;
import com.flowershop.modules.review.service.ReviewAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "买家评价模块")
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerReviewController {

    private final ReviewAppService reviewAppService;

    @Operation(summary = "提交评价", description = "对已完成订单的商品提交评价")
    @RequireRole({"CUSTOMER"})
    @PostMapping("/reviews")
    public Result<Void> submit(@Valid @RequestBody ReviewSubmitRequest request) {
        reviewAppService.submit(request);
        return Result.success();
    }

    @Operation(summary = "商品评价列表", description = "分页查看商品评价内容和商家回复")
    @GetMapping("/products/{productId}/reviews")
    public Result<PageResult<ProductReviewVO>> reviews(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return Result.success(reviewAppService.productReviews(productId, new com.flowershop.common.model.PageQuery(pageNum, pageSize)));
    }
}
