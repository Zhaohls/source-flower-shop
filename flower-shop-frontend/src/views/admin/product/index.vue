<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>商品管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索商品名称或编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" @click="fetchData(1)">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="主图" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage || productPlaceholder"
              class="thumb-image"
              fit="cover"
              @click="openPreview(row.mainImage || productPlaceholder)"
            >
              <template #error>
                <img :src="productPlaceholder" class="thumb-fallback" />
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="productNo" label="商品编号" width="180" show-overflow-tooltip />
        <el-table-column prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商家" width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="price" label="售价" width="100" align="center" />
        <el-table-column prop="stock" label="库存" width="90" align="center" />
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
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除这个商品吗？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="previewVisible" title="商品主图" width="560px" append-to-body destroy-on-close>
      <img :src="previewImage" alt="商品主图" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/api/request';
import productPlaceholder from '@/assets/product-placeholder.svg';

type AdminProductRow = {
  id: number;
  productNo: string;
  productName: string;
  mainImage: string;
  price: number;
  stock: number;
  sales: number;
  status: number;
  categoryName: string;
  merchantName: string;
  createTime: string;
};

const loading = ref(false);
const tableData = ref<AdminProductRow[]>([]);
const previewVisible = ref(false);
const previewImage = ref(productPlaceholder);

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
    const res = await request.get('/admin/products', {
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

const handleStatusChange = async (row: AdminProductRow, value: string | number | boolean) => {
  const nextStatus = Number(value);
  const previousStatus = row.status === 1 ? 0 : 1;
  row.status = nextStatus;
  try {
    await request.patch(`/admin/products/${row.id}/status`, { status: nextStatus });
    ElMessage.success(nextStatus === 1 ? '商品已上架' : '商品已下架');
  } catch (error) {
    row.status = previousStatus;
    throw error;
  }
};

const handleDelete = async (id: number) => {
  await request.delete(`/admin/products/${id}`);
  ElMessage.success('商品已删除');
  await fetchData(tableData.value.length === 1 && pagination.pageNum > 1 ? pagination.pageNum - 1 : pagination.pageNum);
};

const openPreview = (imageUrl: string) => {
  previewImage.value = imageUrl || productPlaceholder;
  previewVisible.value = true;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.thumb-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  cursor: pointer;
}

.thumb-fallback {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
}

.preview-image {
  width: 100%;
  max-height: 70vh;
  object-fit: contain;
  display: block;
  border-radius: 12px;
  background: #f7f8fb;
}
</style>
