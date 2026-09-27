package com.flowershop.modules.banner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class BannerModels {

    private BannerModels() {
    }

    public record BannerSaveRequest(
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "图片地址不能为空") String imageUrl,
        String linkUrl,
        @NotNull(message = "排序不能为空") Integer sort,
        @NotNull(message = "状态不能为空") Integer status
    ) {
    }

    public record BannerVO(Long id, String title, String imageUrl, String linkUrl, Integer sort, Integer status, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime) {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }
}
