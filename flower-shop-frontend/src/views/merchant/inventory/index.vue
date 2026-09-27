<template>
  <div class="merchant-inventory">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>{{ $t('merchantInventory.title') }}</span>
          <el-switch v-model="onlyLowStock" :active-text="$t('merchantInventory.onlyLowStock')" @change="fetchData(1)" />
        </div>
      </template>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="productName" :label="$t('merchantInventory.product')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productNo" :label="$t('merchantInventory.productNo')" width="180" show-overflow-tooltip />
        <el-table-column prop="stock" :label="$t('merchantInventory.currentStock')" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock <= (row.stockWarning || 10) }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockWarning" :label="$t('merchantInventory.warning')" width="120" align="center">
          <template #default="{ row }">{{ row.stockWarning || 10 }}</template>
        </el-table-column>
        <el-table-column :label="$t('merchantInventory.statusLabel')" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.stock <= (row.stockWarning || 10)" type="danger" size="small">{{ $t('merchantInventory.lowStock') }}</el-tag>
            <el-tag v-else type="success" size="small">{{ $t('merchantInventory.normal') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantInventory.actions')" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openAdjust(row)">{{ $t('merchantInventory.adjust') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next" :total="pagination.total" :page-size="pagination.pageSize" :current-page="pagination.pageNum" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="adjustDialog.visible" :title="$t('merchantInventory.adjustTitle')" width="460px">
      <el-form label-width="90px">
        <el-form-item :label="$t('merchantInventory.product')">
          <el-input :model-value="adjustDialog.productName" disabled />
        </el-form-item>
        <el-form-item :label="$t('merchantInventory.currentStock')">
          <span class="current-val">{{ adjustDialog.currentStock }}</span>
        </el-form-item>
        <el-form-item :label="$t('merchantInventory.adjustType')">
          <el-radio-group v-model="adjustDialog.type">
            <el-radio value="increase">{{ $t('merchantInventory.increase') }}</el-radio>
            <el-radio value="decrease">{{ $t('merchantInventory.decrease') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('merchantInventory.adjustQty')">
          <el-input-number v-model="adjustDialog.quantity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item :label="$t('merchantInventory.remark')">
          <el-input v-model="adjustDialog.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog.visible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="adjustDialog.loading" @click="submitAdjust">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
const loading = ref(false);
const onlyLowStock = ref(false);
const tableData = ref<any[]>([]);
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });

const adjustDialog = reactive({ visible: false, loading: false, productId: 0, productName: '', currentStock: 0, type: 'increase' as 'increase' | 'decrease', quantity: 1, remark: '' });

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/merchant/inventory', { params: { pageNum, pageSize: pagination.pageSize, lowStock: onlyLowStock.value || undefined }, timeout: 3000 });
    tableData.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch {
    tableData.value = [];
    pagination.total = 0;
  } finally { loading.value = false; }
};

const openAdjust = (row: any) => { Object.assign(adjustDialog, { visible: true, productId: row.id, productName: row.productName, currentStock: row.stock, type: 'increase', quantity: 1, remark: '' }); };

const submitAdjust = async () => {
  adjustDialog.loading = true;
  try {
    await request.post('/merchant/inventory/adjust', { productId: adjustDialog.productId, type: adjustDialog.type, quantity: adjustDialog.quantity, remark: adjustDialog.remark });
    ElMessage.success(t('common.success'));
    adjustDialog.visible = false;
    await fetchData();
  } finally {
    adjustDialog.loading = false;
  }
};

onMounted(() => { fetchData(1); });
</script>

<style scoped>
.merchant-inventory { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.low-stock { color: #f56c6c; font-weight: 700; }
.current-val { font-size: 20px; font-weight: 700; color: #E8567F; }
</style>
