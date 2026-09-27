<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('adminSystem.title') }}</span></div></template>
      <el-tabs v-model="activeTab">
        <!-- 操作日志 -->
        <el-tab-pane :label="$t('adminSystem.operationLogs')" name="logs">
          <div class="log-actions" style="margin-bottom: 12px;">
            <el-button type="danger" size="small" plain @click="clearLogs">{{ $t('adminSystem.clearLogs') }}</el-button>
          </div>
          <el-table :data="logs" border v-loading="logsLoading">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="module" :label="$t('adminSystem.module')" width="120" />
            <el-table-column prop="action" :label="$t('adminSystem.action')" min-width="200" show-overflow-tooltip />
            <el-table-column prop="operator" :label="$t('adminSystem.operator')" width="120" />
            <el-table-column prop="ip" :label="$t('adminSystem.ip')" width="140" />
            <el-table-column prop="createTime" :label="$t('adminSystem.time')" width="180" align="center" />
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination background layout="total, prev, pager, next" :total="logsPagination.total" :page-size="logsPagination.pageSize" :current-page="logsPagination.pageNum" @current-change="fetchLogs" />
          </div>
        </el-tab-pane>

        <!-- 字典管理 -->
        <el-tab-pane :label="$t('adminSystem.dictManagement')" name="dict">
          <el-table :data="dictTypes" border v-loading="dictLoading">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="dictName" :label="$t('adminSystem.dictName')" min-width="160" />
            <el-table-column prop="dictType" :label="$t('adminSystem.dictType')" width="200" />
            <el-table-column prop="remark" :label="$t('adminSystem.remark')" min-width="200" show-overflow-tooltip />
            <el-table-column :label="$t('adminSystem.actions')" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="viewDictData(row)">{{ $t('adminSystem.viewData') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dictDataVisible" :title="`${$t('adminSystem.dictData')} - ${selectedDict?.dictName || ''}`" width="600px">
      <el-table :data="dictDataList" border>
        <el-table-column prop="dictLabel" :label="$t('adminSystem.dictLabel')" min-width="140" />
        <el-table-column prop="dictValue" :label="$t('adminSystem.dictValue')" width="140" />
        <el-table-column prop="sort" :label="$t('adminSystem.sort')" width="80" align="center" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
const activeTab = ref('logs');
const logsLoading = ref(false);
const dictLoading = ref(false);
const logs = ref<any[]>([]);
const logsPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });
const dictTypes = ref<any[]>([]);
const dictDataVisible = ref(false);
const selectedDict = ref<any>(null);
const dictDataList = ref<any[]>([]);

const fetchLogs = async (pageNum = logsPagination.pageNum) => {
  logsPagination.pageNum = pageNum;
  logsLoading.value = true;
  try {
    const res = await request.get('/admin/system/logs', { params: { pageNum, pageSize: logsPagination.pageSize }, timeout: 3000 });
    logs.value = res.data?.records || [];
    logsPagination.total = res.data?.total || 0;
  } catch (error) {
    logs.value = [];
    logsPagination.total = 0;
    ElMessage.error('获取操作日志失败');
  }
  finally { logsLoading.value = false; }
};

const clearLogs = async () => {
  await request.delete('/admin/system/logs');
  ElMessage.success(t('common.success'));
  logs.value = [];
  logsPagination.total = 0;
};

const fetchDictTypes = async () => {
  dictLoading.value = true;
  try {
    const res = await request.get('/admin/system/dict-types', { timeout: 3000 });
    dictTypes.value = res.data || [];
  } catch (error) {
    dictTypes.value = [];
    ElMessage.error('获取字典类型失败');
  }
  finally { dictLoading.value = false; }
};

const viewDictData = async (dict: any) => {
  selectedDict.value = dict;
  dictDataVisible.value = true;
  try {
    const res = await request.get(`/admin/system/dict-types/${dict.id}/data`, { timeout: 3000 });
    dictDataList.value = res.data || [];
  } catch (error) {
    dictDataList.value = [];
    ElMessage.error('获取字典数据失败');
  }
};

onMounted(() => { fetchLogs(1); fetchDictTypes(); });
</script>

<style scoped>
.admin-page { padding: 20px; }
.header-row { font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
