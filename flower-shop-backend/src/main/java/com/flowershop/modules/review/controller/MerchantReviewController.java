package com.flowershop.modules.review.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.review.dto.ReviewModels.MerchantReviewVO;
import com.flowershop.modules.review.dto.ReviewModels.PageResult;
import com.flowershop.modules.review.dto.ReviewModels.ReviewReplyRequest;
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

@Tag(name = "商家评价回复")
@RequireRole({"MERCHANT"})
@RestController
@RequestMapping("/api/merchant/reviews")
@RequiredArgsConstructor
public class MerchantReviewController {

    private final ReviewAppService reviewAppService;

    @Operation(summary = "评价列表", description = "查看当前商家的评价列表，可按是否已回复筛选")
    @GetMapping
    public Result<PageResult<MerchantReviewVO>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Boolean replied
    ) {
        return Result.success(reviewAppService.merchantReviews(new com.flowershop.common.model.PageQuery(pageNum, pageSize), replied));
    }

    @Operation(summary = "回复评价", description = "商家回复指定评价")
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody ReviewReplyRequest request) {
        reviewAppService.reply(id, request);
        return Result.success();
    }
}
