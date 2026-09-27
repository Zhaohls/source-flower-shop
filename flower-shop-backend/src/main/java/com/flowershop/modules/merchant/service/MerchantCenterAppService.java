package com.flowershop.modules.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.category.service.CategoryService;
import com.flowershop.modules.delivery.entity.DeliveryEntity;
import com.flowershop.modules.delivery.service.DeliveryService;
import com.flowershop.modules.merchant.dto.MerchantModels.CategoryBreakdownVO;
import com.flowershop.modules.merchant.dto.MerchantModels.DeliveryItemVO;
import com.flowershop.modules.merchant.dto.MerchantModels.InventoryAdjustRequest;
import com.flowershop.modules.merchant.dto.MerchantModels.InventoryItemVO;
import com.flowershop.modules.merchant.dto.MerchantModels.PageResult;
import com.flowershop.modules.merchant.dto.MerchantModels.SalesStatisticsVO;
import com.flowershop.modules.merchant.dto.MerchantModels.SalesTrendPointVO;
import com.flowershop.modules.merchant.dto.MerchantModels.ShopInfoUpdateRequest;
import com.flowershop.modules.merchant.dto.MerchantModels.ShopInfoVO;
import com.flowershop.modules.merchant.dto.MerchantModels.TopProductVO;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.entity.OrderItemEntity;
import com.flowershop.modules.order.enums.OrderStatusEnum;
import com.flowershop.modules.order.service.OrderInfoService;
import com.flowershop.modules.order.service.OrderItemService;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantCenterAppService {

    private static final int DEFAULT_STOCK_WARNING = 10;

    private final ProductService productService;
    private final OrderInfoService orderInfoService;
    private final OrderItemService orderItemService;
    private final DeliveryService deliveryService;
    private final UserService userService;
    private final CategoryService categoryService;

    public PageResult<InventoryItemVO> inventory(PageQuery pageQuery, Boolean lowStock) {
        Page<ProductEntity> page = productService.page(
            new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<ProductEntity>()
                .eq(ProductEntity::getMerchantId, UserContextHolder.getUserId())
                .lt(Boolean.TRUE.equals(lowStock), ProductEntity::getStock, DEFAULT_STOCK_WARNING)
                .orderByAsc(ProductEntity::getStock)
                .orderByDesc(ProductEntity::getUpdateTime)
        );

        List<InventoryItemVO> records = page.getRecords().stream()
            .map(product -> new InventoryItemVO(
                product.getId(),
                product.getProductName(),
                product.getProductNo(),
                product.getStock(),
                DEFAULT_STOCK_WARNING
            ))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(InventoryAdjustRequest request) {
        ProductEntity productEntity = loadMerchantProduct(request.productId());
        String type = StringUtils.trimToEmpty(request.type()).toLowerCase();
        int nextStock;
        if ("increase".equals(type)) {
            nextStock = productEntity.getStock() + request.quantity();
        } else if ("decrease".equals(type)) {
            nextStock = productEntity.getStock() - request.quantity();
            if (nextStock < 0) {
                throw new BusinessException("库存不足，无法扣减");
            }
        } else {
            throw new BusinessException("不支持的库存调整类型");
        }
        productEntity.setStock(nextStock);
        productService.updateById(productEntity);
    }

    public PageResult<DeliveryItemVO> delivery(PageQuery pageQuery, Integer status) {
        List<OrderInfoEntity> merchantOrders = orderInfoService.lambdaQuery()
            .eq(OrderInfoEntity::getMerchantId, UserContextHolder.getUserId())
            .list();
        if (merchantOrders.isEmpty()) {
            return new PageResult<>(List.of(), 0L, pageQuery.current(), pageQuery.size(), 0L);
        }

        Map<Long, OrderInfoEntity> orderMap = merchantOrders.stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item), HashMap::putAll);
        List<Long> orderIds = merchantOrders.stream().map(OrderInfoEntity::getId).toList();
        Page<DeliveryEntity> page = deliveryService.page(
            new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<DeliveryEntity>()
                .in(DeliveryEntity::getOrderId, orderIds)
                .eq(status != null, DeliveryEntity::getDeliveryStatus, status)
                .orderByDesc(DeliveryEntity::getUpdateTime)
        );

        List<DeliveryItemVO> records = page.getRecords().stream()
            .map(item -> {
                OrderInfoEntity orderInfoEntity = orderMap.get(item.getOrderId());
                if (orderInfoEntity == null) {
                    return null;
                }
                return new DeliveryItemVO(
                    item.getId(),
                    orderInfoEntity.getOrderNo(),
                    orderInfoEntity.getReceiverName(),
                    orderInfoEntity.getReceiverAddress(),
                    item.getDeliveryMan(),
                    item.getDeliveryStatus(),
                    item.getShippedTime(),
                    item.getDeliveredTime()
                );
            })
            .filter(Objects::nonNull)
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public SalesStatisticsVO salesStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDate rangeEnd = endDate == null ? LocalDate.now() : endDate;
        LocalDate rangeStart = startDate == null ? rangeEnd.minusDays(6) : startDate;
        if (rangeStart.isAfter(rangeEnd)) {
            LocalDate temp = rangeStart;
            rangeStart = rangeEnd;
            rangeEnd = temp;
        }

        LocalDateTime startAt = rangeStart.atStartOfDay();
        LocalDateTime endExclusive = rangeEnd.plusDays(1).atStartOfDay();
        List<OrderInfoEntity> paidOrders = orderInfoService.lambdaQuery()
            .eq(OrderInfoEntity::getMerchantId, UserContextHolder.getUserId())
            .isNotNull(OrderInfoEntity::getPayTime)
            .ge(OrderInfoEntity::getPayTime, startAt)
            .lt(OrderInfoEntity::getPayTime, endExclusive)
            .ne(OrderInfoEntity::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
            .list();

        BigDecimal totalRevenue = paidOrders.stream()
            .map(OrderInfoEntity::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = paidOrders.size();
        BigDecimal avgOrderAmount = totalOrders == 0
            ? BigDecimal.ZERO
            : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<SalesTrendPointVO> dailyTrend = new ArrayList<>();
        for (LocalDate cursor = rangeStart; !cursor.isAfter(rangeEnd); cursor = cursor.plusDays(1)) {
            LocalDate currentDate = cursor;
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime nextDay = currentDate.plusDays(1).atStartOfDay();
            List<OrderInfoEntity> dayOrders = paidOrders.stream()
                .filter(order -> order.getPayTime() != null)
                .filter(order -> !order.getPayTime().isBefore(dayStart) && order.getPayTime().isBefore(nextDay))
                .toList();
            BigDecimal dayRevenue = dayOrders.stream()
                .map(OrderInfoEntity::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyTrend.add(new SalesTrendPointVO(currentDate.toString(), dayRevenue, (long) dayOrders.size()));
        }

        if (paidOrders.isEmpty()) {
            return new SalesStatisticsVO(totalRevenue, totalOrders, avgOrderAmount, dailyTrend, List.of(), List.of());
        }

        List<Long> orderIds = paidOrders.stream().map(OrderInfoEntity::getId).toList();
        List<OrderItemEntity> orderItems = orderItemService.lambdaQuery()
            .in(OrderItemEntity::getOrderId, orderIds)
            .list();
        if (orderItems.isEmpty()) {
            return new SalesStatisticsVO(totalRevenue, totalOrders, avgOrderAmount, dailyTrend, List.of(), List.of());
        }

        Map<Long, ProductEntity> productMap = productService.listByIds(orderItems.stream().map(OrderItemEntity::getProductId).distinct().toList())
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item), HashMap::putAll);
        Map<Long, String> categoryNameMap = categoryService.listByIds(productMap.values().stream().map(ProductEntity::getCategoryId).distinct().toList())
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item.getCategoryName()), HashMap::putAll);

        Map<Long, Long> categorySalesMap = new HashMap<>();
        Map<Long, TopProductVO> productStatsMap = new HashMap<>();
        for (OrderItemEntity item : orderItems) {
            ProductEntity productEntity = productMap.get(item.getProductId());
            if (productEntity != null) {
                categorySalesMap.merge(productEntity.getCategoryId(), item.getQuantity().longValue(), Long::sum);
            }
            TopProductVO current = productStatsMap.get(item.getProductId());
            if (current == null) {
                productStatsMap.put(item.getProductId(), new TopProductVO(item.getProductName(), item.getQuantity().longValue(), item.getSubtotal()));
                continue;
            }
            productStatsMap.put(
                item.getProductId(),
                new TopProductVO(
                    current.productName(),
                    current.sales() + item.getQuantity(),
                    current.revenue().add(item.getSubtotal())
                )
            );
        }

        List<CategoryBreakdownVO> categoryBreakdown = categorySalesMap.entrySet().stream()
            .map(entry -> new CategoryBreakdownVO(categoryNameMap.getOrDefault(entry.getKey(), "未分类"), entry.getValue()))
            .sorted(Comparator.comparing(CategoryBreakdownVO::value).reversed())
            .toList();

        List<TopProductVO> topProducts = productStatsMap.values().stream()
            .sorted(Comparator.comparing(TopProductVO::sales).reversed().thenComparing(TopProductVO::revenue, Comparator.reverseOrder()))
            .limit(5)
            .toList();

        return new SalesStatisticsVO(totalRevenue, totalOrders, avgOrderAmount, dailyTrend, categoryBreakdown, topProducts);
    }

    public ShopInfoVO shopInfo() {
        UserEntity merchant = loadCurrentMerchant();
        return new ShopInfoVO(
            merchant.getShopName(),
            merchant.getShopDesc(),
            merchant.getShopLogo(),
            merchant.getPhone()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShopInfo(ShopInfoUpdateRequest request) {
        UserEntity merchant = loadCurrentMerchant();
        merchant.setShopName(request.shopName().trim());
        merchant.setShopDesc(StringUtils.trimToNull(request.shopDesc()));
        merchant.setShopLogo(StringUtils.trimToNull(request.shopLogo()));
        merchant.setPhone(StringUtils.trimToNull(request.contactPhone()));
        userService.updateById(merchant);
    }

    private UserEntity loadCurrentMerchant() {
        UserEntity userEntity = userService.getById(UserContextHolder.getUserId());
        if (userEntity == null) {
            throw new BusinessException("商家不存在");
        }
        return userEntity;
    }

    private ProductEntity loadMerchantProduct(Long productId) {
        ProductEntity productEntity = productService.getById(productId);
        if (productEntity == null) {
            throw new BusinessException("商品不存在");
        }
        if (!Objects.equals(productEntity.getMerchantId(), UserContextHolder.getUserId())) {
            throw new BusinessException("无权操作其他商家的商品");
        }
        return productEntity;
    }
}
