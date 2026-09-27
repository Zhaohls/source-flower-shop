package com.flowershop.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.annotation.RateLimit;
import com.flowershop.common.config.FlowerProperties;
import com.flowershop.common.constant.LimitTarget;
import com.flowershop.common.constant.RedisKeyConstants;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.common.utils.RedisLockHelper;
import com.flowershop.modules.address.dto.AddressModels.AddressVO;
import com.flowershop.modules.address.entity.AddressEntity;
import com.flowershop.modules.address.service.AddressAppService;
import com.flowershop.modules.banner.entity.BannerEntity;
import com.flowershop.modules.cart.entity.CartItemEntity;
import com.flowershop.modules.cart.service.CartItemService;
import com.flowershop.modules.delivery.entity.DeliveryEntity;
import com.flowershop.modules.delivery.service.DeliveryService;
import com.flowershop.modules.order.dto.OrderModels;
import com.flowershop.modules.order.dto.OrderModels.CardTemplateVO;
import com.flowershop.modules.order.dto.OrderModels.ConfirmOrderItemVO;
import com.flowershop.modules.order.dto.OrderModels.ConfirmOrderRequest;
import com.flowershop.modules.order.dto.OrderModels.ConfirmOrderVO;
import com.flowershop.modules.order.dto.OrderModels.CustomizeRequest;
import com.flowershop.modules.order.dto.OrderModels.DeliveryStatusRequest;
import com.flowershop.modules.order.dto.OrderModels.DeliveryVO;
import com.flowershop.modules.order.dto.OrderModels.MerchantRejectRequest;
import com.flowershop.modules.order.dto.OrderModels.MerchantShipRequest;
import com.flowershop.modules.order.dto.OrderModels.OrderDetailVO;
import com.flowershop.modules.order.dto.OrderModels.OrderItemVO;
import com.flowershop.modules.order.dto.OrderModels.OrderListItemVO;
import com.flowershop.modules.order.dto.OrderModels.OrderSubmitItem;
import com.flowershop.modules.order.dto.OrderModels.OrderSubmitRequest;
import com.flowershop.modules.order.dto.OrderModels.PageResult;
import com.flowershop.modules.order.dto.OrderModels.PayRequest;
import com.flowershop.modules.order.dto.OrderModels.SubmitOrderResponse;
import com.flowershop.modules.order.entity.CardTemplateEntity;
import com.flowershop.modules.order.entity.OrderCustomizeEntity;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.entity.OrderItemEntity;
import com.flowershop.modules.order.enums.OrderStatusEnum;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.mapper.ProductMapper;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.review.entity.ReviewEntity;
import com.flowershop.modules.review.service.ReviewService;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.service.UserService;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final AddressAppService addressAppService;
    private final CartItemService cartItemService;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CardTemplateService cardTemplateService;
    private final OrderInfoService orderInfoService;
    private final OrderItemService orderItemService;
    private final OrderCustomizeService orderCustomizeService;
    private final DeliveryService deliveryService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final RedisLockHelper redisLockHelper;
    private final StringRedisTemplate stringRedisTemplate;
    private final FlowerProperties flowerProperties;

    public ConfirmOrderVO confirmOrder(ConfirmOrderRequest request) {
        List<CartItemEntity> cartItems = cartItemService.lambdaQuery()
            .eq(CartItemEntity::getUserId, UserContextHolder.getUserId())
            .in(CartItemEntity::getId, request.cartItemIds())
            .list();
        if (cartItems.isEmpty()) {
            throw new BusinessException("请选择有效的购物车商品");
        }
        Map<Long, ProductEntity> productMap = loadProductMap(cartItems.stream().map(CartItemEntity::getProductId).toList());
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ConfirmOrderItemVO> items = new ArrayList<>();
        for (CartItemEntity cartItem : cartItems) {
            ProductEntity productEntity = productMap.get(cartItem.getProductId());
            validateProductForOrder(productEntity, cartItem.getQuantity());
            BigDecimal subtotal = productEntity.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            items.add(new ConfirmOrderItemVO(cartItem.getId(), productEntity.getId(), productEntity.getProductName(), productEntity.getMainImage(), productEntity.getPrice(), cartItem.getQuantity(), subtotal, Objects.equals(productEntity.getSupportCustomize(), 1)));
        }
        List<AddressVO> addresses = addressAppService.list();
        List<CardTemplateVO> cardTemplates = listCardTemplates(null);
        return new ConfirmOrderVO(items, addresses, cardTemplates, totalAmount);
    }

    @Transactional(rollbackFor = Exception.class)
    public SubmitOrderResponse submitOrder(OrderSubmitRequest request) {
        AddressEntity addressEntity = addressAppService.getCurrentUserAddress(request.addressId());
        if (request.items().isEmpty()) {
            throw new BusinessException("下单商品不能为空");
        }
        Map<Long, ProductEntity> productMap = loadProductMap(request.items().stream().map(OrderSubmitItem::productId).toList());
        List<ProductEntity> products = request.items().stream().map(item -> productMap.get(item.productId())).toList();
        Long merchantId = validateMerchantConsistency(products);

        List<Long> productIds = request.items().stream().map(OrderSubmitItem::productId).distinct().sorted(Comparator.naturalOrder()).toList();
        Map<String, String> locks = new HashMap<>();
        try {
            for (Long productId : productIds) {
                String lockKey = RedisKeyConstants.STOCK_LOCK + productId;
                String requestId = UUID.randomUUID().toString();
                if (!redisLockHelper.tryLock(lockKey, requestId, Duration.ofSeconds(10))) {
                    throw new BusinessException("当前商品正在抢购中，请稍后重试");
                }
                locks.put(lockKey, requestId);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderSubmitItem item : request.items()) {
                ProductEntity productEntity = productMap.get(item.productId());
                validateProductForOrder(productEntity, item.quantity());
                int rows = productMapper.deductStock(item.productId(), item.quantity());
                if (rows == 0) {
                    throw new BusinessException("库存不足");
                }
                totalAmount = totalAmount.add(productEntity.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            }

            OrderInfoEntity orderInfoEntity = new OrderInfoEntity();
            orderInfoEntity.setOrderNo(generateOrderNo());
            orderInfoEntity.setUserId(UserContextHolder.getUserId());
            orderInfoEntity.setMerchantId(merchantId);
            orderInfoEntity.setTotalAmount(totalAmount);
            orderInfoEntity.setPayAmount(totalAmount);
            orderInfoEntity.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
            orderInfoEntity.setReceiverName(addressEntity.getReceiverName());
            orderInfoEntity.setReceiverPhone(addressEntity.getReceiverPhone());
            orderInfoEntity.setReceiverAddress(
                StringUtils.defaultString(addressEntity.getProvince())
                    + StringUtils.defaultString(addressEntity.getCity())
                    + StringUtils.defaultString(addressEntity.getDistrict())
                    + StringUtils.defaultString(addressEntity.getDetailAddress())
            );
            orderInfoEntity.setDeliveryTime(request.deliveryTime());
            orderInfoEntity.setRemark(request.remark());
            orderInfoService.save(orderInfoEntity);

            List<Long> purchasedProductIds = new ArrayList<>();
            for (OrderSubmitItem item : request.items()) {
                ProductEntity productEntity = productMap.get(item.productId());
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setOrderId(orderInfoEntity.getId());
                orderItemEntity.setProductId(productEntity.getId());
                orderItemEntity.setProductName(productEntity.getProductName());
                orderItemEntity.setProductImage(productEntity.getMainImage());
                orderItemEntity.setPrice(productEntity.getPrice());
                orderItemEntity.setQuantity(item.quantity());
                orderItemEntity.setSubtotal(productEntity.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
                orderItemService.save(orderItemEntity);
                purchasedProductIds.add(productEntity.getId());

                if (item.customize() != null) {
                    OrderCustomizeEntity customizeEntity = new OrderCustomizeEntity();
                    customizeEntity.setOrderItemId(orderItemEntity.getId());
                    customizeEntity.setCardTemplateId(item.customize().cardTemplateId());
                    customizeEntity.setCardContent(item.customize().cardContent());
                    customizeEntity.setWrappingType(item.customize().wrappingType());
                    customizeEntity.setRibbonColor(item.customize().ribbonColor());
                    customizeEntity.setExtraNote(item.customize().extraNote());
                    orderCustomizeService.save(customizeEntity);
                }
            }
            cartItemService.remove(new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, UserContextHolder.getUserId())
                .in(CartItemEntity::getProductId, purchasedProductIds));

            LocalDateTime payExpireTime = LocalDateTime.now().plusMinutes(flowerProperties.getOrder().getTimeoutMinutes());
            stringRedisTemplate.opsForValue().set(RedisKeyConstants.ORDER_TIMEOUT + orderInfoEntity.getOrderNo(), "1", flowerProperties.getOrder().getTimeoutMinutes(), TimeUnit.MINUTES);
            return new SubmitOrderResponse(orderInfoEntity.getOrderNo(), totalAmount, totalAmount, payExpireTime);
        } finally {
            locks.forEach(redisLockHelper::unlock);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo, PayRequest request) {
        OrderInfoEntity orderInfoEntity = loadCustomerOrder(orderNo);
        if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            throw new BusinessException("当前订单状态不允许支付");
        }
        orderInfoEntity.setOrderStatus(OrderStatusEnum.PAID.getCode());
        orderInfoEntity.setPayType(request.payType());
        orderInfoEntity.setPayTime(LocalDateTime.now());
        orderInfoService.updateById(orderInfoEntity);
        for (OrderItemEntity item : listOrderItems(orderInfoEntity.getId())) {
            productMapper.increaseSales(item.getProductId(), item.getQuantity());
        }
        stringRedisTemplate.delete(RedisKeyConstants.ORDER_TIMEOUT + orderNo);
    }

    public PageResult<OrderListItemVO> myOrders(PageQuery pageQuery, Integer status) {
        Page<OrderInfoEntity> page = orderInfoService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<OrderInfoEntity>()
                .eq(OrderInfoEntity::getUserId, UserContextHolder.getUserId())
                .eq(status != null, OrderInfoEntity::getOrderStatus, status)
                .orderByDesc(OrderInfoEntity::getCreateTime));
        return buildOrderPage(page, false);
    }

    public OrderDetailVO myOrderDetail(String orderNo) {
        return buildOrderDetail(loadCustomerOrder(orderNo));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelMyOrder(String orderNo, String reason) {
        OrderInfoEntity orderInfoEntity = loadCustomerOrder(orderNo);
        cancelOrder(orderInfoEntity, reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String orderNo) {
        OrderInfoEntity orderInfoEntity = loadCustomerOrder(orderNo);
        if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.SHIPPING.getCode())) {
            throw new BusinessException("当前订单状态不允许确认收货");
        }
        orderInfoEntity.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        orderInfoService.updateById(orderInfoEntity);
    }

    public List<CardTemplateVO> listCardTemplates(@Nullable String sceneType) {
        return cardTemplateService.lambdaQuery()
            .eq(CardTemplateEntity::getStatus, 1)
            .eq(StringUtils.isNotBlank(sceneType), CardTemplateEntity::getSceneType, sceneType)
            .orderByAsc(CardTemplateEntity::getSort, CardTemplateEntity::getId)
            .list()
            .stream()
            .map(item -> new CardTemplateVO(item.getId(), item.getTemplateName(), item.getTemplateImage(), item.getSceneType()))
            .toList();
    }

    public PageResult<OrderListItemVO> merchantOrders(PageQuery pageQuery, Integer status) {
        Page<OrderInfoEntity> page = orderInfoService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<OrderInfoEntity>()
                .eq(OrderInfoEntity::getMerchantId, UserContextHolder.getUserId())
                .eq(status != null, OrderInfoEntity::getOrderStatus, status)
                .orderByDesc(OrderInfoEntity::getCreateTime));
        return buildOrderPage(page, true);
    }

    public OrderDetailVO merchantOrderDetail(String orderNo) {
        return buildOrderDetail(loadMerchantOrder(orderNo));
    }

    public PageResult<OrderListItemVO> adminOrders(PageQuery pageQuery, Integer status, Long merchantId) {
        Page<OrderInfoEntity> page = orderInfoService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<OrderInfoEntity>()
                .eq(status != null, OrderInfoEntity::getOrderStatus, status)
                .eq(merchantId != null, OrderInfoEntity::getMerchantId, merchantId)
                .orderByDesc(OrderInfoEntity::getCreateTime));
        return buildOrderPage(page, false);
    }

    public OrderDetailVO adminOrderDetail(String orderNo) {
        OrderInfoEntity orderInfoEntity = orderInfoService.lambdaQuery().eq(OrderInfoEntity::getOrderNo, orderNo).one();
        if (orderInfoEntity == null) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderDetail(orderInfoEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(String orderNo) {
        OrderInfoEntity orderInfoEntity = loadMerchantOrder(orderNo);
        if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.PAID.getCode())) {
            throw new BusinessException("仅已支付订单可以接单");
        }
        orderInfoEntity.setOrderStatus(OrderStatusEnum.PREPARING.getCode());
        orderInfoService.updateById(orderInfoEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(String orderNo, MerchantRejectRequest request) {
        OrderInfoEntity orderInfoEntity = loadMerchantOrder(orderNo);
        cancelOrder(orderInfoEntity, request.reason());
    }

    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, MerchantShipRequest request) {
        OrderInfoEntity orderInfoEntity = loadMerchantOrder(orderNo);
        if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.PREPARING.getCode())) {
            throw new BusinessException("仅备货中的订单可以发货");
        }
        orderInfoEntity.setOrderStatus(OrderStatusEnum.SHIPPING.getCode());
        orderInfoService.updateById(orderInfoEntity);

        DeliveryEntity deliveryEntity = deliveryService.lambdaQuery().eq(DeliveryEntity::getOrderId, orderInfoEntity.getId()).one();
        if (deliveryEntity == null) {
            deliveryEntity = new DeliveryEntity();
            deliveryEntity.setOrderId(orderInfoEntity.getId());
        }
        deliveryEntity.setDeliveryMan(request.deliveryMan());
        deliveryEntity.setDeliveryPhone(request.deliveryPhone());
        deliveryEntity.setDeliveryStatus(1);
        deliveryEntity.setShippedTime(LocalDateTime.now());
        deliveryEntity.setRemark(StringUtils.defaultIfBlank(request.deliveryCompany(), "店内配送"));
        deliveryService.saveOrUpdate(deliveryEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatusRequest request) {
        DeliveryEntity deliveryEntity = deliveryService.getById(deliveryId);
        if (deliveryEntity == null) {
            throw new BusinessException("配送记录不存在");
        }
        OrderInfoEntity orderInfoEntity = orderInfoService.getById(deliveryEntity.getOrderId());
        if (orderInfoEntity == null || !Objects.equals(orderInfoEntity.getMerchantId(), UserContextHolder.getUserId())) {
            throw new BusinessException("无权操作该配送单");
        }
        deliveryEntity.setDeliveryStatus(request.deliveryStatus());
        deliveryEntity.setRemark(request.remark());
        if (Objects.equals(request.deliveryStatus(), 2)) {
            deliveryEntity.setDeliveredTime(LocalDateTime.now());
        }
        deliveryService.updateById(deliveryEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoCancelTimeoutOrder(String orderNo) {
        OrderInfoEntity orderInfoEntity = orderInfoService.lambdaQuery().eq(OrderInfoEntity::getOrderNo, orderNo).one();
        if (orderInfoEntity == null || !Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            return;
        }
        cancelOrder(orderInfoEntity, "订单超时自动取消");
    }

    @Transactional(rollbackFor = Exception.class)
    public void scanAndCancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(flowerProperties.getOrder().getTimeoutMinutes());
        List<OrderInfoEntity> timeoutOrders = orderInfoService.lambdaQuery()
            .eq(OrderInfoEntity::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
            .lt(OrderInfoEntity::getCreateTime, deadline)
            .list();
        for (OrderInfoEntity timeoutOrder : timeoutOrders) {
            cancelOrder(timeoutOrder, "订单超时自动取消");
        }
    }

    private PageResult<OrderListItemVO> buildOrderPage(Page<OrderInfoEntity> page, boolean useCustomerName) {
        List<OrderListItemVO> records = page.getRecords().stream().map(order -> {
            UserEntity displayUser = useCustomerName
                ? userService.getById(order.getUserId())
                : userService.getById(order.getMerchantId());
            String displayName = displayUser == null
                ? "-"
                : useCustomerName
                    ? StringUtils.defaultIfBlank(displayUser.getNickname(), displayUser.getUsername())
                    : StringUtils.defaultIfBlank(displayUser.getShopName(), displayUser.getUsername());
            return new OrderListItemVO(order.getId(), order.getOrderNo(), order.getOrderStatus(), statusText(order.getOrderStatus()), order.getPayAmount(), displayName, order.getReceiverName(), order.getReceiverPhone(), order.getCreateTime(), buildOrderItems(order.getId()));
        }).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    private OrderDetailVO buildOrderDetail(OrderInfoEntity orderInfoEntity) {
        DeliveryEntity deliveryEntity = deliveryService.lambdaQuery().eq(DeliveryEntity::getOrderId, orderInfoEntity.getId()).one();
        DeliveryVO deliveryVO = deliveryEntity == null ? null : new DeliveryVO(deliveryEntity.getId(), deliveryEntity.getDeliveryMan(), deliveryEntity.getDeliveryPhone(), deliveryEntity.getDeliveryStatus(), deliveryEntity.getRemark(), deliveryEntity.getShippedTime(), deliveryEntity.getDeliveredTime());
        UserEntity merchant = userService.getById(orderInfoEntity.getMerchantId());
        String merchantName = merchant == null ? "-" : StringUtils.defaultIfBlank(merchant.getShopName(), merchant.getUsername());
        return new OrderDetailVO(
            orderInfoEntity.getId(),
            orderInfoEntity.getOrderNo(),
            orderInfoEntity.getOrderStatus(),
            statusText(orderInfoEntity.getOrderStatus()),
            orderInfoEntity.getTotalAmount(),
            orderInfoEntity.getPayAmount(),
            merchantName,
            orderInfoEntity.getPayType(),
            orderInfoEntity.getPayTime(),
            orderInfoEntity.getReceiverName(),
            orderInfoEntity.getReceiverPhone(),
            orderInfoEntity.getReceiverAddress(),
            orderInfoEntity.getDeliveryTime(),
            orderInfoEntity.getRemark(),
            orderInfoEntity.getCancelReason(),
            orderInfoEntity.getCreateTime(),
            buildOrderItems(orderInfoEntity.getId()),
            deliveryVO
        );
    }

    private List<OrderItemVO> buildOrderItems(Long orderId) {
        List<OrderItemEntity> orderItems = listOrderItems(orderId);
        List<Long> orderItemIds = orderItems.stream().map(OrderItemEntity::getId).toList();
        Map<Long, OrderCustomizeEntity> customizeMap = orderCustomizeService.lambdaQuery()
            .in(!orderItems.isEmpty(), OrderCustomizeEntity::getOrderItemId, orderItemIds)
            .list()
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getOrderItemId(), item), HashMap::putAll);
        Map<Long, Boolean> reviewedMap = reviewService.lambdaQuery()
            .in(!orderItemIds.isEmpty(), ReviewEntity::getOrderItemId, orderItemIds)
            .list()
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getOrderItemId(), Boolean.TRUE), HashMap::putAll);
        return orderItems.stream().map(item -> {
            OrderCustomizeEntity customizeEntity = customizeMap.get(item.getId());
            CustomizeRequest customizeRequest = customizeEntity == null ? null : new CustomizeRequest(customizeEntity.getCardTemplateId(), customizeEntity.getCardContent(), customizeEntity.getWrappingType(), customizeEntity.getRibbonColor(), customizeEntity.getExtraNote());
            return new OrderItemVO(item.getId(), item.getProductId(), item.getProductName(), item.getProductImage(), item.getPrice(), item.getQuantity(), item.getSubtotal(), reviewedMap.getOrDefault(item.getId(), Boolean.FALSE), customizeRequest);
        }).toList();
    }

    private List<OrderItemEntity> listOrderItems(Long orderId) {
        return orderItemService.lambdaQuery().eq(OrderItemEntity::getOrderId, orderId).list();
    }

    private void cancelOrder(OrderInfoEntity orderInfoEntity, String reason) {
        if (!List.of(OrderStatusEnum.PENDING_PAYMENT.getCode(), OrderStatusEnum.PAID.getCode(), OrderStatusEnum.PREPARING.getCode()).contains(orderInfoEntity.getOrderStatus())) {
            throw new BusinessException("当前订单状态不允许取消");
        }
        List<OrderItemEntity> orderItems = listOrderItems(orderInfoEntity.getId());
        for (OrderItemEntity item : orderItems) {
            productMapper.rollbackStock(item.getProductId(), item.getQuantity());
            if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.PENDING_PAYMENT.getCode())) {
                productMapper.decreaseSales(item.getProductId(), item.getQuantity());
            }
        }
        orderInfoEntity.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        orderInfoEntity.setCancelReason(reason);
        orderInfoService.updateById(orderInfoEntity);
        stringRedisTemplate.delete(RedisKeyConstants.ORDER_TIMEOUT + orderInfoEntity.getOrderNo());
    }

    private OrderInfoEntity loadCustomerOrder(String orderNo) {
        OrderInfoEntity orderInfoEntity = orderInfoService.lambdaQuery()
            .eq(OrderInfoEntity::getOrderNo, orderNo)
            .eq(OrderInfoEntity::getUserId, UserContextHolder.getUserId())
            .one();
        if (orderInfoEntity == null) {
            throw new BusinessException("订单不存在");
        }
        return orderInfoEntity;
    }

    private OrderInfoEntity loadMerchantOrder(String orderNo) {
        OrderInfoEntity orderInfoEntity = orderInfoService.lambdaQuery()
            .eq(OrderInfoEntity::getOrderNo, orderNo)
            .eq(OrderInfoEntity::getMerchantId, UserContextHolder.getUserId())
            .one();
        if (orderInfoEntity == null) {
            throw new BusinessException("订单不存在");
        }
        return orderInfoEntity;
    }

    private Map<Long, ProductEntity> loadProductMap(List<Long> productIds) {
        List<ProductEntity> products = productService.listByIds(productIds);
        return products.stream().collect(HashMap::new, (map, item) -> map.put(item.getId(), item), HashMap::putAll);
    }

    private void validateProductForOrder(ProductEntity productEntity, Integer quantity) {
        if (productEntity == null || !Objects.equals(productEntity.getStatus(), 1)) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (productEntity.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
    }

    private Long validateMerchantConsistency(List<ProductEntity> products) {
        if (products.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException("存在无效商品");
        }
        Long merchantId = products.get(0).getMerchantId();
        boolean sameMerchant = products.stream().allMatch(product -> Objects.equals(product.getMerchantId(), merchantId));
        if (!sameMerchant) {
            throw new BusinessException("一次下单仅支持同一商家的商品");
        }
        return merchantId;
    }

    private String statusText(Integer orderStatus) {
        return switch (orderStatus) {
            case 0 -> "待支付";
            case 1 -> "已支付待接单";
            case 2 -> "备货中";
            case 3 -> "配送中";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知状态";
        };
    }

    private String generateOrderNo() {
        return "FS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%03d", (int) (Math.random() * 1000));
    }
}
