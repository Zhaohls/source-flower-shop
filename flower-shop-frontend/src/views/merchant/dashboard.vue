<template>
  <div class="merchant-dashboard">
    <!-- KPI 卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(kpi, i) in kpis" :key="i">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-icon" :style="{ background: kpi.bg }">
            <span v-html="kpi.icon"></span>
          </div>
          <div class="kpi-content">
            <div class="kpi-value" :style="{ color: kpi.color }">{{ kpi.value }}</div>
            <div class="kpi-title">{{ kpi.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('merchantDash.weeklyTrend') }}</span>
            </div>
          </template>
          <div ref="lineChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>{{ $t('merchantDash.quickActions') }}</span></template>
          <div class="quick-actions">
            <el-button color="#E8567F" type="primary" @click="$router.push('/merchant/orders')">
              {{ $t('merchantDash.pendingOrders') }}
              <el-badge v-if="workspace.pendingOrders" :value="workspace.pendingOrders" class="action-badge" />
            </el-button>
            <el-button plain @click="$router.push('/merchant/products')">{{ $t('adminLayout.productManagement') }}</el-button>
            <el-button plain @click="$router.push('/merchant/inventory')">{{ $t('merchantDash.lowStock') }}
              <el-badge v-if="workspace.lowStockProducts" :value="workspace.lowStockProducts" class="action-badge" />
            </el-button>
            <el-button plain @click="$router.push('/merchant/reviews')">{{ $t('merchantDash.pendingReviews') }}
              <el-badge v-if="workspace.pendingReviews" :value="workspace.pendingReviews" class="action-badge" />
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { init, use, graphic, type ECharts } from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

type WorkspaceData = {
  todayOrders: number;
  todayRevenue: number;
  pendingOrders: number;
  lowStockProducts: number;
  pendingReviews: number;
  weeklyTrend: { date: string; orders: number; revenue: number }[];
};

const emptyWorkspace = (): WorkspaceData => ({
  todayOrders: 0,
  todayRevenue: 0,
  pendingOrders: 0,
  lowStockProducts: 0,
  pendingReviews: 0,
  weeklyTrend: []
});

const workspace = ref<WorkspaceData>(emptyWorkspace());
const lineChartRef = ref<HTMLDivElement | null>(null);
let lineChart: ECharts | null = null;

const formatAmount = (v: number) => `¥${v.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`;

const kpis = computed(() => [
  { title: t('merchantDash.todayOrders'), value: String(workspace.value.todayOrders), color: '#E8567F', bg: 'rgba(232,86,127,0.1)', icon: '📦' },
  { title: t('merchantDash.todayRevenue'), value: formatAmount(workspace.value.todayRevenue), color: '#FF8BA7', bg: 'rgba(255,139,167,0.1)', icon: '💰' },
  { title: t('merchantDash.pendingProcess'), value: String(workspace.value.pendingOrders), color: '#f6c445', bg: 'rgba(246,196,69,0.1)', icon: '⏳' },
  { title: t('merchantDash.lowStockCount'), value: String(workspace.value.lowStockProducts), color: '#67C23A', bg: 'rgba(103,194,58,0.1)', icon: '📊' },
]);

const updateChart = () => {
  if (!lineChart || !workspace.value.weeklyTrend.length) return;
  lineChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { data: [t('merchantDash.orderCount'), t('merchantDash.revenueAmount')], bottom: 0 },
    xAxis: { type: 'category', data: workspace.value.weeklyTrend.map(d => d.date.slice(5)) },
    yAxis: [{ type: 'value', name: t('merchantDash.orderCount') }, { type: 'value', name: t('merchantDash.revenueAmount') }],
    series: [
      {
        name: t('merchantDash.orderCount'),
        type: 'line',
        smooth: true,
        data: workspace.value.weeklyTrend.map(d => d.orders),
        itemStyle: { color: '#E8567F' },
        areaStyle: { color: new graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(232,86,127,0.4)' }, { offset: 1, color: 'rgba(232,86,127,0.02)' }]) },
      },
      {
        name: t('merchantDash.revenueAmount'),
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: workspace.value.weeklyTrend.map(d => d.revenue),
        itemStyle: { color: '#409EFF' },
        areaStyle: { color: new graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }]) },
      },
    ],
  });
};

const fetchWorkspace = async () => {
  try {
    const res = await request.get('/merchant/workspace', { timeout: 3000 });
    workspace.value = res.data;
  } catch {
    workspace.value = emptyWorkspace();
  }
  updateChart();
};

const handleResize = () => { lineChart?.resize(); };

onMounted(() => {
  if (lineChartRef.value) {
    lineChart = init(lineChartRef.value);
  }
  fetchWorkspace();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  lineChart?.dispose();
});
</script>

<style scoped lang="scss">
.merchant-dashboard { padding: 20px; }

.kpi-card {
  border-radius: 12px;
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.kpi-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.kpi-content {
  .kpi-value { font-size: 24px; font-weight: 800; }
  .kpi-title { font-size: 13px; color: #909399; margin-top: 4px; }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  .el-button { justify-content: flex-start; width: 100%; position: relative; }
}

.action-badge {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
}
</style>
