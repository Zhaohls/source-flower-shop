<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>商家管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="待审核" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" @click="fetchData(1)">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="shopName" label="店铺名称" min-width="160" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="160" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="merchantStatusTag(row.status)">{{ merchantStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 2">
              <el-button size="small" type="success" plain @click="openAuditDialog(row, 1)">通过</el-button>
              <el-button size="small" type="danger" plain @click="openAuditDialog(row, 3)">拒绝</el-button>
            </template>
            <template v-else>
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                :disabled="row.status === 3"
                @change="handleStatusChange(row, $event)"
              />
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.pageNum"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="auditDialog.visible" :title="auditDialog.status === 1 ? '审核通过' : '审核拒绝'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="店铺名称">
          <el-input :model-value="auditDialog.shopName" disabled />
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="auditDialog.remark"
            type="textarea"
            :rows="4"
            placeholder="可填写审核备注，留空也可提交"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" color="#E8567F" :loading="auditDialog.loading" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/api/request';

type MerchantRow = {
  id: number;
  username: string;
  nickname: string;
  shopName: string;
  phone: string;
  status: number;
  auditRemark: string;
  createTime: string;
};

const loading = ref(false);
const tableData = ref<MerchantRow[]>([]);

const searchForm = reactive<{ auditStatus?: number }>({
  auditStatus: undefined
});

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const auditDialog = reactive({
  visible: false,
  loading: false,
  id: 0,
  shopName: '',
  status: 1,
  remark: ''
});

const merchantStatusText = (status: number) => {
  if (status === 1) return '正常';
  if (status === 0) return '禁用';
  if (status === 2) return '待审核';
  return '已拒绝';
};

const merchantStatusTag = (status: number) => {
  if (status === 1) return 'success';
  if (status === 0) return 'info';
  if (status === 2) return 'warning';
  return 'danger';
};

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/admin/merchants', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        auditStatus: searchForm.auditStatus
      }
    });
    tableData.value = res.data.records;
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const openAuditDialog = (row: MerchantRow, status: 1 | 3) => {
  auditDialog.visible = true;
  auditDialog.id = row.id;
  auditDialog.shopName = row.shopName;
  auditDialog.status = status;
  auditDialog.remark = '';
};

const submitAudit = async () => {
  auditDialog.loading = true;
  try {
    await request.post(`/admin/merchants/${auditDialog.id}/audit`, {
      auditStatus: auditDialog.status,
      auditRemark: auditDialog.remark.trim() || undefined
    });
    ElMessage.success(auditDialog.status === 1 ? '商家已审核通过' : '商家已拒绝');
    auditDialog.visible = false;
    await fetchData();
  } finally {
    auditDialog.loading = false;
  }
};

const handleStatusChange = async (row: MerchantRow, value: string | number | boolean) => {
  const nextStatus = Number(value);
  const previousStatus = row.status === 1 ? 0 : 1;
  row.status = nextStatus;
  try {
    await request.patch(`/admin/merchants/${row.id}/status`, { status: nextStatus });
    ElMessage.success(nextStatus === 1 ? '商家已启用' : '商家已禁用');
  } catch (error) {
    row.status = previousStatus;
    throw error;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.admin-page {
  padding: 20px;
}

.header-row {
  font-weight: 700;
}

.filter-form {
  padding-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
