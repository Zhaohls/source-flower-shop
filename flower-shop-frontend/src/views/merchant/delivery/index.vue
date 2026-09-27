<template>
  <div class="merchant-delivery">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('merchantDelivery.title') }}</span></div></template>
      <el-tabs v-model="statusFilter" @tab-change="fetchData(1)">
        <el-tab-pane :label="$t('merchantDelivery.all')" name="all" />
        <el-tab-pane :label="$t('merchantDelivery.pending')" name="0" />
        <el-tab-pane :label="$t('merchantDelivery.inProgress')" name="1" />
        <el-tab-pane :label="$t('merchantDelivery.delivered')" name="2" />
      </el-tabs>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="orderNo" :label="$t('merchantDelivery.orderNo')" width="200" show-overflow-tooltip />
        <el-table-column prop="receiverName" :label="$t('merchantDelivery.receiver')" width="120" />
        <el-table-column prop="receiverAddress" :label="$t('merchantDelivery.address')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="deliveryMan" :label="$t('merchantDelivery.deliveryMan')" width="120" />
        <el-table-column :label="$t('merchantDelivery.status')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.deliveryStatus === 2 ? 'success' : row.deliveryStatus === 1 ? 'warning' : 'info'" size="small">
              {{ row.deliveryStatus === 2 ? $t('merchantDelivery.delivered') : row.deliveryStatus === 1 ? $t('merchantDelivery.inProgress') : $t('merchantDelivery.pending') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantDelivery.actions')" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.deliveryStatus === 0" type="primary" size="small" @click="updateStatus(row.id, 1)">{{ $t('merchantDelivery.startDelivery') }}</el-button>
            <el-button v-if="row.deliveryStatus === 1" type="success" size="small" @click="updateStatus(row.id, 2)">{{ $t('merchantDelivery.markDelivered') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next" :total="pagination.total" :page-size="pagination.pageSize" :current-page="pagination.pageNum" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
const loading = ref(false);
const statusFilter = ref('all');
const tableData = ref<any[]>([]);
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/merchant/delivery', { params: { pageNum, pageSize: pagination.pageSize, status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value) }, timeout: 3000 });
    tableData.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch {
    tableData.value = [];
    pagination.total = 0;
  } finally { loading.value = false; }
};

const updateStatus = async (id: number, status: number) => {
  await request.patch(`/merchant/delivery/${id}/status`, { deliveryStatus: status });
  ElMessage.success(t('common.success'));
  await fetchData();
};

onMounted(() => { fetchData(1); });
</script>

<style scoped>
.merchant-delivery { padding: 20px; }
.header-row { font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
