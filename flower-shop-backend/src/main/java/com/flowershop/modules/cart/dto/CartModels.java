package com.flowershop.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class CartModels {

    private CartModels() {
    }

    public record AddCartItemRequest(@NotNull(message = "商品不能为空") Long productId, @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量最少为1") Integer quantity) {
    }

    public record UpdateCartItemRequest(@NotNull(message = "数量不能为空") @Min(value = 1, message = "数量最少为1") Integer quantity) {
    }

    public record SelectAllRequest(@NotNull(message = "选中状态不能为空") Boolean selected) {
    }

    public record UpdateCartSelectionRequest(@NotNull(message = "选中状态不能为空") Boolean selected) {
    }

    public record CartItemVO(
        Long id,
        Long productId,
        String productName,
        Long merchantId,
        String merchantName,
        String mainImage,
        BigDecimal price,
        Integer quantity,
        Integer selected,
        Integer stock,
        Boolean invalid,
        String invalidReason,
        Boolean supportCustomize
    ) {
    }

    public record CartListVO(List<CartItemVO> items, BigDecimal totalAmount, Integer totalCount) {
    }
}
