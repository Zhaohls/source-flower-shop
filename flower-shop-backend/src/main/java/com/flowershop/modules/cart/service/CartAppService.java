package com.flowershop.modules.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.modules.cart.dto.CartModels.CartItemVO;
import com.flowershop.modules.cart.dto.CartModels.CartListVO;
import com.flowershop.modules.cart.entity.CartItemEntity;
import com.flowershop.modules.cart.mapper.CartItemMapper;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.enums.ProductStatusEnum;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.service.UserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartAppService {

    private final CartItemService cartItemService;
    private final CartItemMapper cartItemMapper;
    private final ProductService productService;
    private final UserService userService;

    public void add(Long productId, Integer quantity) {
        ProductEntity productEntity = productService.getById(productId);
        if (productEntity == null || Objects.equals(productEntity.getStatus(), ProductStatusEnum.OFF_SHELF.getCode())) {
            throw new BusinessException("商品不存在或已下架");
        }
        cartItemMapper.upsert(UserContextHolder.getUserId(), productId, quantity);
    }

    public CartListVO list() {
        List<CartItemEntity> cartItems = cartItemService.lambdaQuery()
            .eq(CartItemEntity::getUserId, UserContextHolder.getUserId())
            .orderByDesc(CartItemEntity::getUpdateTime)
            .list();
        if (cartItems.isEmpty()) {
            return new CartListVO(List.of(), BigDecimal.ZERO, 0);
        }

        Map<Long, ProductEntity> productMap = productService.listByIds(cartItems.stream().map(CartItemEntity::getProductId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(ProductEntity::getId, item -> item));
        List<Long> merchantIds = productMap.values().stream()
            .map(ProductEntity::getMerchantId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, UserEntity> merchantMap = merchantIds.isEmpty()
            ? Map.of()
            : userService.listByIds(merchantIds).stream().collect(Collectors.toMap(UserEntity::getId, item -> item));
        List<CartItemVO> items = cartItems.stream().map(cartItem -> {
            ProductEntity productEntity = productMap.get(cartItem.getProductId());
            UserEntity merchantEntity = productEntity == null ? null : merchantMap.get(productEntity.getMerchantId());
            boolean invalid = productEntity == null || Objects.equals(productEntity.getStatus(), ProductStatusEnum.OFF_SHELF.getCode()) || productEntity.getStock() < cartItem.getQuantity();
            String invalidReason = invalid
                ? (productEntity == null ? "商品不存在" : (Objects.equals(productEntity.getStatus(), ProductStatusEnum.OFF_SHELF.getCode()) ? "商品已下架" : "库存不足"))
                : "";
            return new CartItemVO(
                cartItem.getId(),
                cartItem.getProductId(),
                productEntity == null ? "" : productEntity.getProductName(),
                productEntity == null ? null : productEntity.getMerchantId(),
                merchantEntity == null ? "" : merchantEntity.getShopName(),
                productEntity == null ? "" : productEntity.getMainImage(),
                productEntity == null ? BigDecimal.ZERO : productEntity.getPrice(),
                cartItem.getQuantity(),
                cartItem.getSelected(),
                productEntity == null ? 0 : productEntity.getStock(),
                invalid,
                invalidReason,
                productEntity != null && Objects.equals(productEntity.getSupportCustomize(), 1)
            );
        }).toList();
        BigDecimal totalAmount = items.stream()
            .filter(item -> !Boolean.TRUE.equals(item.invalid()) && Objects.equals(item.selected(), 1))
            .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartListVO(items, totalAmount, items.size());
    }

    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItemEntity cartItemEntity = loadCartItem(cartItemId);
        cartItemEntity.setQuantity(quantity);
        cartItemService.updateById(cartItemEntity);
    }

    public void delete(Long cartItemId) {
        cartItemService.remove(new LambdaQueryWrapper<CartItemEntity>()
            .eq(CartItemEntity::getId, cartItemId)
            .eq(CartItemEntity::getUserId, UserContextHolder.getUserId()));
    }

    public void selectAll(Boolean selected) {
        List<CartItemEntity> items = cartItemService.lambdaQuery().eq(CartItemEntity::getUserId, UserContextHolder.getUserId()).list();
        for (CartItemEntity item : items) {
            item.setSelected(Boolean.TRUE.equals(selected) ? 1 : 0);
        }
        cartItemService.updateBatchById(items);
    }

    public void updateSelection(Long cartItemId, Boolean selected) {
        CartItemEntity cartItemEntity = loadCartItem(cartItemId);
        cartItemEntity.setSelected(Boolean.TRUE.equals(selected) ? 1 : 0);
        cartItemService.updateById(cartItemEntity);
    }

    private CartItemEntity loadCartItem(Long cartItemId) {
        CartItemEntity cartItemEntity = cartItemService.getById(cartItemId);
        if (cartItemEntity == null || !Objects.equals(cartItemEntity.getUserId(), UserContextHolder.getUserId())) {
            throw new BusinessException("购物车项不存在");
        }
        return cartItemEntity;
    }
}
