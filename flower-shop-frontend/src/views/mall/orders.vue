<template>
  <div class="orders-page">
    <h2 class="page-title">{{ $t('ordersPage.title') }}</h2>

    <!-- 状态筛选 -->
    <el-tabs v-model="statusFilter" @tab-change="fetchOrders(1)">
      <el-tab-pane :label="$t('ordersPage.all')" name="all" />
      <el-tab-pane :label="$t('ordersPage.pendingPay')" name="0" />
      <el-tab-pane :label="$t('ordersPage.paid')" name="1" />
      <el-tab-pane :label="$t('ordersPage.preparing')" name="2" />
      <el-tab-pane :label="$t('ordersPage.delivering')" name="3" />
      <el-tab-pane :label="$t('ordersPage.completed')" name="4" />
      <el-tab-pane :label="$t('ordersPage.cancelled')" name="5" />
    </el-tabs>

    <!-- 订单列表 -->
    <div v-if="orders.length" class="order-list">
      <div v-for="order in orders" :key="order.orderNo" class="order-card">
        <div class="order-header">
          <div class="order-meta">
            <span class="order-no">{{ $t('ordersPage.orderNo') }}：{{ order.orderNo }}</span>
            <span class="merchant-name">{{ $t('ordersPage.shop') }}：{{ order.merchantName || $t('mall.defaultShopName') }}</span>
          </div>
          <el-tag :type="statusTagType(order.orderStatus)" size="small">{{ order.orderStatusText }}</el-tag>
        </div>
        <div class="order-body">
          <div v-for="item in order.items" :key="item.orderItemId" class="order-product">
            <el-image :src="item.productImage || productPlaceholder" style="width: 64px; height: 64px; border-radius: 8px; cursor: pointer;" fit="cover" @click="goProductDetail(item.productId)">
              <template #error><img :src="productPlaceholder" style="width: 64px; height: 64px; border-radius: 8px; object-fit: cover;" /></template>
            </el-image>
            <div class="product-info">
              <span class="product-name" @click="goProductDetail(item.productId)">{{ item.productName }}</span>
              <span class="product-qty">× {{ item.quantity }}</span>
            </div>
            <span class="product-subtotal">¥{{ Number(item.subtotal).toFixed(2) }}</span>
          </div>
        </div>
        <div class="order-footer">
          <div class="order-total">
            {{ $t('ordersPage.totalPay') }}：<span class="amount">¥{{ Number(order.payAmount).toFixed(2) }}</span>
          </div>
          <div class="order-actions">
            <el-button v-if="order.orderStatus === 0" type="primary" color="#E8567F" size="small" @click="payOrder(order.orderNo)">{{ $t('ordersPage.goPay') }}</el-button>
            <el-button v-if="order.orderStatus === 0" size="small" @click="cancelOrder(order.orderNo)">{{ $t('ordersPage.cancel') }}</el-button>
            <el-button v-if="order.orderStatus === 3" type="success" size="small" @click="confirmReceive(order.orderNo)">{{ $t('ordersPage.confirmReceive') }}</el-button>
            <el-button v-if="order.orderStatus === 4 && canReview(order)" type="warning" size="small" plain @click="openReview(order)">{{ $t('ordersPage.goReview') }}</el-button>
            <el-button size="small" plain @click="viewDetail(order.orderNo)">{{ $t('ordersPage.viewDetail') }}</el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else :description="$t('ordersPage.noOrders')" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
      <el-pagination background layout="prev, pager, next" :total="pagination.total" :page-size="pagination.pageSize" :current-page="pagination.pageNum" @current-change="fetchOrders" />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="$t('ordersPage.orderDetail')" width="700px" destroy-on-close>
      <div v-loading="detailLoading" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('ordersPage.orderNo')">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.statusLabel')">{{ detailData.orderStatusText }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.shop')">{{ detailData.merchantName || $t('mall.defaultShopName') }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.payAmountLabel')">¥{{ Number(detailData.payAmount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.receiver')">{{ detailData.receiverName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.phone')">{{ detailData.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.address')" :span="2">{{ detailData.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.deliveryTime')">{{ detailData.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.remark')" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-items" v-if="detailData.items?.length">
          <div v-for="item in detailData.items" :key="item.orderItemId" class="detail-item">
            <el-image :src="item.productImage || productPlaceholder" style="width: 56px; height: 56px; border-radius: 8px; cursor: pointer;" fit="cover" @click="goProductDetail(item.productId)" />
            <div class="detail-item-info">
              <span class="detail-item-name" @click="goProductDetail(item.productId)">{{ item.productName }}</span>
              <span class="detail-item-meta">¥{{ Number(item.price).toFixed(2) }} × {{ item.quantity }}</span>
            </div>
            <span class="detail-item-subtotal">¥{{ Number(item.subtotal).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 配送信息 -->
        <el-descriptions v-if="detailData.delivery" :column="2" border style="margin-top: 16px;" :title="$t('ordersPage.deliveryInfo')">
          <el-descriptions-item :label="$t('ordersPage.deliveryMan')">{{ detailData.delivery.deliveryMan }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.deliveryPhone')">{{ detailData.delivery.deliveryPhone }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ordersPage.deliveryStatus')">{{ deliveryStatusText(detailData.delivery.deliveryStatus) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewVisible" :title="$t('ordersPage.writeReview')" width="520px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item :label="$t('ordersPage.rating')">
          <el-rate v-model="reviewForm.rating" :colors="['#E8567F', '#E8567F', '#E8567F']" />
        </el-form-item>
        <el-form-item :label="$t('ordersPage.reviewContent')">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit :placeholder="$t('ordersPage.reviewPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="reviewSubmitting" @click="submitReview">{{ $t('ordersPage.submitReview') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import request from '@/api/request';
import productPlaceholder from '@/assets/product-placeholder.svg';

const { t } = useI18n();
const router = useRouter();

type OrderItem = { orderItemId: number; productId: number; productName: string; productImage: string; price: number; quantity: number; subtotal: number; reviewed?: boolean };
type Order = { orderId: number; orderNo: string; orderStatus: number; orderStatusText: string; payAmount: number; merchantName: string; createTime: string; items: OrderItem[] };
type OrderDetail = Order & { receiverName: string; receiverPhone: string; receiverAddress: string; deliveryTime: string; remark: string; delivery?: { deliveryMan: string; deliveryPhone: string; deliveryStatus: number } };

// 状态
const statusFilter = ref('all');
const orders = ref<Order[]>([]);
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailData = ref<OrderDetail | null>(null);
const reviewVisible = ref(false);
const reviewSubmitting = ref(false);
const reviewOrderNo = ref('');
const reviewForm = reactive({ rating: 5, content: '', orderId: 0, orderItemId: 0 });

const statusTagType = (s: number) => { if (s === 4) return 'success'; if (s === 5) return 'info'; if (s === 3) return 'warning'; return 'primary'; };
const deliveryStatusText = (s: number) => { if (s === 2) return '已送达'; if (s === 1) return '配送中'; return '待配送'; };
const canReview = (order: Order) => order.items.some(item => !item.reviewed);

const fetchOrders = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  try {
    const res = await request.get('/customer/orders', {
      params: { pageNum, pageSize: pagination.pageSize, status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value) },
      timeout: 3000,
    });
    orders.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch {
    orders.value = [];
    pagination.total = 0;
  }
};

const payOrder = async (orderNo: string) => {
  try {
    await request.post(`/customer/orders/${orderNo}/pay`, { payType: 3 });
    ElMessage.success(t('common.success'));
    await fetchOrders();
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

const cancelOrder = async (orderNo: string) => {
  try {
    await request.post(`/customer/orders/${orderNo}/cancel`, { reason: '买家主动取消' });
    ElMessage.success(t('common.success'));
    await fetchOrders();
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

const confirmReceive = async (orderNo: string) => {
  try {
    await request.post(`/customer/orders/${orderNo}/confirm-receive`);
    ElMessage.success(t('common.success'));
    await fetchOrders();
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

const viewDetail = async (orderNo: string) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detailData.value = null;
  try {
    const res = await request.get(`/customer/orders/${orderNo}`, { timeout: 3000 });
    detailData.value = res.data;
  } catch {
    detailVisible.value = false;
  } finally {
    detailLoading.value = false;
  }
};

const goProductDetail = (productId: number) => {
  router.push(`/mall/products/${productId}`);
};

const openReview = (order: Order) => {
  reviewOrderNo.value = order.orderNo;
  const targetItem = order.items.find(item => !item.reviewed) || order.items[0];
  if (targetItem) {
    reviewForm.orderId = order.orderId;
    reviewForm.orderItemId = targetItem.orderItemId;
  }
  reviewForm.rating = 5;
  reviewForm.content = '';
  reviewVisible.value = true;
};

const submitReview = async () => {
  reviewSubmitting.value = true;
  try {
    await request.post('/customer/reviews', {
      orderId: reviewForm.orderId,
      orderItemId: reviewForm.orderItemId,
      rating: reviewForm.rating,
      content: reviewForm.content,
    });
    ElMessage.success(t('common.success'));
    reviewVisible.value = false;
    await fetchOrders(1);
  } catch {
    // 请求拦截器已经统一提示错误，这里不再伪造成功态
  } finally {
    reviewSubmitting.value = false;
  }
};

onMounted(() => { fetchOrders(1); });
</script>

<style scoped lang="scss">
.orders-page { min-height: 60vh; }

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0 0 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  .order-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .order-no { font-size: 14px; color: #888; }
  .merchant-name { font-size: 13px; color: #666; }
}

.order-product {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  &:not(:last-child) { border-bottom: 1px solid #f5f5f5; }
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  .product-name { font-size: 15px; color: #333; font-weight: 500; cursor: pointer; transition: color 0.2s ease; }
  .product-name:hover { color: #E8567F; }
  .product-qty { font-size: 13px; color: #999; margin-top: 4px; }
}

.product-subtotal {
  font-size: 15px;
  font-weight: 600;
  color: #E8567F;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.order-total {
  font-size: 14px;
  color: #666;
  .amount { font-size: 18px; font-weight: 700; color: #E8567F; }
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-items {
  margin-top: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
}

.detail-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.detail-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
}

.detail-item-name:hover {
  color: #E8567F;
}

.detail-item-meta {
  font-size: 13px;
  color: #999;
}

.detail-item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #E8567F;
}
</style>
