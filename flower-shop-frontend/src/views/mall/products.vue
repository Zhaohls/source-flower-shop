<template>
  <div class="products-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="filters.keyword"
          :placeholder="$t('productList.searchPlaceholder')"
          clearable
          class="search-input"
          @keyup.enter="fetchProducts(1)"
        >
          <template #prefix>
            <el-icon><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M909.6 854.5L649.9 594.8C690.2 542.7 714 478.4 714 408c0-167.4-135.6-303-303-303S108 240.6 108 408s135.6 303 303 303c70.4 0 134.7-23.8 186.8-64.1l259.7 259.7c3.1 3.1 7.2 4.7 11.3 4.7s8.2-1.6 11.3-4.7l29.5-29.5c6.2-6.3 6.2-16.4 0-22.6zM411 676c-147.3 0-267-119.7-267-267S263.7 142 411 142s267 119.7 267 267-119.7 267-267 267z"/></svg></el-icon>
          </template>
        </el-input>
        <el-button type="primary" color="#E8567F" @click="fetchProducts(1)">{{ $t('common.search') }}</el-button>
      </div>
      <div class="filter-right">
        <el-select v-model="filters.sortBy" :placeholder="$t('productList.sortBy')" style="width: 140px" @change="fetchProducts(1)">
          <el-option :label="$t('productList.sortSales')" value="sales" />
          <el-option :label="$t('productList.sortPriceAsc')" value="priceAsc" />
          <el-option :label="$t('productList.sortPriceDesc')" value="priceDesc" />
          <el-option :label="$t('productList.sortNewest')" value="newest" />
        </el-select>
      </div>
    </div>

    <!-- 分类Tabs -->
    <div class="category-tabs">
      <el-radio-group v-model="filters.categoryId" @change="fetchProducts(1)">
        <el-radio-button :value="undefined">{{ $t('productList.allCategories') }}</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.categoryName }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 价格区间 -->
    <div class="price-filter">
      <span class="price-label">{{ $t('productList.priceRange') }}：</span>
      <el-input-number v-model="filters.minPrice" :min="0" :precision="0" :controls="false" :placeholder="$t('productList.minPrice')" size="small" style="width: 100px" />
      <span class="price-sep">—</span>
      <el-input-number v-model="filters.maxPrice" :min="0" :precision="0" :controls="false" :placeholder="$t('productList.maxPrice')" size="small" style="width: 100px" />
      <el-button size="small" @click="fetchProducts(1)">{{ $t('common.confirm') }}</el-button>
    </div>

    <!-- 商品列表 -->
    <div v-if="products.length" class="product-grid">
      <div
        class="product-card"
        v-for="product in products"
        :key="product.id"
        @click="goDetail(product.id)"
      >
        <div class="product-img-wrapper">
          <img :src="product.mainImage" @error="setFallback" class="product-img" />
          <div class="product-badge" v-if="product.sales > 50">{{ $t('productList.hotTag') }}</div>
          <div class="product-badge customize-badge" v-if="product.supportCustomize">{{ $t('productList.customTag') }}</div>
        </div>
        <div class="product-info">
          <h4 class="product-name">{{ product.productName }}</h4>
          <p class="flower-language">{{ product.flowerLanguage || $t('mall.defaultFlowerLang') }}</p>
          <div class="product-bottom">
            <div class="price-area">
              <span class="price">¥{{ Number(product.price).toFixed(2) }}</span>
              <span class="original-price" v-if="product.originalPrice">¥{{ Number(product.originalPrice).toFixed(2) }}</span>
            </div>
            <span class="sales-count">{{ $t('productList.sold') }} {{ product.sales }}</span>
          </div>
          <div class="shop-name">{{ product.merchantName || $t('mall.defaultShopName') }}</div>
        </div>
      </div>
    </div>
    <el-empty v-else :description="$t('mall.noProducts')" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="pagination.total"
        :page-size="pagination.pageSize"
        :current-page="pagination.pageNum"
        @current-change="fetchProducts"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/api/request';
import productPlaceholder from '@/assets/product-placeholder.svg';

const route = useRoute();
const router = useRouter();

// 类型定义
type Product = {
  id: number;
  productName: string;
  mainImage: string;
  price: number;
  originalPrice?: number;
  sales: number;
  flowerLanguage: string;
  supportCustomize: boolean;
  merchantName: string;
};

type Category = {
  id: number;
  categoryName: string;
};

// 响应式状态
const products = ref<Product[]>([]);
const categories = ref<Category[]>([]);
const filters = reactive<{
  keyword: string;
  categoryId?: number;
  sortBy: string;
  minPrice?: number;
  maxPrice?: number;
}>({
  keyword: '',
  categoryId: undefined,
  sortBy: 'sales',
  minPrice: undefined,
  maxPrice: undefined,
});
const pagination = reactive({ pageNum: 1, pageSize: 12, total: 0 });

// 获取商品列表
const fetchProducts = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  try {
    const sortBy = filters.sortBy === 'priceAsc' || filters.sortBy === 'priceDesc' ? 'price' : filters.sortBy === 'newest' ? 'createTime' : 'sales';
    const sortOrder = filters.sortBy === 'priceAsc' ? 'asc' : 'desc';

    const res = await request.get('/customer/products', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        categoryId: filters.categoryId,
        keyword: filters.keyword.trim() || undefined,
        sortBy,
        sortOrder,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
      },
      timeout: 3000,
    });
    products.value = res.data.records || [];
    pagination.total = res.data.total || 0;
  } catch {
    products.value = [];
    pagination.total = 0;
  }
};

// 获取分类
const fetchCategories = async () => {
  try {
    const res = await request.get('/common/categories', { timeout: 3000 });
    if (res.data?.length) categories.value = res.data;
  } catch {
    categories.value = [];
  }
};

const goDetail = (id: number) => {
  router.push(`/mall/products/${id}`);
};

const setFallback = (e: Event) => {
  const target = e.target as HTMLImageElement;
  if (target) target.src = productPlaceholder;
};

// 监听路由参数变化（从首页跳转过来）
watch(() => route.query, (query) => {
  if (query.keyword) filters.keyword = String(query.keyword);
  if (query.categoryId) filters.categoryId = Number(query.categoryId);
  fetchProducts(1);
}, { immediate: false });

onMounted(() => {
  // 从URL参数读取初始筛选条件
  if (route.query.keyword) filters.keyword = String(route.query.keyword);
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId);
  fetchCategories();
  fetchProducts(1);
});
</script>

<style scoped lang="scss">
.products-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.category-tabs {
  :deep(.el-radio-button__inner) {
    border-radius: 20px !important;
    border: none !important;
    box-shadow: none !important;
  }
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background-color: #E8567F;
    color: #fff;
  }
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  .price-label {
    font-size: 14px;
    color: #666;
  }
  .price-sep {
    color: #ccc;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    .product-img { transform: scale(1.05); }
  }
}

.product-img-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 3;
  background: #f8f4f5;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #E8567F, #FF8BA7);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.customize-badge {
  left: auto;
  right: 12px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.product-info {
  padding: 16px;
}

.product-name {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #2f2a33;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.flower-language {
  margin: 0 0 12px;
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-area {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #E8567F;
}

.original-price {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
}

.sales-count {
  font-size: 12px;
  color: #bbb;
}

.shop-name {
  margin-top: 8px;
  font-size: 12px;
  color: #bbb;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}
</style>
