package com.flowershop.modules.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CategoryModels {

    private CategoryModels() {
    }

    public record CategorySaveRequest(
        @NotBlank(message = "分类名称不能为空") String categoryName,
        String categoryIcon,
        @NotNull(message = "排序不能为空") Integer sort,
        @NotNull(message = "状态不能为空") Integer status
    ) {
    }

    public record CategoryVO(Long id, String categoryName, String categoryIcon, Integer sort, Integer status) {
    }
}
