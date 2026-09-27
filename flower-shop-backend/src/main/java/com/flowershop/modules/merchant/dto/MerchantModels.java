package com.flowershop.modules.merchant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class MerchantModels {

    private MerchantModels() {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }

    public record InventoryItemVO(
        Long id,
        String productName,
        String productNo,
        Integer stock,
        Integer stockWarning
    ) {
    }

    public record InventoryAdjustRequest(
        @NotNull(message = "商品不能为空") Long productId,
        @NotBlank(message = "调整类型不能为空") String type,
        @NotNull(message = "调整数量不能为空") @Min(value = 1, message = "调整数量至少为1") Integer quantity,
        String remark
    ) {
    }

    public record DeliveryItemVO(
        Long id,
        String orderNo,
        String receiverName,
        String receiverAddress,
        String deliveryMan,
        Integer deliveryStatus,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime shippedTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveredTime
    ) {
    }

    public record ShopInfoVO(String shopName, String shopDesc, String shopLogo, String contactPhone) {
    }

    public record ShopInfoUpdateRequest(
        @NotBlank(message = "店铺名称不能为空") String shopName,
        String shopDesc,
        String shopLogo,
        @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确") String contactPhone
    ) {
    }

    public record SalesTrendPointVO(String date, BigDecimal revenue, Long orders) {
    }

    public record CategoryBreakdownVO(String name, Long value) {
    }

    public record TopProductVO(String productName, Long sales, BigDecimal revenue) {
    }

    public record SalesStatisticsVO(
        BigDecimal totalRevenue,
        Long totalOrders,
        BigDecimal avgOrderAmount,
        List<SalesTrendPointVO> dailyTrend,
        List<CategoryBreakdownVO> categoryBreakdown,
        List<TopProductVO> topProducts
    ) {
    }
}
