<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/mall' }">{{ $t('mall.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/mall/products' }">{{ $t('mall.shop') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.productName }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 商品主体 -->
    <div class="detail-main" v-if="product.id">
      <!-- 左侧图片区 -->
      <div class="gallery-section">
        <div class="main-image-wrapper">
          <el-image
            :src="currentImage"
            :preview-src-list="allImages"
            fit="cover"
            class="main-image"
          >
            <template #error>
              <img :src="productPlaceholder" class="main-image" />
            </template>
          </el-image>
        </div>
        <div class="thumbnail-list" v-if="allImages.length > 1">
          <div
            v-for="(img, index) in allImages"
            :key="index"
            :class="['thumb-item', { active: currentImage === img }]"
            @click="currentImage = img"
          >
            <img :src="img" @error="setFallback" />
          </div>
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="info-section">
        <h1 class="product-title">{{ product.productName }}</h1>
        <p class="flower-language" v-if="product.flowerLanguage">
          <span class="fl-icon">🌸</span> {{ product.flowerLanguage }}
        </p>

        <div class="price-block">
          <div class="price-row">
            <span class="current-price">¥{{ Number(product.price).toFixed(2) }}</span>
            <span class="original-price" v-if="product.originalPrice">¥{{ Number(product.originalPrice).toFixed(2) }}</span>
            <el-tag v-if="product.originalPrice" type="danger" size="small" effect="dark" class="discount-tag">
              {{ $t('productDetail.discountLabel', { percent: Math.round((1 - product.price / product.originalPrice) * 100) }) }}
            </el-tag>
          </div>
          <div class="sales-stock">
            <span>{{ $t('productDetail.sold') }}：{{ product.sales }}</span>
            <span>{{ $t('productDetail.stock') }}：{{ product.stock }}</span>
          </div>
        </div>

        <!-- 花材说明 -->
        <div class="material-block" v-if="product.materialDesc">
          <h4>{{ $t('productDetail.material') }}</h4>
          <p>{{ product.materialDesc }}</p>
        </div>

        <!-- 商家信息 -->
        <div class="merchant-block" v-if="product.merchant">
          <div class="merchant-info">
            <el-avatar :size="36" class="merchant-avatar">{{ (product.merchant.shopName || '?').slice(0, 1) }}</el-avatar>
            <div>
              <div class="merchant-name">{{ product.merchant.shopName }}</div>
              <div class="merchant-label">{{ $t('productDetail.officialShop') }}</div>
            </div>
          </div>
        </div>

        <!-- 定制标签 -->
        <el-tag v-if="product.supportCustomize" type="primary" effect="plain" class="custom-tag">
          ✨ {{ $t('productDetail.supportsCustom') }}
        </el-tag>

        <!-- 数量选择 & 按钮 -->
        <div class="action-block">
          <div class="qty-row">
            <span class="qty-label">{{ $t('productDetail.quantity') }}</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock || 99" />
          </div>
          <div class="btn-row">
            <el-button size="large" color="#E8567F" type="primary" @click="addToCart" :disabled="product.stock === 0">
              {{ product.stock === 0 ? $t('productDetail.outOfStock') : $t('productDetail.addToCart') }}
            </el-button>
            <el-button size="large" type="warning" plain @click="buyNow" :disabled="product.stock === 0">
              {{ $t('productDetail.buyNow') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else :description="$t('productDetail.noDetail')" />

    <!-- 底部Tabs: 详情 | 评价 -->
    <div class="detail-tabs" v-if="product.id">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="$t('productDetail.detailTab')" name="detail">
          <div class="detail-content" v-if="product.detailContent" v-html="product.detailContent"></div>
          <el-empty v-else :description="$t('productDetail.noDetail')" />
        </el-tab-pane>
        <el-tab-pane :label="`${$t('productDetail.reviewTab')}(${reviewSummary.totalCount})`" name="reviews">
          <!-- 评价统计 -->
          <div class="review-summary" v-if="reviewSummary.totalCount">
            <div class="avg-score">
              <span class="score-num">{{ reviewSummary.avgRating }}</span>
              <el-rate :model-value="Number(reviewSummary.avgRating)" disabled show-score text-color="#E8567F" />
            </div>
            <span class="good-rate">{{ $t('productDetail.goodRate') }}：{{ reviewSummary.goodRate }}</span>
          </div>
          <!-- 评价列表 -->
          <div class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="32" class="review-avatar">{{ (review.username || '?').slice(0, 1) }}</el-avatar>
                <div class="review-meta">
                  <span class="review-user">{{ review.username }}</span>
                  <el-rate :model-value="review.rating" disabled size="small" />
                </div>
                <span class="review-time">{{ review.createTime }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
              <div class="review-images" v-if="review.images?.length">
                <el-image
                  v-for="(img, i) in review.images"
                  :key="i"
                  :src="img"
                  :preview-src-list="review.images"
                  fit="cover"
                  class="review-img"
                />
              </div>
              <div class="review-reply" v-if="review.replyContent">
                <strong>{{ $t('productDetail.merchantReply') }}：</strong>{{ review.replyContent }}
              </div>
            </div>
          </div>
          <el-empty v-if="!reviews.length" :description="$t('productDetail.noReviews')" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import productPlaceholder from '@/assets/product-placeholder.svg';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();

// 类型
type ProductDetail = {
  id: number;
  productName: string;
  mainImage: string;
  images: string[];
  price: number;
  originalPrice?: number;
  stock: number;
  sales: number;
  flowerLanguage: string;
  materialDesc: string;
  detailContent: string;
  supportCustomize: boolean;
  categoryName: string;
  merchant: { id: number; shopName: string; shopLogo: string };
  reviewSummary: { avgRating: number; totalCount: number; goodRate: string };
};

type Review = {
  id: number;
  username: string;
  rating: number;
  content: string;
  images: string[];
  replyContent: string;
  createTime: string;
};

// 状态
const loading = ref(false);
const product = ref<ProductDetail>({} as ProductDetail);
const reviews = ref<Review[]>([]);
const currentImage = ref('');
const quantity = ref(1);
const activeTab = ref('detail');

const allImages = computed(() => {
  const imgs: string[] = [];
  if (product.value.mainImage) imgs.push(product.value.mainImage);
  if (product.value.images?.length) imgs.push(...product.value.images.filter(i => i !== product.value.mainImage));
  return imgs.length ? imgs : [productPlaceholder];
});

const reviewSummary = computed(() => product.value.reviewSummary || { avgRating: 0, totalCount: 0, goodRate: '0%' });

const setFallback = (e: Event) => {
  const target = e.target as HTMLImageElement;
  if (target) target.src = productPlaceholder;
};

// 获取商品详情
const fetchProduct = async () => {
  loading.value = true;
  const productId = route.params.id;
  try {
    const res = await request.get(`/customer/products/${productId}`, { timeout: 3000 });
    product.value = res.data;
    currentImage.value = res.data.mainImage || productPlaceholder;
  } catch {
    product.value = {} as ProductDetail;
    currentImage.value = productPlaceholder;
  } finally {
    loading.value = false;
  }
};

// 获取评价列表
const fetchReviews = async () => {
  const productId = route.params.id;
  try {
    const res = await request.get(`/customer/products/${productId}/reviews`, { params: { pageNum: 1, pageSize: 10 }, timeout: 3000 });
    reviews.value = res.data?.records || res.data || [];
  } catch {
    reviews.value = [];
  }
};

const ensureCustomerForPurchase = () => {
  const currentRole = userStore.userInfo?.role || userStore.roles[0];
  if (!userStore.token) {
    ElMessage.warning('请先登录后再购买');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  if (currentRole !== 'CUSTOMER') {
    ElMessage.warning('请使用买家账号进行购买');
    return false;
  }
  return true;
};

const addToCart = async () => {
  if (!ensureCustomerForPurchase()) return;
  try {
    await cartStore.addItem(product.value.id, quantity.value);
    ElMessage.success(t('mall.addedToCart'));
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

const buyNow = async () => {
  if (!ensureCustomerForPurchase()) return;
  try {
    await cartStore.addItem(product.value.id, quantity.value);
    await cartStore.selectOnlyProduct(product.value.id);
    router.push('/mall/checkout');
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

onMounted(() => {
  fetchProduct();
  fetchReviews();
});
</script>

<style scoped lang="scss">
.product-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

/* 图片区 */
.main-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 1 / 1;
  background: #f8f4f5;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.thumb-item {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  &.active, &:hover {
    border-color: #E8567F;
  }
}

/* 信息区 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  line-height: 1.4;
}

.flower-language {
  margin: 0;
  font-size: 15px;
  color: #E8567F;
  background: rgba(232, 86, 127, 0.06);
  padding: 8px 14px;
  border-radius: 8px;
  .fl-icon { margin-right: 4px; }
}

.price-block {
  background: linear-gradient(135deg, #fff5f7, #fff0f3);
  border-radius: 12px;
  padding: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-price {
  font-size: 32px;
  font-weight: 800;
  color: #E8567F;
}

.original-price {
  font-size: 16px;
  color: #bbb;
  text-decoration: line-through;
}

.discount-tag {
  margin-left: 4px;
}

.sales-stock {
  margin-top: 8px;
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #999;
}

.material-block {
  h4 {
    margin: 0 0 6px;
    font-size: 14px;
    color: #666;
  }
  p {
    margin: 0;
    font-size: 14px;
    color: #888;
    line-height: 1.6;
  }
}

.merchant-block {
  .merchant-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #f9f9f9;
    border-radius: 10px;
  }
  .merchant-avatar {
    background: linear-gradient(135deg, #E8567F, #FF8BA7);
    color: #fff;
    font-weight: 600;
  }
  .merchant-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
  }
  .merchant-label {
    font-size: 12px;
    color: #999;
  }
}

.custom-tag {
  align-self: flex-start;
}

.action-block {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 16px;
  .qty-label {
    font-size: 14px;
    color: #666;
    font-weight: 500;
  }
}

.btn-row {
  display: flex;
  gap: 12px;
  .el-button {
    flex: 1;
  }
}

/* 详情Tabs */
.detail-tabs {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #555;
  :deep(p) { margin: 0 0 12px; }
  :deep(img) { max-width: 100%; border-radius: 8px; }
}

/* 评价 */
.review-summary {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 20px;
  background: #fef7f9;
  border-radius: 10px;
  margin-bottom: 20px;
}

.avg-score {
  display: flex;
  align-items: center;
  gap: 8px;
  .score-num {
    font-size: 36px;
    font-weight: 800;
    color: #E8567F;
  }
}

.good-rate {
  font-size: 14px;
  color: #999;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-avatar {
  background: #E8567F;
  color: #fff;
  font-size: 14px;
}

.review-meta {
  flex: 1;
  .review-user {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    display: block;
  }
}

.review-time {
  font-size: 12px;
  color: #bbb;
}

.review-content {
  margin: 0 0 8px;
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.review-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}

.review-reply {
  background: #f8f8f8;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  strong { color: #E8567F; }
}
</style>
