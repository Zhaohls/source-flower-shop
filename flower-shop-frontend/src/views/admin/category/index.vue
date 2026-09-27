<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('adminCategory.title') }}</span><el-button type="primary" color="#E8567F" @click="openDialog('add')">{{ $t('adminCategory.add') }}</el-button></div></template>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="categoryName" :label="$t('adminCategory.name')" min-width="160" />
        <el-table-column prop="icon" :label="$t('adminCategory.icon')" width="100" align="center" />
        <el-table-column prop="sort" :label="$t('adminCategory.sort')" width="100" align="center" />
        <el-table-column :label="$t('adminCategory.status')" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row, $event)" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminCategory.actions')" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" plain @click="openDialog('edit', row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('adminCategory.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" size="small" plain>{{ $t('common.delete') }}</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? $t('adminCategory.addTitle') : $t('adminCategory.editTitle')" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item :label="$t('adminCategory.name')" prop="categoryName"><el-input v-model="formData.categoryName" maxlength="20" /></el-form-item>
        <el-form-item :label="$t('adminCategory.icon')"><el-input v-model="formData.icon" maxlength="10" :placeholder="$t('adminCategory.iconTip')" /></el-form-item>
        <el-form-item :label="$t('adminCategory.sort')"><el-input-number v-model="formData.sort" :min="0" :max="999" /></el-form-item>
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
const formData = reactive({ categoryName: '', icon: '🌸', sort: 0 });
const rules = { categoryName: [{ required: true, message: () => t('common.required'), trigger: 'blur' }] };

const fetchData = async () => {
  loading.value = true;
  try { const res = await request.get('/admin/categories', { timeout: 3000 }); tableData.value = res.data || []; }
  catch { tableData.value = []; }
  finally { loading.value = false; }
};

const openDialog = (type: 'add' | 'edit', row?: any) => {
  dialogType.value = type;
  editId.value = null;
  Object.assign(formData, { categoryName: '', icon: '🌸', sort: 0 });
  if (type === 'edit' && row) { editId.value = row.id; Object.assign(formData, row); }
  dialogVisible.value = true;
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (dialogType.value === 'add') await request.post('/admin/categories', formData);
    else await request.put(`/admin/categories/${editId.value}`, formData);
    ElMessage.success(t('common.success'));
    dialogVisible.value = false;
    await fetchData();
  } finally {
    submitting.value = false;
  }
};

const handleStatusChange = async (row: any, val: string | number | boolean) => {
  const previousStatus = Number(val) === 1 ? 0 : 1;
  try {
    await request.patch(`/admin/categories/${row.id}/status`, { status: Number(val) });
    ElMessage.success(t('common.success'));
  } catch {
    row.status = previousStatus;
  }
};

const handleDelete = async (id: number) => {
  await request.delete(`/admin/categories/${id}`);
  ElMessage.success(t('common.success'));
  await fetchData();
};

onMounted(() => { fetchData(); });
</script>

<style scoped>
.admin-page { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; font-weight: 700; }
</style>
