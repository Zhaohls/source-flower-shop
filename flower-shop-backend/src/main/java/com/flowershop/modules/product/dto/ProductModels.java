package com.flowershop.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductModels {

    private ProductModels() {
    }

    public record ProductSaveRequest(
        @NotNull(message = "分类不能为空") Long categoryId,
        @NotBlank(message = "商品名称不能为空") @Size(max = 200, message = "商品名称不能超过200位") String productName,
        String productSn,
        @NotBlank(message = "商品主图不能为空") @Size(max = 500, message = "商品主图地址不能超过500位") String mainImage,
        @Size(max = 5, message = "商品附图最多5张") List<String> images,
        @NotNull(message = "售价不能为空") @DecimalMin(value = "0.01", message = "售价必须大于0") BigDecimal price,
        @DecimalMin(value = "0.01", message = "原价必须大于0") BigDecimal originalPrice,
        @NotNull(message = "库存不能为空") @Min(value = 0, message = "库存不能小于0") Integer stock,
        @Min(value = 0, message = "库存预警值不能小于0") Integer stockWarning,
        String flowerLanguage,
        String materialDesc,
        String detailContent,
        Boolean supportCustomize
    ) {
    }

    public record ProductStatusRequest(
        @NotNull(message = "状态不能为空") @Min(value = 0, message = "状态值不合法") @Max(value = 1, message = "状态值不合法") Integer status
    ) {
    }

    public record ProductPageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }

    public record MerchantProductItem(
        Long id,
        String productNo,
        String productName,
        String mainImage,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        Integer sales,
        Integer status,
        String categoryName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record MerchantProductDetailVO(
        Long id,
        Long categoryId,
        String productNo,
        String productName,
        String mainImage,
        List<String> images,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        String flowerLanguage,
        String materialDesc,
        String detailContent,
        Boolean supportCustomize,
        Integer status
    ) {
    }

    public record AdminProductItem(
        Long id,
        String productNo,
        String productName,
        String mainImage,
        BigDecimal price,
        Integer stock,
        Integer sales,
        Integer status,
        String categoryName,
        String merchantName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record CustomerProductItem(
        Long id,
        String productName,
        String mainImage,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer sales,
        String flowerLanguage,
        Boolean supportCustomize,
        String merchantName
    ) {
    }

    public record CustomerHomeVO(
        List<BannerSimpleVO> banners,
        List<CategorySimpleVO> categories,
        List<CustomerProductItem> hotProducts,
        List<CustomerProductItem> newProducts,
        List<NoticeSimpleVO> notices
    ) {
    }

    public record BannerSimpleVO(Long id, String imageUrl, String linkUrl, String title) {
    }

    public record CategorySimpleVO(Long id, String categoryName, String categoryIcon) {
    }

    public record NoticeSimpleVO(Long id, String title, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime) {
    }

    public record MerchantSimpleVO(Long id, String shopName, String shopLogo) {
    }

    public record ReviewSummaryVO(BigDecimal avgRating, Long totalCount, String goodRate) {
    }

    public record ProductDetailVO(
        Long id,
        String productName,
        String productSn,
        String mainImage,
        List<String> images,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        Integer sales,
        String flowerLanguage,
        String materialDesc,
        String detailContent,
        Boolean supportCustomize,
        String categoryName,
        MerchantSimpleVO merchant,
        ReviewSummaryVO reviewSummary
    ) {
    }
}
