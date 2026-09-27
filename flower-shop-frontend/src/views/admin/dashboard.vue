<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(kpi, index) in kpis" :key="index">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-top">
            <span class="kpi-title">{{ kpi.title }}</span>
            <span class="kpi-chip" :style="{ background: kpi.softColor, color: kpi.color }">{{ kpi.tag }}</span>
          </div>
          <div class="kpi-value" :style="{ color: kpi.color }">{{ kpi.value }}</div>
          <div class="kpi-bottom">{{ kpi.hint }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card chart-card--main">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">{{ $t('admin.salesTrend') }}</div>
                <div class="card-subtitle">{{ $t('admin.trendOverview') }}</div>
              </div>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button value="Week">{{ $t('admin.week') }}</el-radio-button>
                <el-radio-button value="Month">{{ $t('admin.month') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart chart--lg"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">{{ $t('admin.orderStatus') }}</div>
                <div class="card-subtitle">{{ $t('admin.orderStatusSubtitle') }}</div>
              </div>
            </div>
          </template>
          <div ref="statusChartRef" class="chart chart--lg"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">{{ $t('admin.productCategories') }}</div>
                <div class="card-subtitle">{{ $t('admin.categoryDistributionSubtitle') }}</div>
              </div>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">{{ $t('admin.merchantRevenueRank') }}</div>
                <div class="card-subtitle">{{ $t('admin.merchantRevenueSubtitle') }}</div>
              </div>
            </div>
          </template>
          <div ref="merchantChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { init, use, graphic, type ComposeOption, type ECharts } from 'echarts/core';
import { BarChart, LineChart, PieChart, type BarSeriesOption, type LineSeriesOption, type PieSeriesOption } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
  type GridComponentOption,
  type LegendComponentOption,
  type TooltipComponentOption
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();

use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

type ChartOption = ComposeOption<
  BarSeriesOption |
  LineSeriesOption |
  PieSeriesOption |
  GridComponentOption |
  LegendComponentOption |
  TooltipComponentOption
>;

type TrendPoint = {
  date: string;
  orders: number;
  revenue: number;
};

type StatusDistribution = {
  status: string;
  count: number;
};

type CategoryDistribution = {
  categoryName: string;
  productCount: number;
};

type MerchantRevenueRanking = {
  merchantId: number;
  merchantName: string;
  revenue: number;
  orderCount: number;
};

type DashboardData = {
  totalUsers: number;
  totalMerchants: number;
  totalProducts: number;
  todayOrders: number;
  todayRevenue: number;
  monthlyRevenue: number;
  newUsersToday: number;
  orderTrend7Days: TrendPoint[];
  revenueTrend30Days: TrendPoint[];
  orderStatusDistribution: StatusDistribution[];
  categoryDistribution: CategoryDistribution[];
  merchantRevenueRanking: MerchantRevenueRanking[];
};

type TrendMode = 'Week' | 'Month';

const dashboard = ref<DashboardData | null>(null);
const trendType = ref<TrendMode>('Week');

const trendChartRef = ref<HTMLDivElement | null>(null);
const statusChartRef = ref<HTMLDivElement | null>(null);
const categoryChartRef = ref<HTMLDivElement | null>(null);
const merchantChartRef = ref<HTMLDivElement | null>(null);

let trendChart: ECharts | null = null;
let statusChart: ECharts | null = null;
let categoryChart: ECharts | null = null;
let merchantChart: ECharts | null = null;

const palette = ['#E8567F', '#FF8BA7', '#FFB3A7', '#FFCF7D', '#67C23A', '#409EFF', '#7A7CE4'];

const formatAmount = (value: number) =>
  `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const kpis = computed(() => {
  const data = dashboard.value;
  if (!data) {
    return [
      { title: t('admin.totalUsers'), value: '0', hint: `${t('admin.newUsersToday')} 0`, color: '#E8567F', softColor: 'rgba(232, 86, 127, 0.12)', tag: '用户' },
      { title: t('admin.totalMerchants'), value: '0', hint: t('admin.shopCount'), color: '#FF8BA7', softColor: 'rgba(255, 139, 167, 0.12)', tag: '店铺' },
      { title: t('admin.totalProducts'), value: '0', hint: t('admin.inStockProducts'), color: '#67C23A', softColor: 'rgba(103, 194, 58, 0.12)', tag: '商品' },
      { title: t('admin.todayRevenue'), value: formatAmount(0), hint: `${t('admin.todayOrders')} 0 · ${t('admin.monthlyRevenue')} ${formatAmount(0)}`, color: '#909399', softColor: 'rgba(144, 147, 153, 0.12)', tag: '营收' }
    ];
  }

  return [
    { title: t('admin.totalUsers'), value: String(data.totalUsers), hint: `${t('admin.newUsersToday')} ${data.newUsersToday}`, color: '#E8567F', softColor: 'rgba(232, 86, 127, 0.12)', tag: '用户' },
    { title: t('admin.totalMerchants'), value: String(data.totalMerchants), hint: t('admin.shopCount'), color: '#FF8BA7', softColor: 'rgba(255, 139, 167, 0.12)', tag: '店铺' },
    { title: t('admin.totalProducts'), value: String(data.totalProducts), hint: t('admin.inStockProducts'), color: '#67C23A', softColor: 'rgba(103, 194, 58, 0.12)', tag: '商品' },
    {
      title: t('admin.todayRevenue'),
      value: formatAmount(data.todayRevenue),
      hint: `${t('admin.todayOrders')} ${data.todayOrders} · ${t('admin.monthlyRevenue')} ${formatAmount(data.monthlyRevenue)}`,
      color: '#7A7CE4',
      softColor: 'rgba(122, 124, 228, 0.12)',
      tag: '营收'
    }
  ];
});

const updateCharts = () => {
  if (!dashboard.value || !trendChart || !statusChart || !categoryChart || !merchantChart) {
    return;
  }

  const trendPoints = trendType.value === 'Week' ? dashboard.value.orderTrend7Days : dashboard.value.revenueTrend30Days;
  const trendLabels = trendPoints.map((item) => item.date.slice(5));
  const orderSeries = trendPoints.map((item) => Number(item.orders || 0));
  const revenueSeries = trendPoints.map((item) => Number(item.revenue || 0));

  const trendOption: ChartOption = {
    color: ['#FFB3C7', '#E8567F'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      top: 0,
      itemGap: 20,
      textStyle: { color: '#666' }
    },
    grid: {
      top: 52,
      left: 18,
      right: 18,
      bottom: 12,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendLabels,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: t('admin.revenueAmount'),
        axisLabel: {
          formatter: (value: number) => `¥${value}`
        },
        splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)' } }
      },
      {
        type: 'value',
        name: t('admin.orderVolume'),
        minInterval: 1,
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: t('admin.orderVolume'),
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 18,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#FFB3C7'
        },
        data: orderSeries
      },
      {
        name: t('admin.revenueAmount'),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#E8567F' },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(232, 86, 127, 0.35)' },
            { offset: 1, color: 'rgba(232, 86, 127, 0.04)' }
          ])
        },
        data: revenueSeries
      }
    ]
  };

  const statusOption: ChartOption = {
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      left: 'center',
      itemWidth: 12,
      itemHeight: 12
    },
    series: [
      {
        name: t('admin.orderStatus'),
        type: 'pie',
        radius: ['44%', '72%'],
        center: ['50%', '44%'],
        label: {
          formatter: '{b}\n{c}',
          lineHeight: 18
        },
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff',
          borderWidth: 3
        },
        data: dashboard.value.orderStatusDistribution.map((item, index) => ({
          value: item.count,
          name: item.status,
          itemStyle: { color: palette[index % palette.length] }
        }))
      }
    ]
  };

  const categoryOption: ChartOption = {
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      left: 'center'
    },
    series: [
      {
        name: t('admin.categoryProductCount'),
        type: 'pie',
        roseType: 'area',
        radius: [28, 110],
        center: ['50%', '44%'],
        itemStyle: {
          borderRadius: 12
        },
        label: {
          formatter: '{b}\n{c}',
          lineHeight: 18
        },
        data: dashboard.value.categoryDistribution.map((item, index) => ({
          value: item.productCount,
          name: item.categoryName,
          itemStyle: { color: palette[index % palette.length] }
        }))
      }
    ]
  };

  const merchantOption: ChartOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      top: 12,
      left: 18,
      right: 24,
      bottom: 10,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => `¥${value}`
      },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)' } }
    },
    yAxis: {
      type: 'category',
      data: dashboard.value.merchantRevenueRanking.map((item) => item.merchantName),
      axisTick: { show: false }
    },
    series: [
      {
        name: t('admin.revenueAmount'),
        type: 'bar',
        barWidth: 18,
        label: {
          show: true,
          position: 'right',
          formatter: (params: any) => formatAmount(Number(params?.value || 0))
        },
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: new graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#E8567F' },
            { offset: 1, color: '#FFB3C7' }
          ])
        },
        data: dashboard.value.merchantRevenueRanking.map((item) => Number(item.revenue || 0))
      }
    ]
  };

  trendChart.setOption(trendOption, true);
  statusChart.setOption(statusOption, true);
  categoryChart.setOption(categoryOption, true);
  merchantChart.setOption(merchantOption, true);
};

const initCharts = () => {
  if (!trendChartRef.value || !statusChartRef.value || !categoryChartRef.value || !merchantChartRef.value) {
    return;
  }

  const theme = document.documentElement.className.includes('dark') ? 'dark' : 'light';
  trendChart = init(trendChartRef.value, theme);
  statusChart = init(statusChartRef.value, theme);
  categoryChart = init(categoryChartRef.value, theme);
  merchantChart = init(merchantChartRef.value, theme);
  updateCharts();
};

const fetchDashboard = async () => {
  const res = await request.get('/admin/dashboard');
  dashboard.value = res.data;
  updateCharts();
};

const handleResize = () => {
  trendChart?.resize();
  statusChart?.resize();
  categoryChart?.resize();
  merchantChart?.resize();
};

onMounted(() => {
  initCharts();
  fetchDashboard();
  window.addEventListener('resize', handleResize);
});

watch(trendType, () => {
  updateCharts();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  trendChart?.dispose();
  statusChart?.dispose();
  categoryChart?.dispose();
  merchantChart?.dispose();
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-row {
  margin-top: 20px;
}

.kpi-card {
  border: 1px solid rgba(232, 86, 127, 0.08);
  border-radius: 18px;
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.kpi-title {
  color: #909399;
  font-size: 14px;
}

.kpi-chip {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}

.kpi-value {
  font-size: 34px;
  font-weight: 700;
  margin: 14px 0 10px;
  letter-spacing: 0.5px;
}

.kpi-bottom {
  font-size: 13px;
  color: #b0b3be;
  line-height: 1.6;
}

.chart-card {
  border-radius: 18px;
}

.chart-card--main {
  background: linear-gradient(180deg, rgba(255, 247, 250, 0.72), #fff 46%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #b0b3be;
}

.chart {
  height: 320px;
}

.chart--lg {
  height: 360px;
}
</style>
