<template>
  <div class="cart-page" v-loading="cartStore.loading">
    <h2 class="page-title">{{ $t('cartPage.title') }}</h2>

    <!-- 空状态 -->
    <div v-if="cartStore.items.length === 0" class="empty-state">
      <el-empty :description="$t('cartPage.emptyCart')">
        <el-button type="primary" color="#E8567F" @click="$router.push('/mall')">{{ $t('cartPage.goShopping') }}</el-button>
      </el-empty>
    </div>

    <!-- 购物车内容 -->
    <template v-else>
      <el-table :data="cartStore.items" style="width: 100%" class="cart-table">
        <el-table-column width="55" align="center">
          <template #header>
            <el-checkbox :model-value="cartStore.isAllChecked" @change="handleSelectAll" />
          </template>
          <template #default="{ row }">
            <el-checkbox :model-value="row.checked" :disabled="row.invalid" @change="handleItemChecked(row.id, $event)" />
          </template>
        </el-table-column>

        <el-table-column label="商品" min-width="320">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.image" style="width: 80px; height: 80px; border-radius: 8px; cursor: pointer;" fit="cover" @click="goProductDetail(row.productId)" />
              <div class="product-detail">
                <div class="product-name" @click="goProductDetail(row.productId)">{{ row.name }}</div>
                <div class="merchant-name">{{ $t('cartPage.shop') }}：{{ row.merchantName || $t('mall.defaultShopName') }}</div>
                <el-tag v-if="row.invalid" type="danger" size="small">{{ row.invalidReason || '商品当前不可购买' }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="160" align="center">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="Math.max(row.stock, 1)"
              :disabled="row.invalid"
              size="small"
              @change="handleQuantityChange(row.id, $event)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120" align="center">
          <template #default="{ row }">
            <span class="subtotal-text">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemove(row.id)">
              {{ $t('cartPage.deleteBtn') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部结算栏 -->
      <div class="checkout-bar">
        <div class="checkout-info">
          <el-checkbox :model-value="cartStore.isAllChecked" @change="handleSelectAll">{{ $t('cartPage.selectAll') }}</el-checkbox>
          <span class="selected-count">{{ $t('cartPage.selectedTip', { count: cartStore.selectedCount }) }}</span>
        </div>
        <div class="checkout-right">
          <div class="total-price">
            合计：<span class="total-amount">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="primary" color="#E8567F" size="large" :disabled="cartStore.selectedCount === 0" @click="onSubmit">
            {{ $t('cartPage.goCheckout') }}
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const cartStore = useCartStore();
const router = useRouter();

const onSubmit = () => {
  if (cartStore.selectedCount === 0) {
    ElMessage.warning(t('cartPage.selectAtLeast'));
    return;
  }
  const selectedMerchants = [...new Set(cartStore.items
    .filter(item => item.checked && !item.invalid)
    .map(item => item.merchantId || item.merchantName || t('mall.defaultShopName')))];
  if (selectedMerchants.length > 1) {
    ElMessage.warning(t('cartPage.multiMerchantTip'));
    return;
  }
  router.push('/mall/checkout');
};

const handleSelectAll = (checked: string | number | boolean) => {
  void cartStore.setAllChecked(Boolean(checked)).catch(() => undefined);
};

const handleItemChecked = (id: string, checked: string | number | boolean) => {
  void cartStore.setItemChecked(id, Boolean(checked)).catch(() => undefined);
};

const handleQuantityChange = (id: string, quantity?: number) => {
  if (typeof quantity === 'number') {
    void cartStore.updateQuantity(id, quantity).catch(() => undefined);
  }
};

const handleRemove = (id: string) => {
  void cartStore.removeItem(id).catch(() => undefined);
};

const goProductDetail = (productId: number) => {
  router.push(`/mall/products/${productId}`);
};

onMounted(() => {
  void cartStore.loadCart().catch(() => undefined);
});
</script>

<style scoped lang="scss">
.cart-page {
  min-height: 60vh;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0 0 24px;
}

.empty-state {
  padding: 80px 0;
  background: #fff;
  border-radius: 12px;
}

.cart-table {
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table__header th) {
    background: #fafafa;
    font-weight: 600;
    color: #555;
  }
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-detail {
  .product-name {
    font-size: 15px;
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
    cursor: pointer;
    transition: color 0.2s ease;
  }

  .product-name:hover {
    color: #E8567F;
  }

  .merchant-name {
    font-size: 13px;
    color: #888;
    margin-bottom: 4px;
  }
  .product-sku {
    font-size: 13px;
    color: #999;
  }
}

.price-text {
  color: #666;
  font-size: 14px;
}

.subtotal-text {
  color: #E8567F;
  font-weight: 600;
  font-size: 15px;
}

.checkout-bar {
  margin-top: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.checkout-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-count {
  color: #999;
  font-size: 14px;
}

.checkout-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-price {
  font-size: 14px;
  color: #666;

  .total-amount {
    font-size: 24px;
    font-weight: 700;
    color: #E8567F;
  }
}
</style>
