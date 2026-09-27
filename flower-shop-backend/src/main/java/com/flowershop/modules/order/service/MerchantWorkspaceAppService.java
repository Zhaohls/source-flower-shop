package com.flowershop.modules.order.service;

import com.flowershop.common.context.UserContextHolder;
import com.flowershop.modules.order.dto.OrderModels.TrendPointVO;
import com.flowershop.modules.order.dto.OrderModels.WorkspaceVO;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.enums.OrderStatusEnum;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.review.service.ReviewService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantWorkspaceAppService {

    private final OrderInfoService orderInfoService;
    private final ProductService productService;
    private final ReviewService reviewService;

    public WorkspaceVO getWorkspace() {
        Long merchantId = UserContextHolder.getUserId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        List<OrderInfoEntity> merchantOrders = orderInfoService.lambdaQuery().eq(OrderInfoEntity::getMerchantId, merchantId).list();

        long todayOrders = merchantOrders.stream().filter(order -> !order.getCreateTime().isBefore(todayStart) && order.getCreateTime().isBefore(tomorrowStart)).count();
        BigDecimal todayRevenue = merchantOrders.stream()
            .filter(order -> order.getPayTime() != null && !order.getPayTime().isBefore(todayStart) && order.getPayTime().isBefore(tomorrowStart))
            .filter(order -> !Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode()))
            .map(OrderInfoEntity::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingOrders = merchantOrders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.PAID.getCode())).count();
        long lowStockProducts = productService.lambdaQuery().eq(ProductEntity::getMerchantId, merchantId).lt(ProductEntity::getStock, 10).count();
        List<Long> merchantProductIds = productService.lambdaQuery().eq(ProductEntity::getMerchantId, merchantId).list().stream().map(ProductEntity::getId).toList();
        long pendingReviews = merchantProductIds.isEmpty() ? 0 : reviewService.lambdaQuery().in(com.flowershop.modules.review.entity.ReviewEntity::getProductId, merchantProductIds).isNull(com.flowershop.modules.review.entity.ReviewEntity::getReplyContent).count();

        List<TrendPointVO> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime nextDay = date.plusDays(1).atStartOfDay();
            long orders = merchantOrders.stream().filter(order -> !order.getCreateTime().isBefore(dayStart) && order.getCreateTime().isBefore(nextDay)).count();
            BigDecimal revenue = merchantOrders.stream()
                .filter(order -> order.getPayTime() != null && !order.getPayTime().isBefore(dayStart) && order.getPayTime().isBefore(nextDay))
                .filter(order -> !Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode()))
                .map(OrderInfoEntity::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            weeklyTrend.add(new TrendPointVO(date.toString(), orders, revenue));
        }
        return new WorkspaceVO(todayOrders, todayRevenue, pendingOrders, lowStockProducts, pendingReviews, weeklyTrend);
    }
}
