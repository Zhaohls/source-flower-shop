package com.flowershop.modules.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class ReviewModels {

    private ReviewModels() {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }

    public record ReviewSubmitRequest(
        @NotNull(message = "订单不能为空") Long orderId,
        @NotNull(message = "订单明细不能为空") Long orderItemId,
        @NotNull(message = "评分不能为空") @Min(value = 1, message = "评分最小为1") @Max(value = 5, message = "评分最大为5") Integer rating,
        String content,
        List<String> images
    ) {
    }

    public record ReviewReplyRequest(@NotBlank(message = "回复内容不能为空") String replyContent) {
    }

    public record ProductReviewVO(
        Long id,
        String username,
        String avatar,
        Integer rating,
        String content,
        List<String> images,
        String replyContent,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime replyTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record MerchantReviewVO(
        Long id,
        Long orderItemId,
        Long productId,
        String productName,
        String username,
        Integer rating,
        String content,
        List<String> images,
        String replyContent,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }
}
