<template>
  <div class="merchant-stats">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>{{ $t('merchantStats.title') }}</span>
          <el-date-picker v-model="dateRange" type="daterange" :start-placeholder="$t('merchantStats.startDate')" :end-placeholder="$t('merchantStats.endDate')" value-format="YYYY-MM-DD" @change="fetchData" style="width: 300px;" />
        </div>
      </template>

      <!-- KPI 卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :span="8" v-for="(kpi, i) in kpis" :key="i">
          <div class="stat-kpi" :style="{ borderColor: kpi.color }">
            <div class="kpi-val" :style="{ color: kpi.color }">{{ kpi.value }}</div>
            <div class="kpi-label">{{ kpi.label }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="hover"><template #header><span>{{ $t('merchantStats.salesTrend') }}</span></template><div ref="lineRef" style="height: 320px;"></div></el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover"><template #header><span>{{ $t('merchantStats.categoryPie') }}</span></template><div ref="pieRef" style="height: 320px;"></div></el-card>
        </el-col>
      </el-row>

      <!-- 热销榜单 -->
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header><span>{{ $t('merchantStats.topProducts') }}</span></template>
        <el-table :data="topProducts" border>
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="productName" :label="$t('merchantStats.productName')" min-width="200" />
          <el-table-column prop="sales" :label="$t('merchantStats.salesCount')" width="120" align="center" />
          <el-table-column prop="revenue" :label="$t('merchantStats.revenue')" width="140" align="center">
            <template #default="{ row }">¥{{ Number(row.revenue).toLocaleString() }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { init, use, graphic, type ECharts } from 'echarts/core';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
use([LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const dateRange = ref<[string, string] | null>(null);
const lineRef = ref<HTMLDivElement | null>(null);
const pieRef = ref<HTMLDivElement | null>(null);
let lineChart: ECharts | null = null;
let pieChart: ECharts | null = null;

const statsData = ref({ totalRevenue: 0, totalOrders: 0, avgOrderAmount: 0, dailyTrend: [] as Array<{ date: string; revenue: number; orders: number }>, categoryBreakdown: [] as Array<{ name: string; value: number }> });

const topProducts = ref<Array<{ productName: string; sales: number; revenue: number }>>([]);

const kpis = computed(() => [
  { label: t('merchantStats.totalRevenue'), value: `¥${statsData.value.totalRevenue.toLocaleString()}`, color: '#E8567F' },
  { label: t('merchantStats.totalOrders'), value: String(statsData.value.totalOrders), color: '#409EFF' },
  { label: t('merchantStats.avgOrder'), value: `¥${statsData.value.avgOrderAmount}`, color: '#67C23A' },
]);

const updateCharts = () => {
  lineChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: statsData.value.dailyTrend.map(d => d.date) },
    yAxis: [{ type: 'value', name: t('merchantStats.revenue') }, { type: 'value', name: t('merchantStats.salesCount') }],
    series: [
      { name: t('merchantStats.revenue'), type: 'line', smooth: true, data: statsData.value.dailyTrend.map(d => d.revenue), itemStyle: { color: '#E8567F' }, areaStyle: { color: new graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(232,86,127,0.3)' }, { offset: 1, color: 'rgba(232,86,127,0.02)' }]) } },
      { name: t('merchantStats.salesCount'), type: 'line', smooth: true, yAxisIndex: 1, data: statsData.value.dailyTrend.map(d => d.orders), itemStyle: { color: '#409EFF' } },
    ],
  });
  pieChart?.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['40%', '70%'], data: statsData.value.categoryBreakdown, label: { show: true, formatter: '{b}: {c}' }, emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } } }],
    color: ['#E8567F', '#FF8BA7', '#409EFF', '#67C23A', '#f6c445'],
  });
};

const fetchData = async () => {
  try {
    const params: any = {};
    if (dateRange.value) { params.startDate = dateRange.value[0]; params.endDate = dateRange.value[1]; }
    const res = await request.get('/merchant/statistics/sales', { params, timeout: 3000 });
    if (res.data) statsData.value = res.data;
    if (res.data?.topProducts) topProducts.value = res.data.topProducts;
  } catch {
    statsData.value = { totalRevenue: 0, totalOrders: 0, avgOrderAmount: 0, dailyTrend: [], categoryBreakdown: [] };
    topProducts.value = [];
  }
  updateCharts();
};

const handleResize = () => { lineChart?.resize(); pieChart?.resize(); };

onMounted(() => {
  if (lineRef.value) lineChart = init(lineRef.value);
  if (pieRef.value) pieChart = init(pieRef.value);
  fetchData();
  window.addEventListener('resize', handleResize);
});
onUnmounted(() => { window.removeEventListener('resize', handleResize); lineChart?.dispose(); pieChart?.dispose(); });
</script>

<style scoped lang="scss">
.merchant-stats { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; font-weight: 700; }
.stat-kpi { background: #fff; border-left: 4px solid; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.kpi-val { font-size: 28px; font-weight: 800; }
.kpi-label { font-size: 13px; color: #909399; margin-top: 6px; }
</style>
