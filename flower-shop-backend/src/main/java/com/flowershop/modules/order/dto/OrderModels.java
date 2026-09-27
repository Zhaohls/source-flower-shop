package com.flowershop.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flowershop.modules.address.dto.AddressModels.AddressVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderModels {

    private OrderModels() {
    }

    public record ConfirmOrderRequest(@NotEmpty(message = "请选择购物车商品") List<Long> cartItemIds) {
    }

    public record OrderSubmitRequest(
        @NotNull(message = "收货地址不能为空") Long addressId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTime,
        String remark,
        @Valid @NotEmpty(message = "下单商品不能为空") List<OrderSubmitItem> items
    ) {
    }

    public record OrderSubmitItem(
        @NotNull(message = "商品不能为空") Long productId,
        @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量最少为1") Integer quantity,
        @Valid CustomizeRequest customize
    ) {
    }

    public record CustomizeRequest(Long cardTemplateId, String cardContent, String wrappingType, String ribbonColor, String extraNote) {
    }

    public record PayRequest(@NotNull(message = "支付方式不能为空") Integer payType) {
    }

    public record CancelRequest(@NotBlank(message = "取消原因不能为空") String reason) {
    }

    public record MerchantRejectRequest(@NotBlank(message = "拒单原因不能为空") String reason) {
    }

    public record MerchantShipRequest(
        String deliveryCompany,
        @NotBlank(message = "配送员不能为空") String deliveryMan,
        @NotBlank(message = "配送联系电话不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String deliveryPhone
    ) {
    }

    public record DeliveryStatusRequest(@NotNull(message = "配送状态不能为空") Integer deliveryStatus, String remark) {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }

    public record CardTemplateVO(Long id, String templateName, String templateImage, String sceneType) {
    }

    public record ConfirmOrderItemVO(
        Long cartItemId,
        Long productId,
        String productName,
        String mainImage,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal,
        Boolean supportCustomize
    ) {
    }

    public record ConfirmOrderVO(List<ConfirmOrderItemVO> items, List<AddressVO> addresses, List<CardTemplateVO> cardTemplates, BigDecimal totalAmount) {
    }

    public record SubmitOrderResponse(String orderNo, BigDecimal totalAmount, BigDecimal payAmount, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payExpireTime) {
    }

    public record OrderItemVO(Long orderItemId, Long productId, String productName, String productImage, BigDecimal price, Integer quantity, BigDecimal subtotal, Boolean reviewed, CustomizeRequest customize) {
    }

    public record DeliveryVO(Long deliveryId, String deliveryMan, String deliveryPhone, Integer deliveryStatus, String remark, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime shippedTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveredTime) {
    }

    public record OrderListItemVO(
        Long orderId,
        String orderNo,
        Integer orderStatus,
        String orderStatusText,
        BigDecimal payAmount,
        String merchantName,
        String receiverName,
        String receiverPhone,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime,
        List<OrderItemVO> items
    ) {
    }

    public record OrderDetailVO(
        Long orderId,
        String orderNo,
        Integer orderStatus,
        String orderStatusText,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String merchantName,
        Integer payType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime payTime,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime deliveryTime,
        String remark,
        String cancelReason,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime,
        List<OrderItemVO> items,
        DeliveryVO delivery
    ) {
    }

    public record TrendPointVO(String date, Long orders, BigDecimal revenue) {
    }

    public record WorkspaceVO(Long todayOrders, BigDecimal todayRevenue, Long pendingOrders, Long lowStockProducts, Long pendingReviews, List<TrendPointVO> weeklyTrend) {
    }
}
