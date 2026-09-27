package com.flowershop.modules.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.common.utils.JsonUtils;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.entity.OrderItemEntity;
import com.flowershop.modules.order.enums.OrderStatusEnum;
import com.flowershop.modules.order.service.OrderInfoService;
import com.flowershop.modules.order.service.OrderItemService;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.review.dto.ReviewModels.MerchantReviewVO;
import com.flowershop.modules.review.dto.ReviewModels.PageResult;
import com.flowershop.modules.review.dto.ReviewModels.ProductReviewVO;
import com.flowershop.modules.review.dto.ReviewModels.ReviewReplyRequest;
import com.flowershop.modules.review.dto.ReviewModels.ReviewSubmitRequest;
import com.flowershop.modules.review.entity.ReviewEntity;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.service.UserService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewAppService {

    private final ReviewService reviewService;
    private final OrderInfoService orderInfoService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserService userService;
    private final JsonUtils jsonUtils;

    @Transactional(rollbackFor = Exception.class)
    public void submit(ReviewSubmitRequest request) {
        OrderInfoEntity orderInfoEntity = orderInfoService.getById(request.orderId());
        if (orderInfoEntity == null || !Objects.equals(orderInfoEntity.getUserId(), UserContextHolder.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        if (!Objects.equals(orderInfoEntity.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
            throw new BusinessException("仅已完成订单支持评价");
        }
        OrderItemEntity orderItemEntity = orderItemService.getById(request.orderItemId());
        if (orderItemEntity == null || !Objects.equals(orderItemEntity.getOrderId(), request.orderId())) {
            throw new BusinessException("订单明细不存在");
        }
        boolean reviewed = reviewService.lambdaQuery().eq(ReviewEntity::getOrderItemId, request.orderItemId()).exists();
        if (reviewed) {
            throw new BusinessException("该商品已评价，不能重复提交");
        }
        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setOrderItemId(orderItemEntity.getId());
        reviewEntity.setUserId(UserContextHolder.getUserId());
        reviewEntity.setProductId(orderItemEntity.getProductId());
        reviewEntity.setRating(request.rating());
        reviewEntity.setContent(request.content());
        reviewEntity.setImages(jsonUtils.toJson(request.images() == null ? List.of() : request.images()));
        reviewService.save(reviewEntity);
    }

    public PageResult<ProductReviewVO> productReviews(Long productId, PageQuery pageQuery) {
        Page<ReviewEntity> page = reviewService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getProductId, productId)
                .orderByDesc(ReviewEntity::getCreateTime));
        Map<Long, UserEntity> userMap = loadUserMap(page.getRecords().stream().map(ReviewEntity::getUserId).toList());
        List<ProductReviewVO> records = page.getRecords().stream()
            .map(item -> {
                UserEntity userEntity = userMap.get(item.getUserId());
                return new ProductReviewVO(item.getId(), userEntity == null ? "匿名用户" : userEntity.getNickname(), userEntity == null ? "" : userEntity.getAvatar(), item.getRating(), item.getContent(), jsonUtils.toStringList(item.getImages()), item.getReplyContent(), item.getReplyTime(), item.getCreateTime());
            })
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public PageResult<MerchantReviewVO> merchantReviews(PageQuery pageQuery, Boolean replied) {
        List<ProductEntity> merchantProducts = productService.lambdaQuery().eq(ProductEntity::getMerchantId, UserContextHolder.getUserId()).list();
        if (merchantProducts.isEmpty()) {
            return new PageResult<>(List.of(), 0L, pageQuery.current(), pageQuery.size(), 0L);
        }
        List<Long> productIds = merchantProducts.stream().map(ProductEntity::getId).toList();
        Page<ReviewEntity> page = reviewService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewEntity>()
                .in(ReviewEntity::getProductId, productIds)
                .isNull(Boolean.FALSE.equals(replied), ReviewEntity::getReplyContent)
                .isNotNull(Boolean.TRUE.equals(replied), ReviewEntity::getReplyContent)
                .orderByDesc(ReviewEntity::getCreateTime));
        Map<Long, String> productNameMap = merchantProducts.stream().collect(HashMap::new, (map, item) -> map.put(item.getId(), item.getProductName()), HashMap::putAll);
        Map<Long, UserEntity> userMap = loadUserMap(page.getRecords().stream().map(ReviewEntity::getUserId).toList());
        List<MerchantReviewVO> records = page.getRecords().stream()
            .map(item -> new MerchantReviewVO(item.getId(), item.getOrderItemId(), item.getProductId(), productNameMap.getOrDefault(item.getProductId(), ""), userMap.get(item.getUserId()) == null ? "匿名用户" : userMap.get(item.getUserId()).getNickname(), item.getRating(), item.getContent(), jsonUtils.toStringList(item.getImages()), item.getReplyContent(), item.getCreateTime()))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Transactional(rollbackFor = Exception.class)
    public void reply(Long reviewId, ReviewReplyRequest request) {
        ReviewEntity reviewEntity = reviewService.getById(reviewId);
        if (reviewEntity == null) {
            throw new BusinessException("评价不存在");
        }
        ProductEntity productEntity = productService.getById(reviewEntity.getProductId());
        if (productEntity == null || !Objects.equals(productEntity.getMerchantId(), UserContextHolder.getUserId())) {
            throw new BusinessException("无权回复该评价");
        }
        reviewEntity.setReplyContent(request.replyContent());
        reviewEntity.setReplyTime(LocalDateTime.now());
        reviewService.updateById(reviewEntity);
    }

    private Map<Long, UserEntity> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userService.listByIds(userIds).stream().collect(HashMap::new, (map, item) -> map.put(item.getId(), item), HashMap::putAll);
    }
}
