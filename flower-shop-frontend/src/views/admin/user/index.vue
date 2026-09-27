<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>用户管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索用户名或手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" @click="fetchData(1)">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="phone" label="手机号" width="160" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除这个用户吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" plain size="small">删除</el-button>
              </template>
            </el-popconfirm>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/api/request';

type UserRow = {
  id: number;
  username: string;
  nickname: string;
  role: string;
  phone: string;
  status: number;
  createTime: string;
};

const loading = ref(false);
const tableData = ref<UserRow[]>([]);

const searchForm = reactive<{ keyword: string; status?: number }>({
  keyword: '',
  status: undefined
});

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/admin/users', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword.trim() || undefined,
        status: searchForm.status
      }
    });
    tableData.value = res.data.records;
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const handleStatusChange = async (row: UserRow, value: string | number | boolean) => {
  const nextStatus = Number(value);
  const previousStatus = row.status === 1 ? 0 : 1;
  row.status = nextStatus;
  try {
    await request.patch(`/admin/users/${row.id}/status`, { status: nextStatus });
    ElMessage.success(nextStatus === 1 ? '用户已启用' : '用户已禁用');
  } catch (error) {
    row.status = previousStatus;
    throw error;
  }
};

const handleDelete = async (id: number) => {
  await request.delete(`/admin/users/${id}`);
  ElMessage.success('用户已删除');
  await fetchData(tableData.value.length === 1 && pagination.pageNum > 1 ? pagination.pageNum - 1 : pagination.pageNum);
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
