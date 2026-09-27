<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" @click="fetchData(1)">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商家" width="140" show-overflow-tooltip />
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column prop="receiverPhone" label="联系电话" width="140" />
        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)">{{ row.orderStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="支付金额" width="110" align="center" />
        <el-table-column prop="createTime" label="下单时间" width="180" align="center" />
        <el-table-column label="商品摘要" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.items.map((item: OrderItem) => `${item.productName} x${item.quantity}`).join('，') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openDetail(row.orderNo)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="780px" destroy-on-close>
      <div v-loading="detailLoading" class="order-detail" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ detailData.orderStatusText }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailData.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailData.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="配送时间">{{ detailData.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ detailData.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">{{ detailData.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ detailData.cancelReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detailData.items" border style="margin-top: 16px;">
          <el-table-column prop="productName" label="商品" min-width="160" />
          <el-table-column prop="price" label="单价" width="100" align="center" />
          <el-table-column prop="quantity" label="数量" width="90" align="center" />
          <el-table-column prop="subtotal" label="小计" width="110" align="center" />
        </el-table>

        <el-descriptions v-if="detailData.delivery" :column="2" border style="margin-top: 16px;">
          <el-descriptions-item label="配送员">{{ detailData.delivery.deliveryMan }}</el-descriptions-item>
          <el-descriptions-item label="配送电话">{{ detailData.delivery.deliveryPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送状态">{{ deliveryStatusText(detailData.delivery.deliveryStatus) }}</el-descriptions-item>
          <el-descriptions-item label="配送备注">{{ detailData.delivery.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import request from '@/api/request';

type OrderItem = {
  orderItemId: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  subtotal: number;
};

type OrderRow = {
  orderNo: string;
  orderStatus: number;
  orderStatusText: string;
  payAmount: number;
  merchantName: string;
  receiverName: string;
  receiverPhone: string;
  createTime: string;
  items: OrderItem[];
};

type OrderDetail = {
  orderNo: string;
  orderStatus: number;
  orderStatusText: string;
  totalAmount: number;
  payAmount: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  deliveryTime: string;
  remark: string;
  cancelReason: string;
  createTime: string;
  items: OrderItem[];
  delivery?: {
    deliveryMan: string;
    deliveryPhone: string;
    deliveryStatus: number;
    remark: string;
  };
};

const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '已支付待接单', value: 1 },
  { label: '备货中', value: 2 },
  { label: '配送中', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
];

const loading = ref(false);
const tableData = ref<OrderRow[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailData = ref<OrderDetail | null>(null);

const searchForm = reactive<{ status?: number }>({
  status: undefined
});

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const statusTagType = (status: number) => {
  if (status === 4) return 'success';
  if (status === 5) return 'info';
  if (status === 3) return 'warning';
  return 'primary';
};

const deliveryStatusText = (status: number) => {
  if (status === 2) return '已送达';
  if (status === 1) return '配送中';
  return '待配送';
};

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/admin/orders', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        status: searchForm.status
      }
    });
    tableData.value = res.data.records;
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const openDetail = async (orderNo: string) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detailData.value = null;
  try {
    const res = await request.get(`/admin/orders/${orderNo}`);
    detailData.value = res.data;
  } finally {
    detailLoading.value = false;
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
