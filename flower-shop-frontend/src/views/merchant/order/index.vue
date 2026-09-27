<template>
  <div class="merchant-orders">
    <el-card shadow="never">
      <template #header>
        <div class="header-row"><span>{{ $t('merchantOrder.title') }}</span></div>
      </template>

      <!-- 状态筛选 -->
      <el-tabs v-model="statusFilter" @tab-change="fetchOrders(1)">
        <el-tab-pane :label="$t('merchantOrder.all')" name="all" />
        <el-tab-pane :label="$t('merchantOrder.pendingAccept')" name="1" />
        <el-tab-pane :label="$t('merchantOrder.preparing')" name="2" />
        <el-tab-pane :label="$t('merchantOrder.delivering')" name="3" />
        <el-tab-pane :label="$t('merchantOrder.completed')" name="4" />
        <el-tab-pane :label="$t('merchantOrder.cancelled')" name="5" />
      </el-tabs>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="orderNo" :label="$t('merchantOrder.orderNo')" width="200" show-overflow-tooltip />
        <el-table-column :label="$t('merchantOrder.buyer')" width="120">
          <template #default="{ row }">{{ row.receiverName }}</template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrder.products')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.items?.map((i: any) => `${i.productName}×${i.quantity}`).join('，') }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" :label="$t('merchantOrder.amount')" width="110" align="center">
          <template #default="{ row }">¥{{ Number(row.payAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrder.status')" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)" size="small">{{ row.orderStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('merchantOrder.time')" width="170" align="center" />
        <el-table-column :label="$t('merchantOrder.actions')" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.orderStatus === 1" type="success" size="small" @click="acceptOrder(row.orderNo)">{{ $t('merchantOrder.accept') }}</el-button>
            <el-button v-if="row.orderStatus === 1" type="danger" size="small" plain @click="openReject(row.orderNo)">{{ $t('merchantOrder.reject') }}</el-button>
            <el-button v-if="row.orderStatus === 2" type="primary" size="small" @click="openShip(row.orderNo)">{{ $t('merchantOrder.ship') }}</el-button>
            <el-button size="small" plain @click="viewDetail(row.orderNo)">{{ $t('merchantOrder.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next" :total="pagination.total" :page-size="pagination.pageSize" :current-page="pagination.pageNum" @current-change="fetchOrders" />
      </div>
    </el-card>

    <!-- 拒单对话框 -->
    <el-dialog v-model="rejectDialog.visible" :title="$t('merchantOrder.rejectTitle')" width="460px">
      <el-form>
        <el-form-item :label="$t('merchantOrder.rejectReason')">
          <el-input v-model="rejectDialog.reason" type="textarea" :rows="3" :placeholder="$t('merchantOrder.rejectPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog.visible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="danger" :loading="rejectDialog.loading" @click="submitReject">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialog.visible" :title="$t('merchantOrder.shipTitle')" width="460px">
      <el-form label-width="90px">
        <el-form-item :label="$t('merchantOrder.deliveryMan')">
          <el-input v-model="shipDialog.deliveryMan" :placeholder="$t('merchantOrder.deliveryManPh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantOrder.deliveryPhone')">
          <el-input
            v-model="shipDialog.deliveryPhone"
            :placeholder="$t('merchantOrder.deliveryPhonePh')"
            maxlength="11"
            inputmode="numeric"
            @input="handleDeliveryPhoneInput"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog.visible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="shipDialog.loading" @click="submitShip">{{ $t('merchantOrder.confirmShip') }}</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情 -->
    <el-dialog v-model="detailDialog.visible" :title="$t('merchantOrder.detailTitle')" width="700px" destroy-on-close>
      <div v-loading="detailDialog.loading" v-if="detailDialog.data">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('merchantOrder.orderNo')">{{ detailDialog.data.orderNo }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.status')">{{ detailDialog.data.orderStatusText }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.buyer')">{{ detailDialog.data.receiverName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.phone')">{{ detailDialog.data.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.address')" :span="2">{{ detailDialog.data.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.deliveryTime')">{{ detailDialog.data.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('merchantOrder.remark')">{{ detailDialog.data.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detailDialog.data.items" border style="margin-top: 16px;">
          <el-table-column prop="productName" :label="$t('merchantOrder.productName')" min-width="160" />
          <el-table-column prop="price" :label="$t('merchantOrder.unitPrice')" width="100" align="center" />
          <el-table-column prop="quantity" :label="$t('merchantOrder.qty')" width="80" align="center" />
          <el-table-column prop="subtotal" :label="$t('merchantOrder.subtotal')" width="110" align="center" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';
import { isValidPhone, sanitizePhoneInput } from '@/utils/phone';

const { t } = useI18n();

const statusFilter = ref('all');
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });

const rejectDialog = reactive({ visible: false, loading: false, orderNo: '', reason: '' });
const shipDialog = reactive({ visible: false, loading: false, orderNo: '', deliveryMan: '', deliveryPhone: '' });
const detailDialog = reactive({ visible: false, loading: false, data: null as any });

const statusTagType = (s: number) => { if (s === 4) return 'success'; if (s === 5) return 'info'; if (s === 3) return 'warning'; if (s === 1) return 'danger'; return 'primary'; };

const fetchOrders = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/merchant/orders', { params: { pageNum, pageSize: pagination.pageSize, status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value) }, timeout: 3000 });
    tableData.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch {
    tableData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

const acceptOrder = async (orderNo: string) => {
  await request.post(`/merchant/orders/${orderNo}/accept`);
  ElMessage.success(t('common.success'));
  await fetchOrders();
};

const openReject = (orderNo: string) => { rejectDialog.orderNo = orderNo; rejectDialog.reason = ''; rejectDialog.visible = true; };
const submitReject = async () => {
  rejectDialog.loading = true;
  try {
    await request.post(`/merchant/orders/${rejectDialog.orderNo}/reject`, { reason: rejectDialog.reason });
    ElMessage.success(t('common.success'));
    rejectDialog.visible = false;
    await fetchOrders();
  } finally {
    rejectDialog.loading = false;
  }
};

const openShip = (orderNo: string) => { shipDialog.orderNo = orderNo; shipDialog.deliveryMan = ''; shipDialog.deliveryPhone = ''; shipDialog.visible = true; };
const handleDeliveryPhoneInput = () => {
  shipDialog.deliveryPhone = sanitizePhoneInput(shipDialog.deliveryPhone);
};
const submitShip = async () => {
  if (!isValidPhone(shipDialog.deliveryPhone)) {
    ElMessage.warning(t('common.phoneInvalid'));
    return;
  }
  shipDialog.loading = true;
  try {
    await request.post(`/merchant/orders/${shipDialog.orderNo}/ship`, {
      deliveryCompany: '店内配送', deliveryMan: shipDialog.deliveryMan, deliveryPhone: shipDialog.deliveryPhone,
    });
    ElMessage.success(t('common.success'));
    shipDialog.visible = false;
    await fetchOrders();
  } finally {
    shipDialog.loading = false;
  }
};

const viewDetail = async (orderNo: string) => {
  detailDialog.visible = true;
  detailDialog.loading = true;
  detailDialog.data = null;
  try {
    const res = await request.get(`/merchant/orders/${orderNo}`, { timeout: 3000 });
    detailDialog.data = res.data;
  } catch {
    detailDialog.visible = false;
  } finally {
    detailDialog.loading = false;
  }
};

onMounted(() => { fetchOrders(1); });
</script>

<style scoped>
.merchant-orders { padding: 20px; }
.header-row { font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
