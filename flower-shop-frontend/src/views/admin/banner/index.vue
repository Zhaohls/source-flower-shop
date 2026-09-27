<template>
  <div class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>{{ $t('adminBanner.title') }}</span>
          <el-button type="primary" color="#E8567F" @click="openDialog('add')">
            {{ $t('adminBanner.add') }}
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column :label="$t('adminBanner.image')" width="180" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              fit="cover"
              class="banner-thumb"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" :label="$t('adminBanner.bannerTitle')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="linkUrl" :label="$t('adminBanner.link')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sort" :label="$t('adminBanner.sort')" width="90" align="center" />
        <el-table-column :label="$t('adminBanner.status')" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminBanner.actions')" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" plain @click="openDialog('edit', row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('adminBanner.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" plain>{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? $t('adminBanner.addTitle') : $t('adminBanner.editTitle')"
      width="620px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item :label="$t('adminBanner.bannerTitle')" prop="title">
          <el-input v-model="formData.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('adminBanner.image')" prop="imageUrl">
          <el-upload
            v-model:file-list="imageFileList"
            class="banner-uploader"
            list-type="picture-card"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{ type: 'banner' }"
            :limit="1"
            :show-file-list="true"
            accept="image/*"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :on-exceed="handleUploadExceed"
          >
            <el-icon>
              <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
                <path d="M482 152h60v720h-60z" />
                <path d="M152 482h720v60H152z" />
              </svg>
            </el-icon>
          </el-upload>
          <div class="upload-tip">{{ $t('adminBanner.uploadTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('adminBanner.link')">
          <el-input v-model="formData.linkUrl" :placeholder="$t('adminBanner.linkPh')" />
        </el-form-item>
        <el-form-item :label="$t('adminBanner.sort')">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('adminBanner.status')">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="submitting" @click="submitForm">
          {{ $t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, UploadProps, UploadRawFile, UploadUserFile } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useUserStore } from '@/stores/user';
import request from '@/api/request';

const { t } = useI18n();
const userStore = useUserStore();

type BannerRow = {
  id: number;
  title: string;
  imageUrl: string;
  linkUrl: string;
  sort: number;
  status: number;
};

type BannerForm = {
  title: string;
  imageUrl: string;
  linkUrl: string;
  sort: number;
  status: number;
};

const loading = ref(false);
const tableData = ref<BannerRow[]>([]);
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitting = ref(false);
const formRef = ref<FormInstance>();
const editId = ref<number | null>(null);
const imageFileList = ref<UploadUserFile[]>([]);

const uploadUrl = `${import.meta.env.DEV ? '/api' : import.meta.env.VITE_API_BASE_URL || '/api'}/common/upload`;
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}));

const createEmptyForm = (): BannerForm => ({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
});

const formData = reactive<BannerForm>(createEmptyForm());

const rules = {
  title: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  imageUrl: [{ required: true, message: () => t('common.required'), trigger: 'change' }]
};

const buildUploadFile = (url: string, name = 'banner'): UploadUserFile => ({
  name,
  url,
  status: 'success'
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/admin/banners');
    tableData.value = res.data.records || [];
  } finally {
    loading.value = false;
  }
};

const openDialog = (type: 'add' | 'edit', row?: BannerRow) => {
  dialogType.value = type;
  editId.value = row?.id ?? null;
  Object.assign(formData, createEmptyForm());
  imageFileList.value = [];

  if (type === 'edit' && row) {
    Object.assign(formData, row);
    if (row.imageUrl) {
      imageFileList.value = [buildUploadFile(row.imageUrl, row.title)];
    }
  }

  dialogVisible.value = true;
};

const beforeUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
};

const handleUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  const fileUrl = (response as { data?: { fileUrl?: string } })?.data?.fileUrl || '';
  if (!fileUrl) {
    ElMessage.error(t('common.failed'));
    imageFileList.value = [];
    formData.imageUrl = '';
    return;
  }

  formData.imageUrl = fileUrl;
  imageFileList.value = [buildUploadFile(fileUrl, uploadFile.name)];
  formRef.value?.validateField('imageUrl');
};

const handleUploadRemove: UploadProps['onRemove'] = () => {
  formData.imageUrl = '';
  imageFileList.value = [];
};

const handleUploadExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('只能上传一张轮播图');
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      title: formData.title.trim(),
      imageUrl: formData.imageUrl,
      linkUrl: formData.linkUrl.trim() || undefined,
      sort: formData.sort,
      status: formData.status
    };

    if (dialogType.value === 'add') {
      await request.post('/admin/banners', payload);
    } else {
      await request.put(`/admin/banners/${editId.value}`, payload);
    }
    ElMessage.success(t('common.success'));
    dialogVisible.value = false;
    await fetchData();
  } finally {
    submitting.value = false;
  }
};

const handleStatusChange = async (row: BannerRow, value: string | number | boolean) => {
  const nextStatus = Number(value);
  const previousStatus = row.status === 1 ? 0 : 1;
  row.status = nextStatus;

  try {
    await request.patch(`/admin/banners/${row.id}/status`, { status: nextStatus });
    ElMessage.success(t('common.success'));
  } catch (error) {
    row.status = previousStatus;
    throw error;
  }
};

const handleDelete = async (id: number) => {
  await request.delete(`/admin/banners/${id}`);
  ElMessage.success(t('common.success'));
  await fetchData();
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
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
}

.banner-thumb {
  width: 120px;
  height: 48px;
  border-radius: 8px;
}

.upload-tip {
  width: 100%;
  margin-top: 8px;
  color: #999;
  font-size: 12px;
  line-height: 1.6;
}

.banner-uploader :deep(.el-upload--picture-card),
.banner-uploader :deep(.el-upload-list__item) {
  width: 240px;
  height: 120px;
  border-radius: 12px;
}
</style>
