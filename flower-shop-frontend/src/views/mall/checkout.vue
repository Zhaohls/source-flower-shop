<template>
  <div class="checkout-page">
    <h2 class="page-title">{{ $t('checkout.title') }}</h2>

    <el-row :gutter="24">
      <!-- 左侧：收货信息 + 商品清单 -->
      <el-col :span="16">
        <!-- 收货地址 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header">
              <span>{{ $t('checkout.deliveryAddress') }}</span>
              <el-button link type="primary" @click="$router.push('/mall/address')">{{ $t('checkout.manageAddress') }}</el-button>
            </div>
          </template>
          <div v-if="addresses.length" class="address-select">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              :class="['addr-option', { selected: selectedAddressId === addr.id }]"
              @click="selectedAddressId = addr.id"
            >
              <div class="addr-radio">
                <div :class="['radio-dot', { active: selectedAddressId === addr.id }]"></div>
              </div>
              <div class="addr-content">
                <span class="addr-name">{{ addr.receiverName }}</span>
                <span class="addr-phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.isDefault" size="small" type="danger">{{ $t('addressPage.default') }}</el-tag>
                <p class="addr-detail">{{ formatAddress(addr) }}</p>
              </div>
            </div>
          </div>
          <el-empty v-else :description="$t('checkout.noAddress')">
            <el-button type="primary" color="#E8567F" @click="$router.push('/mall/address')">{{ $t('checkout.goAddAddress') }}</el-button>
          </el-empty>
        </el-card>

        <!-- 配送时间 -->
        <el-card shadow="never" class="section-card">
          <template #header><span>{{ $t('checkout.deliveryTime') }}</span></template>
          <el-date-picker
            v-model="deliveryTime"
            type="datetime"
            :placeholder="$t('checkout.selectTime')"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:00"
            style="width: 100%"
          />
        </el-card>

        <!-- 商品清单 -->
        <el-card shadow="never" class="section-card">
          <template #header><span>{{ $t('checkout.orderItems') }}</span></template>
          <div class="order-items">
            <div v-for="item in cartItems" :key="item.id" class="order-item">
              <el-image :src="item.image" style="width: 80px; height: 80px; border-radius: 8px;" fit="cover" />
              <div class="item-info">
                <h4>{{ item.name }}</h4>
                <div class="merchant-name">{{ $t('checkout.shop') }}：{{ item.merchantName || $t('mall.defaultShopName') }}</div>
                <span class="item-price">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</span>
              </div>
              <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 定制选项（简化版） -->
        <el-card shadow="never" class="section-card">
          <template #header><span>{{ $t('checkout.customOptions') }}</span></template>
          <el-form label-width="100px">
            <el-form-item :label="$t('checkout.cardTemplate')">
              <el-select v-model="customization.cardTemplateId" :placeholder="$t('checkout.noCard')" clearable style="width: 100%">
                <el-option v-for="tpl in cardTemplates" :key="tpl.id" :label="tpl.templateName" :value="tpl.id" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('checkout.cardMessage')" v-if="customization.cardTemplateId">
              <el-input v-model="customization.cardContent" type="textarea" :rows="2" maxlength="200" :placeholder="$t('checkout.cardPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('checkout.wrapping')">
              <el-select v-model="customization.wrappingType" :placeholder="$t('checkout.defaultWrap')" clearable style="width: 100%">
                <el-option label="韩式包装" value="korean" />
                <el-option label="牛皮纸包装" value="kraft" />
                <el-option label="精美礼盒" value="giftbox" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('checkout.ribbon')">
              <el-select v-model="customization.ribbonColor" :placeholder="$t('checkout.defaultRibbon')" clearable style="width: 100%">
                <el-option label="粉色" value="粉色" />
                <el-option label="红色" value="红色" />
                <el-option label="白色" value="白色" />
                <el-option label="金色" value="金色" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 备注 -->
        <el-card shadow="never" class="section-card">
          <template #header><span>{{ $t('checkout.remark') }}</span></template>
          <el-input v-model="remark" type="textarea" :rows="2" maxlength="200" :placeholder="$t('checkout.remarkPlaceholder')" />
        </el-card>
      </el-col>

      <!-- 右侧：订单汇总 -->
      <el-col :span="8">
        <div class="summary-card">
          <h3>{{ $t('checkout.orderSummary') }}</h3>
          <div class="summary-row">
            <span>{{ $t('checkout.itemTotal') }}</span>
            <span>¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>{{ $t('checkout.shipping') }}</span>
            <span class="free">{{ $t('checkout.freeShipping') }}</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>{{ $t('checkout.payAmount') }}</span>
            <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            color="#E8567F"
            size="large"
            class="submit-btn"
            :loading="submitting"
            :disabled="!selectedAddressId || cartItems.length === 0"
            @click="submitOrder"
          >
            {{ $t('checkout.submitOrder') }}
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 支付成功对话框 -->
    <el-dialog v-model="payDialogVisible" :title="$t('checkout.payTitle')" width="420px" :close-on-click-modal="false">
      <div class="pay-success">
        <div class="pay-icon" aria-hidden="true">
          <svg viewBox="0 0 96 96" class="pay-icon-svg">
            <defs>
              <linearGradient id="payIconBg" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#ffd8e5" />
                <stop offset="100%" stop-color="#ffe8ef" />
              </linearGradient>
              <linearGradient id="payIconCard" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#E8567F" />
                <stop offset="100%" stop-color="#FF8BA7" />
              </linearGradient>
            </defs>
            <circle cx="48" cy="48" r="42" fill="url(#payIconBg)" />
            <rect x="24" y="25" width="48" height="38" rx="12" fill="url(#payIconCard)" />
            <rect x="30" y="36" width="16" height="6" rx="3" fill="#fff" opacity="0.9" />
            <rect x="30" y="47" width="22" height="6" rx="3" fill="#fff" opacity="0.75" />
            <circle cx="67" cy="64" r="13" fill="#67C23A" />
            <path d="M61 64.5l4.2 4.2 8.3-9" fill="none" stroke="#fff" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <h3>{{ $t('checkout.paySuccess') }}</h3>
        <p>{{ $t('checkout.orderNo') }}：{{ orderNo }}</p>
      </div>
      <template #footer>
        <el-button @click="$router.push('/mall/orders')">{{ $t('checkout.viewOrders') }}</el-button>
        <el-button type="primary" color="#E8567F" @click="$router.push('/mall')">{{ $t('checkout.continueShopping') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useCartStore } from '@/stores/cart';
import request from '@/api/request';
import { formatAddress } from '@/utils/address';

const { t } = useI18n();
const router = useRouter();
const cartStore = useCartStore();

type Address = {
  id: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
  isDefault: boolean;
};

type CardTemplate = {
  id: number;
  templateName: string;
  templateImage: string;
  sceneType: string;
};

// 状态
const addresses = ref<Address[]>([]);
const cardTemplates = ref<CardTemplate[]>([]);
const selectedAddressId = ref<number | null>(null);
const deliveryTime = ref('');
const remark = ref('');
const submitting = ref(false);
const payDialogVisible = ref(false);
const orderNo = ref('');

const customization = reactive({
  cardTemplateId: null as number | null,
  cardContent: '',
  wrappingType: '',
  ribbonColor: '',
});

// 从购物车中获取已选商品
const cartItems = computed(() => cartStore.items.filter(i => i.checked));
const totalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0));
const selectedMerchants = computed(() => [...new Set(cartItems.value.map(item => item.merchantId || item.merchantName || t('mall.defaultShopName')))]);

// 获取地址
const fetchAddresses = async () => {
  try {
    const res = await request.get('/customer/addresses', { timeout: 3000 });
    addresses.value = res.data || [];
  } catch {
    addresses.value = [];
  }
  // 自动选择默认地址
  const defaultAddr = addresses.value.find(a => a.isDefault);
  if (defaultAddr) selectedAddressId.value = defaultAddr.id;
  else if (addresses.value.length) selectedAddressId.value = addresses.value[0].id;
};

// 获取贺卡模板
const fetchCardTemplates = async () => {
  try {
    const res = await request.get('/customer/card-templates', { timeout: 3000 });
    if (res.data?.length) cardTemplates.value = res.data;
  } catch {
    cardTemplates.value = [];
  }
};

// 提交订单
const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning(t('checkout.selectAddress'));
    return;
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning(t('checkout.noItems'));
    return;
  }
  if (selectedMerchants.value.length > 1) {
    ElMessage.warning(t('cartPage.multiMerchantTip'));
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      addressId: selectedAddressId.value,
      deliveryTime: deliveryTime.value || undefined,
      remark: remark.value || undefined,
      items: cartItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        customize: customization.cardTemplateId ? {
          cardTemplateId: customization.cardTemplateId,
          cardContent: customization.cardContent || undefined,
          wrappingType: customization.wrappingType || undefined,
          ribbonColor: customization.ribbonColor || undefined,
        } : null,
      })),
    };

    const res = await request.post('/customer/orders/submit', payload);
    orderNo.value = res.data?.orderNo || 'FS' + Date.now();

    // 模拟支付
    await request.post(`/customer/orders/${orderNo.value}/pay`, { payType: 3 });

    // 后端在订单创建成功后会删除已购买的购物车项。
    await cartStore.loadCart();

    payDialogVisible.value = true;
  } catch {
    // 统一错误提示由请求拦截器处理
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  try {
    await cartStore.loadCart();
  } catch {
    return;
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning(t('checkout.noItems'));
    router.push('/mall/cart');
    return;
  }
  await Promise.all([fetchAddresses(), fetchCardTemplates()]);
});
</script>

<style scoped lang="scss">
.checkout-page { min-height: 60vh; }

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0 0 24px;
}

.section-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 地址选择 */
.address-select {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-option {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 2px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
  &.selected { border-color: #E8567F; background: #fff5f7; }
  &:hover { border-color: #ffb3c1; }
}

.radio-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #ddd;
  margin-top: 2px;
  flex-shrink: 0;
  position: relative;
  &.active {
    border-color: #E8567F;
    &::after {
      content: '';
      position: absolute;
      top: 3px; left: 3px;
      width: 8px; height: 8px;
      border-radius: 50%;
      background: #E8567F;
    }
  }
}

.addr-content {
  flex: 1;
  .addr-name { font-weight: 600; margin-right: 8px; }
  .addr-phone { color: #888; font-size: 14px; margin-right: 8px; }
  .addr-detail { margin: 4px 0 0; font-size: 14px; color: #666; }
}

/* 商品清单 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
}

.item-info {
  flex: 1;
  h4 { margin: 0 0 4px; font-size: 15px; color: #333; }
  .merchant-name { font-size: 13px; color: #888; margin-bottom: 4px; }
  .item-price { font-size: 13px; color: #999; }
}

.item-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #E8567F;
}

/* 订单汇总 */
.summary-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 88px;
  h3 { margin: 0 0 20px; font-size: 18px; color: #2f2a33; }
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
  &.total { font-size: 16px; font-weight: 700; color: #333; }
  .free { color: #67C23A; }
}

.total-price {
  font-size: 24px;
  font-weight: 800;
  color: #E8567F;
}

.submit-btn {
  width: 100%;
  margin-top: 20px;
  height: 48px;
  font-size: 16px;
}

/* 支付成功 */
.pay-success {
  text-align: center;
  padding: 20px 0;
  .pay-icon {
    width: 88px;
    height: 88px;
    margin: 0 auto 14px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .pay-icon-svg {
    width: 88px;
    height: 88px;
    display: block;
    filter: drop-shadow(0 10px 22px rgba(232, 86, 127, 0.16));
  }
  h3 { margin: 0 0 8px; color: #67C23A; }
  p { color: #888; font-size: 14px; }
}
</style>
