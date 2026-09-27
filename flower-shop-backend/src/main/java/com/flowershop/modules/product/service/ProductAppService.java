package com.flowershop.modules.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.constant.RedisKeyConstants;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.common.utils.JsonUtils;
import com.flowershop.modules.banner.entity.BannerEntity;
import com.flowershop.modules.banner.service.BannerService;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.category.service.CategoryService;
import com.flowershop.modules.notice.entity.NoticeEntity;
import com.flowershop.modules.notice.service.NoticeService;
import com.flowershop.modules.product.dto.ProductModels.AdminProductItem;
import com.flowershop.modules.product.dto.ProductModels.BannerSimpleVO;
import com.flowershop.modules.product.dto.ProductModels.CategorySimpleVO;
import com.flowershop.modules.product.dto.ProductModels.CustomerHomeVO;
import com.flowershop.modules.product.dto.ProductModels.CustomerProductItem;
import com.flowershop.modules.product.dto.ProductModels.MerchantProductDetailVO;
import com.flowershop.modules.product.dto.ProductModels.MerchantProductItem;
import com.flowershop.modules.product.dto.ProductModels.MerchantSimpleVO;
import com.flowershop.modules.product.dto.ProductModels.NoticeSimpleVO;
import com.flowershop.modules.product.dto.ProductModels.ProductDetailVO;
import com.flowershop.modules.product.dto.ProductModels.ProductPageResult;
import com.flowershop.modules.product.dto.ProductModels.ProductSaveRequest;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.enums.ProductStatusEnum;
import com.flowershop.modules.review.entity.ReviewEntity;
import com.flowershop.modules.review.service.ReviewService;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductAppService {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final BannerService bannerService;
    private final NoticeService noticeService;
    private final ReviewService reviewService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonUtils jsonUtils;

    public ProductPageResult<MerchantProductItem> getMerchantProducts(PageQuery pageQuery, String keyword, Integer status) {
        Page<ProductEntity> page = productService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<ProductEntity>()
                .eq(ProductEntity::getMerchantId, UserContextHolder.getUserId())
                .eq(status != null, ProductEntity::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper.like(ProductEntity::getProductName, keyword).or().like(ProductEntity::getProductNo, keyword))
                .orderByDesc(ProductEntity::getUpdateTime));
        return buildMerchantPage(page);
    }

    public Long createMerchantProduct(ProductSaveRequest request) {
        CategoryEntity categoryEntity = loadCategory(request.categoryId());
        ProductEntity productEntity = new ProductEntity();
        fillProduct(productEntity, request, categoryEntity);
        productEntity.setMerchantId(UserContextHolder.getUserId());
        productEntity.setProductNo(generateProductNo(categoryEntity.getId()));
        productEntity.setSales(0);
        productEntity.setStatus(ProductStatusEnum.OFF_SHELF.getCode());
        productService.save(productEntity);
        return productEntity.getId();
    }

    public MerchantProductDetailVO getMerchantProductDetail(Long id) {
        ProductEntity productEntity = loadMerchantOwnedProduct(id);
        return new MerchantProductDetailVO(
            productEntity.getId(),
            productEntity.getCategoryId(),
            productEntity.getProductNo(),
            productEntity.getProductName(),
            productEntity.getMainImage(),
            jsonUtils.toStringList(productEntity.getImages()),
            productEntity.getPrice(),
            productEntity.getOriginalPrice(),
            productEntity.getStock(),
            productEntity.getFlowerLanguage(),
            productEntity.getMaterialDesc(),
            productEntity.getDetailContent(),
            productEntity.getSupportCustomize() != null && productEntity.getSupportCustomize() == 1,
            productEntity.getStatus()
        );
    }

    public void updateMerchantProduct(Long id, ProductSaveRequest request) {
        ProductEntity productEntity = loadMerchantOwnedProduct(id);
        fillProduct(productEntity, request, loadCategory(request.categoryId()));
        productService.updateById(productEntity);
        evictProductCache(id);
    }

    public void deleteMerchantProduct(Long id) {
        ProductEntity productEntity = loadMerchantOwnedProduct(id);
        productService.removeById(productEntity.getId());
        evictProductCache(id);
    }

    public void updateMerchantStatus(Long id, Integer status) {
        ProductEntity productEntity = loadMerchantOwnedProduct(id);
        productEntity.setStatus(status);
        productService.updateById(productEntity);
        evictProductCache(id);
    }

    public CustomerHomeVO getCustomerHome() {
        List<BannerSimpleVO> banners = bannerService.lambdaQuery()
            .eq(BannerEntity::getStatus, 1)
            .orderByAsc(BannerEntity::getSort, BannerEntity::getId)
            .list()
            .stream()
            .map(item -> new BannerSimpleVO(item.getId(), item.getImageUrl(), item.getLinkUrl(), item.getTitle()))
            .toList();
        List<CategorySimpleVO> categories = categoryService.lambdaQuery()
            .eq(CategoryEntity::getStatus, 1)
            .orderByAsc(CategoryEntity::getSort, CategoryEntity::getId)
            .list()
            .stream()
            .map(item -> new CategorySimpleVO(item.getId(), item.getCategoryName(), item.getCategoryIcon()))
            .toList();
        List<CustomerProductItem> hotProducts = getHotProducts();
        List<CustomerProductItem> newProducts = mapCustomerProducts(productService.lambdaQuery()
            .eq(ProductEntity::getStatus, ProductStatusEnum.ON_SHELF.getCode())
            .orderByDesc(ProductEntity::getCreateTime)
            .last("limit 6")
            .list());
        List<NoticeSimpleVO> notices = noticeService.lambdaQuery()
            .eq(NoticeEntity::getStatus, 1)
            .orderByDesc(NoticeEntity::getCreateTime)
            .last("limit 5")
            .list()
            .stream()
            .map(item -> new NoticeSimpleVO(item.getId(), item.getTitle(), item.getCreateTime()))
            .toList();
        return new CustomerHomeVO(banners, categories, hotProducts, newProducts, notices);
    }

    public ProductPageResult<CustomerProductItem> getCustomerProducts(
        PageQuery pageQuery,
        Long categoryId,
        String keyword,
        String sortBy,
        String sortOrder,
        BigDecimal minPrice,
        BigDecimal maxPrice
    ) {
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<ProductEntity>()
            .eq(ProductEntity::getStatus, ProductStatusEnum.ON_SHELF.getCode())
            .eq(categoryId != null, ProductEntity::getCategoryId, categoryId)
            .ge(minPrice != null, ProductEntity::getPrice, minPrice)
            .le(maxPrice != null, ProductEntity::getPrice, maxPrice)
            .and(StringUtils.isNotBlank(keyword), item -> item.like(ProductEntity::getProductName, keyword).or().like(ProductEntity::getFlowerLanguage, keyword));

        boolean desc = !"asc".equalsIgnoreCase(sortOrder);
        String normalizedSort = StringUtils.defaultIfBlank(sortBy, "createTime");
        if ("sales".equalsIgnoreCase(normalizedSort)) {
            wrapper.orderBy(true, !desc, ProductEntity::getSales);
        } else if ("price".equalsIgnoreCase(normalizedSort)) {
            wrapper.orderBy(true, !desc, ProductEntity::getPrice);
        } else {
            wrapper.orderByDesc(ProductEntity::getCreateTime);
        }

        Page<ProductEntity> page = productService.page(new Page<>(pageQuery.current(), pageQuery.size()), wrapper);
        List<CustomerProductItem> records = mapCustomerProducts(page.getRecords());
        return new ProductPageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public ProductDetailVO getCustomerProductDetail(Long id) {
        String cacheKey = RedisKeyConstants.PRODUCT + id;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.equals(cachedValue, "NULL")) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (StringUtils.isNotBlank(cachedValue)) {
            return jsonUtils.fromJson(cachedValue, ProductDetailVO.class);
        }

        ProductEntity productEntity = productService.lambdaQuery()
            .eq(ProductEntity::getId, id)
            .eq(ProductEntity::getStatus, ProductStatusEnum.ON_SHELF.getCode())
            .one();
        if (productEntity == null) {
            stringRedisTemplate.opsForValue().set(cacheKey, "NULL", 5, TimeUnit.MINUTES);
            throw new BusinessException("商品不存在或已下架");
        }

        CategoryEntity categoryEntity = categoryService.getById(productEntity.getCategoryId());
        UserEntity merchant = userService.getById(productEntity.getMerchantId());
        List<ReviewEntity> reviews = reviewService.lambdaQuery().eq(ReviewEntity::getProductId, id).list();
        long totalCount = reviews.size();
        BigDecimal avgRating = totalCount == 0 ? BigDecimal.ZERO :
            BigDecimal.valueOf(reviews.stream().mapToInt(ReviewEntity::getRating).average().orElse(0D)).setScale(1, RoundingMode.HALF_UP);
        long goodCount = reviews.stream().filter(review -> review.getRating() != null && review.getRating() >= 4).count();
        String goodRate = totalCount == 0 ? "0%" : BigDecimal.valueOf(goodCount * 100D / totalCount).setScale(0, RoundingMode.HALF_UP) + "%";

        ProductDetailVO detailVO = new ProductDetailVO(
            productEntity.getId(),
            productEntity.getProductName(),
            productEntity.getProductNo(),
            productEntity.getMainImage(),
            jsonUtils.toStringList(productEntity.getImages()),
            productEntity.getPrice(),
            productEntity.getOriginalPrice(),
            productEntity.getStock(),
            productEntity.getSales(),
            productEntity.getFlowerLanguage(),
            productEntity.getMaterialDesc(),
            productEntity.getDetailContent(),
            Objects.equals(productEntity.getSupportCustomize(), 1),
            categoryEntity == null ? "" : categoryEntity.getCategoryName(),
            new MerchantSimpleVO(merchant == null ? null : merchant.getId(), merchant == null ? "" : merchant.getShopName(), merchant == null ? "" : merchant.getShopLogo()),
            new com.flowershop.modules.product.dto.ProductModels.ReviewSummaryVO(avgRating, totalCount, goodRate)
        );
        stringRedisTemplate.opsForValue().set(cacheKey, jsonUtils.toJson(detailVO), 30, TimeUnit.MINUTES);
        return detailVO;
    }

    public ProductPageResult<AdminProductItem> getAdminProducts(PageQuery pageQuery, Long merchantId, Integer status, String keyword) {
        Page<ProductEntity> page = productService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new LambdaQueryWrapper<ProductEntity>()
                .eq(merchantId != null, ProductEntity::getMerchantId, merchantId)
                .eq(status != null, ProductEntity::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper.like(ProductEntity::getProductName, keyword).or().like(ProductEntity::getProductNo, keyword))
                .orderByDesc(ProductEntity::getUpdateTime));
        List<AdminProductItem> records = mapAdminProducts(page.getRecords());
        return new ProductPageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public void updateAdminProduct(Long id, ProductSaveRequest request) {
        ProductEntity productEntity = productService.getById(id);
        if (productEntity == null) {
            throw new BusinessException("商品不存在");
        }
        fillProduct(productEntity, request, loadCategory(request.categoryId()));
        productService.updateById(productEntity);
        evictProductCache(id);
    }

    public void deleteAdminProduct(Long id) {
        if (!productService.removeById(id)) {
            throw new BusinessException("商品不存在");
        }
        evictProductCache(id);
    }

    public void updateAdminStatus(Long id, Integer status) {
        ProductEntity productEntity = productService.getById(id);
        if (productEntity == null) {
            throw new BusinessException("商品不存在");
        }
        productEntity.setStatus(status);
        productService.updateById(productEntity);
        evictProductCache(id);
    }

    private ProductPageResult<MerchantProductItem> buildMerchantPage(Page<ProductEntity> page) {
        List<MerchantProductItem> records = mapMerchantProducts(page.getRecords());
        return new ProductPageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    private void fillProduct(ProductEntity productEntity, ProductSaveRequest request, CategoryEntity categoryEntity) {
        productEntity.setCategoryId(categoryEntity.getId());
        productEntity.setProductName(request.productName());
        productEntity.setMainImage(request.mainImage());
        productEntity.setImages(jsonUtils.toJson(request.images() == null ? Collections.emptyList() : request.images()));
        productEntity.setPrice(request.price());
        productEntity.setOriginalPrice(request.originalPrice());
        productEntity.setStock(request.stock());
        productEntity.setFlowerLanguage(request.flowerLanguage());
        productEntity.setMaterialDesc(request.materialDesc());
        productEntity.setDetailContent(request.detailContent());
        productEntity.setSupportCustomize(Boolean.TRUE.equals(request.supportCustomize()) ? 1 : 0);
    }

    private CategoryEntity loadCategory(Long categoryId) {
        CategoryEntity categoryEntity = categoryService.getById(categoryId);
        if (categoryEntity == null) {
            throw new BusinessException("商品分类不存在");
        }
        return categoryEntity;
    }

    private ProductEntity loadMerchantOwnedProduct(Long id) {
        ProductEntity productEntity = productService.getById(id);
        if (productEntity == null) {
            throw new BusinessException("商品不存在");
        }
        if (!Objects.equals(productEntity.getMerchantId(), UserContextHolder.getUserId())) {
            throw new BusinessException("无权操作其他商家的商品");
        }
        return productEntity;
    }

    private List<CustomerProductItem> getHotProducts() {
        String cacheKey = RedisKeyConstants.HOT_PRODUCT;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cachedValue)) {
            return jsonUtils.fromJson(cachedValue, new TypeReference<List<CustomerProductItem>>() {
            });
        }
        List<CustomerProductItem> hotProducts = mapCustomerProducts(productService.lambdaQuery()
            .eq(ProductEntity::getStatus, ProductStatusEnum.ON_SHELF.getCode())
            .orderByDesc(ProductEntity::getSales)
            .last("limit 6")
            .list());
        stringRedisTemplate.opsForValue().set(cacheKey, jsonUtils.toJson(hotProducts), 10, TimeUnit.MINUTES);
        return hotProducts;
    }

    private List<MerchantProductItem> mapMerchantProducts(List<ProductEntity> entities) {
        Map<Long, String> categoryMap = loadCategoryNameMap(entities);
        return entities.stream()
            .map(item -> new MerchantProductItem(
                item.getId(),
                item.getProductNo(),
                item.getProductName(),
                item.getMainImage(),
                item.getPrice(),
                item.getOriginalPrice(),
                item.getStock(),
                item.getSales(),
                item.getStatus(),
                categoryMap.getOrDefault(item.getCategoryId(), ""),
                item.getCreateTime()
            ))
            .toList();
    }

    private List<AdminProductItem> mapAdminProducts(List<ProductEntity> entities) {
        Map<Long, String> categoryMap = loadCategoryNameMap(entities);
        Map<Long, String> merchantNameMap = loadMerchantNameMap(entities);
        return entities.stream()
            .map(item -> new AdminProductItem(
                item.getId(),
                item.getProductNo(),
                item.getProductName(),
                item.getMainImage(),
                item.getPrice(),
                item.getStock(),
                item.getSales(),
                item.getStatus(),
                categoryMap.getOrDefault(item.getCategoryId(), ""),
                merchantNameMap.getOrDefault(item.getMerchantId(), ""),
                item.getCreateTime()
            ))
            .toList();
    }

    private List<CustomerProductItem> mapCustomerProducts(List<ProductEntity> entities) {
        Map<Long, String> merchantNameMap = loadMerchantNameMap(entities);
        return entities.stream()
            .map(item -> new CustomerProductItem(
                item.getId(),
                item.getProductName(),
                item.getMainImage(),
                item.getPrice(),
                item.getOriginalPrice(),
                item.getSales(),
                item.getFlowerLanguage(),
                Objects.equals(item.getSupportCustomize(), 1),
                merchantNameMap.getOrDefault(item.getMerchantId(), "")
            ))
            .toList();
    }

    private Map<Long, String> loadCategoryNameMap(List<ProductEntity> entities) {
        if (entities.isEmpty()) {
            return Map.of();
        }
        return categoryService.listByIds(entities.stream().map(ProductEntity::getCategoryId).distinct().toList())
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item.getCategoryName()), HashMap::putAll);
    }

    private Map<Long, String> loadMerchantNameMap(List<ProductEntity> entities) {
        if (entities.isEmpty()) {
            return Map.of();
        }
        return userService.listByIds(entities.stream().map(ProductEntity::getMerchantId).distinct().toList())
            .stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item.getShopName()), HashMap::putAll);
    }

    private String generateProductNo(Long categoryId) {
        return "FS" + categoryId + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%03d", (int) (Math.random() * 1000));
    }

    private void evictProductCache(Long id) {
        stringRedisTemplate.delete(List.of(RedisKeyConstants.PRODUCT + id, RedisKeyConstants.HOT_PRODUCT));
    }
}
