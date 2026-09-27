<template>
  <div class="mall-home">
    <!-- 轮播 Banner -->
    <div class="banner-section">
      <el-carousel height="420px" :interval="4000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="(banner, index) in displayBanners" :key="index">
          <img :src="banner.image" class="banner-img" :alt="banner.alt" />
          <div class="banner-overlay">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.subtitle }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 公告通知 -->
    <el-alert
      v-if="noticeText"
      :title="noticeText"
      type="info"
      show-icon
      :closable="false"
      class="notice-bar"
    />

    <!-- 分类导航 -->
    <section class="categories-section" v-if="displayCategories.length">
      <h3 class="section-title">商品分类</h3>
      <div class="categories-grid">
        <div class="cat-card" v-for="(cat, index) in displayCategories" :key="cat.id" @click="router.push({ path: '/mall/products', query: { categoryId: cat.id } })">
          <div class="cat-icon" :style="{ backgroundColor: categoryColors[index % categoryColors.length] }">
            <span>{{ cat.categoryName.slice(0, 1) }}</span>
          </div>
          <div class="cat-name">{{ cat.categoryName }}</div>
        </div>
      </div>
    </section>

    <!-- 畅销推荐 -->
    <section class="products-section">
      <div class="section-header">
        <h3 class="section-title">{{ $t('mall.bestsellers') }}</h3>
        <el-button text type="primary" class="view-all-btn" @click="router.push('/mall/products')">查看全部 →</el-button>
      </div>

      <div v-if="displayProducts.length" class="product-grid">
        <div class="product-card" v-for="product in displayProducts" :key="product.id" @click="router.push(`/mall/products/${product.id}`)">
          <div class="product-img-wrapper">
            <img
              :src="product.mainImage"
              @error="(event) => setImageFallback(event)"
              class="product-img"
            />
            <div class="product-badge" v-if="product.sales > 50">热销</div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.productName }}</h4>
            <p class="flower-language">{{ product.flowerLanguage || $t('mall.defaultFlowerLang') }}</p>
            <div class="product-bottom">
              <div class="price-area">
                <span class="price">¥{{ Number(product.price).toFixed(2) }}</span>
                <span class="original-price" v-if="product.originalPrice">¥{{ Number(product.originalPrice).toFixed(2) }}</span>
              </div>
              <el-button type="primary" color="#E8567F" size="small" circle @click="addToCart(product)">
                <el-icon>
                  <svg viewBox="0 0 1024 1024" width="14" height="14" fill="currentColor">
                    <path d="M922.9 335.2H324.3L292.6 162c-2.8-15.2-16.1-26.2-31.6-26.2H120.6c-17.7 0-32 14.3-32 32s14.3 32 32 32h114.5l31.7 173.2L331 684.4c2.8 15.2 16.1 26.2 31.6 26.2h443.8c13.3 0 25.3-8.3 29.9-20.9l110.6-316.4c7.9-22.5-8.5-38.1-24-38.1zM793.2 646.6H389.4L335 399.2h548.4L793.2 646.6z"/>
                    <path d="M416 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/>
                    <path d="M736 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/>
                  </svg>
                </el-icon>
              </el-button>
            </div>
            <div class="shop-name">{{ product.merchantName || $t('mall.defaultShopName') }}</div>
          </div>
        </div>
      </div>

      <el-empty v-else :description="$t('mall.noProducts')" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import productPlaceholder from '@/assets/product-placeholder.svg';
import request from '@/api/request';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';

const { t } = useI18n();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();

// ====== 响应式数据 ======
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
  categoryIcon: string;
};

type HomeResponse = {
  banners: { id: number; imageUrl: string; linkUrl: string; title: string }[];
  categories: Category[];
  hotProducts: Product[];
  newProducts: Product[];
  notices: { id: number; title: string; createTime: string }[];
};

const categoryColors = ['#E8567F', '#FF8BA7', '#ffb3a7', '#f6c445', '#67C23A', '#409EFF'];
const homeData = ref<HomeResponse | null>(null);
const displayBanners = computed(() => {
  return (homeData.value?.banners || []).map((banner) => ({
    image: banner.imageUrl,
    alt: banner.title || '轮播图',
    title: banner.title || '花店推荐',
    subtitle: '为重要时刻，挑一束刚刚好的花。'
  }));
});

const displayCategories = computed(() => {
  return homeData.value?.categories || [];
});

const displayProducts = computed(() => {
  if (homeData.value) {
    return homeData.value.hotProducts.length ? homeData.value.hotProducts : homeData.value.newProducts;
  }
  return [];
});

const noticeText = computed(() => homeData.value?.notices?.map(n => n.title).join('   ·   ') || '');

const setImageFallback = (event: Event) => {
  const target = event.target as HTMLImageElement | null;
  if (target) target.src = productPlaceholder;
};

// 带超时的 API 请求，不阻塞页面渲染
const fetchHomeData = async () => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000); // 3秒超时
    const res = await request.get('/customer/home', { signal: controller.signal });
    clearTimeout(timeoutId);
    homeData.value = res.data;
  } catch {
    homeData.value = null;
  }
};

const ensureCustomerForPurchase = () => {
  const currentRole = userStore.userInfo?.role || userStore.roles[0];
  if (!userStore.token) {
    ElMessage.warning('请先登录后再购买');
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
    return false;
  }
  if (currentRole !== 'CUSTOMER') {
    ElMessage.warning('请使用买家账号进行购买');
    return false;
  }
  return true;
};

const addToCart = async (product: Product) => {
  if (!ensureCustomerForPurchase()) return;
  try {
    await cartStore.addItem(product.id, 1);
    ElMessage.success(t('mall.addedToCart'));
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

onMounted(() => {
  fetchHomeData();
});
</script>

<style scoped lang="scss">
.mall-home {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* ====== 轮播 ====== */
.banner-section {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

  :deep(.el-carousel__indicators--outside) {
    .el-carousel__indicator .el-carousel__button {
      background-color: #E8567F;
    }
  }
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 48px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  color: #fff;

  h2 {
    margin: 0 0 8px;
    font-size: 2rem;
    font-weight: 700;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  p {
    margin: 0;
    font-size: 1.1rem;
    opacity: 0.9;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  }
}

/* ====== 公告 ====== */
.notice-bar {
  border-radius: 10px;
}

/* ====== 分类导航 ====== */
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0 0 20px;
  position: relative;
  padding-left: 14px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 4px;
    width: 4px;
    height: 22px;
    border-radius: 2px;
    background: linear-gradient(180deg, #E8567F, #FF8BA7);
  }
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 16px;
}

.cat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(232, 86, 127, 0.15);
  }
}

.cat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.cat-name {
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

/* ====== 商品网格 ====== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .section-title {
    margin-bottom: 0;
  }
}

.view-all-btn {
  color: #E8567F !important;
  font-weight: 500;
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

    .product-img {
      transform: scale(1.05);
    }
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
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.flower-language {
  margin: 0 0 12px;
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.shop-name {
  margin-top: 8px;
  font-size: 12px;
  color: #bbb;
}
</style>
