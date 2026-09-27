<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('adminNotice.title') }}</span><el-button type="primary" color="#E8567F" @click="openDialog('add')">{{ $t('adminNotice.add') }}</el-button></div></template>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" :label="$t('adminNotice.noticeTitle')" min-width="220" show-overflow-tooltip />
        <el-table-column :label="$t('adminNotice.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? $t('adminNotice.published') : $t('adminNotice.draft') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('adminNotice.createTime')" width="180" align="center" />
        <el-table-column :label="$t('adminNotice.actions')" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" plain @click="openDialog('edit', row)">{{ $t('common.edit') }}</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" plain @click="publishNotice(row.id)">{{ $t('adminNotice.publish') }}</el-button>
            <el-popconfirm :title="$t('adminNotice.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" size="small" plain>{{ $t('common.delete') }}</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? $t('adminNotice.addTitle') : $t('adminNotice.editTitle')" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item :label="$t('adminNotice.noticeTitle')" prop="title"><el-input v-model="formData.title" maxlength="100" /></el-form-item>
        <el-form-item :label="$t('adminNotice.content')" prop="content"><el-input v-model="formData.content" type="textarea" :rows="6" maxlength="2000" show-word-limit /></el-form-item>
        <el-form-item :label="$t('adminNotice.status')">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">{{ $t('adminNotice.published') }}</el-radio>
            <el-radio :value="0">{{ $t('adminNotice.draft') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="submitting" @click="submitForm">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
const loading = ref(false);
const tableData = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitting = ref(false);
const formRef = ref<FormInstance>();
const editId = ref<number | null>(null);
const formData = reactive({ title: '', content: '', status: 0 });
const rules = { title: [{ required: true, message: () => t('common.required'), trigger: 'blur' }], content: [{ required: true, message: () => t('common.required'), trigger: 'blur' }] };

const fetchData = async () => {
  loading.value = true;
  try { const res = await request.get('/admin/notices', { timeout: 3000 }); tableData.value = res.data?.records || res.data || []; }
  catch { tableData.value = []; }
  finally { loading.value = false; }
};

const openDialog = (type: 'add' | 'edit', row?: any) => {
  dialogType.value = type; editId.value = null;
  Object.assign(formData, { title: '', content: '', status: 0 });
  if (type === 'edit' && row) { editId.value = row.id; Object.assign(formData, row); }
  dialogVisible.value = true;
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (dialogType.value === 'add') await request.post('/admin/notices', formData);
    else await request.put(`/admin/notices/${editId.value}`, formData);
    ElMessage.success(t('common.success'));
    dialogVisible.value = false;
    await fetchData();
  } finally {
    submitting.value = false;
  }
};

const publishNotice = async (id: number) => {
  await request.patch(`/admin/notices/${id}/status`, { status: 1 });
  ElMessage.success(t('common.success'));
  await fetchData();
};

const handleDelete = async (id: number) => {
  await request.delete(`/admin/notices/${id}`);
  ElMessage.success(t('common.success'));
  await fetchData();
};

onMounted(() => { fetchData(); });
</script>

<style scoped>
.admin-page { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; font-weight: 700; }
</style>
