<template>
  <div class="product-management">
    <el-card shadow="never">
      <template #header>
        <div class="header-tools">
          <span>{{ $t('merchantProduct.title') }}</span>
          <el-button type="primary" color="#E8567F" @click="openDialog('add')">
            <el-icon><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M482 152h60v720h-60z"/><path d="M152 482h720v60H152z"/></svg></el-icon>
            {{ $t('merchantProduct.addProduct') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item :label="$t('merchantProduct.keyword')">
          <el-input v-model="searchForm.keyword" :placeholder="$t('merchantProduct.searchPlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="$t('merchantProduct.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('merchantProduct.all')" clearable>
            <el-option :label="$t('merchantProduct.onSale')" :value="1" />
            <el-option :label="$t('merchantProduct.offShelf')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" @click="fetchData(1)">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" :label="$t('merchantProduct.id')" width="80" align="center" />
        <el-table-column :label="$t('merchantProduct.thumbnail')" width="100" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage || productPlaceholder"
              style="width: 50px; height: 50px; border-radius: 4px;"
              :preview-src-list="[row.mainImage || productPlaceholder]"
            >
              <template #error>
                <img :src="productPlaceholder" style="width: 50px; height: 50px; border-radius: 4px; object-fit: cover;" />
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="productName" :label="$t('merchantProduct.productName')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" :label="$t('merchantProduct.category')" width="140" show-overflow-tooltip />
        <el-table-column prop="price" :label="$t('merchantProduct.price')" width="120" sortable align="center" />
        <el-table-column prop="stock" :label="$t('merchantProduct.stock')" width="100" sortable align="center" />
        <el-table-column :label="$t('merchantProduct.status')" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              @change="handleStatusChange(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProduct.actions')" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openDialog('edit', row)">{{ $t('merchantProduct.editBtn') }}</el-button>
            <el-popconfirm :title="$t('merchantProduct.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" plain>{{ $t('merchantProduct.deleteBtn') }}</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? $t('merchantProduct.addTitle') : $t('merchantProduct.editTitle')"
      width="720px"
      destroy-on-close
    >
      <div v-loading="dialogLoading">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('merchantProduct.basicInfo')" name="basic">
              <el-form-item v-if="dialogType === 'edit' && formData.productNo" :label="$t('merchantProduct.productNo')">
                <el-input :model-value="formData.productNo" disabled />
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.categoryLabel')" prop="categoryId">
                <el-select v-model="formData.categoryId" :placeholder="$t('merchantProduct.selectCategory')" style="width: 100%">
                  <el-option
                    v-for="category in categoryOptions"
                    :key="category.id"
                    :label="category.categoryName"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.nameLabel')" prop="productName">
                <el-input v-model="formData.productName" maxlength="60" show-word-limit />
              </el-form-item>

              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item :label="$t('merchantProduct.priceLabel')" prop="price">
                    <el-input-number v-model="formData.price" :min="0" :precision="2" :step="1" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="$t('merchantProduct.originalPrice')">
                    <el-input-number
                      v-model="formData.originalPrice"
                      :min="0"
                      :precision="2"
                      :step="1"
                      :value-on-clear="undefined"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="$t('merchantProduct.stockLabel')" prop="stock">
                    <el-input-number v-model="formData.stock" :min="0" :step="1" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item :label="$t('merchantProduct.flowerLanguage')">
                <el-input v-model="formData.flowerLanguage" maxlength="120" show-word-limit />
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.materialDesc')">
                <el-input
                  v-model="formData.materialDesc"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.supportCustom')">
                <el-switch v-model="formData.supportCustomize" />
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.mainImage')" prop="mainImage">
                <el-upload
                  v-model:file-list="mainImageList"
                  class="uploader"
                  list-type="picture-card"
                  :limit="1"
                  :http-request="uploadProductImage"
                  :on-success="handleMainImageSuccess"
                  :on-remove="handleMainImageRemove"
                  :on-exceed="handleMainImageExceed"
                >
                  <el-icon><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M482 152h60v720h-60z"/><path d="M152 482h720v60H152z"/></svg></el-icon>
                </el-upload>
                <div class="el-upload__tip">{{ $t('merchantProduct.mainImageTip') }}</div>
              </el-form-item>

              <el-form-item :label="$t('merchantProduct.galleryImages')">
                <el-upload
                  v-model:file-list="galleryFileList"
                  class="uploader"
                  list-type="picture-card"
                  :limit="5"
                  multiple
                  :http-request="uploadProductImage"
                  :on-success="handleGallerySuccess"
                  :on-remove="handleGalleryRemove"
                  :on-exceed="handleGalleryExceed"
                >
                  <el-icon><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M482 152h60v720h-60z"/><path d="M152 482h720v60H152z"/></svg></el-icon>
                </el-upload>
                <div class="el-upload__tip">{{ $t('merchantProduct.galleryTip') }}</div>
              </el-form-item>
            </el-tab-pane>

            <el-tab-pane :label="$t('merchantProduct.detail')" name="detail">
              <el-form-item :label="$t('merchantProduct.detailContent')" label-width="0" prop="detailContent">
                <el-input
                  v-model="formData.detailContent"
                  type="textarea"
                  :rows="12"
                  maxlength="5000"
                  show-word-limit
                  :placeholder="$t('merchantProduct.detailPlaceholder')"
                />
              </el-form-item>
            </el-tab-pane>
          </el-tabs>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('merchantProduct.cancelBtn') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="submitLoading" @click="submitForm">{{ $t('merchantProduct.saveBtn') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules, UploadProps, UploadRequestOptions, UploadUserFile } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';
import productPlaceholder from '@/assets/product-placeholder.svg';

const { t } = useI18n();

type ProductRow = {
  id: number;
  productNo: string;
  productName: string;
  mainImage: string;
  price: number;
  stock: number;
  sales: number;
  status: number;
  categoryName: string;
  createTime: string;
};

type CategoryOption = {
  id: number;
  categoryName: string;
  categoryIcon?: string;
};

type MerchantProductDetail = {
  id: number;
  categoryId: number;
  productNo: string;
  productName: string;
  mainImage: string;
  images: string[];
  price: number;
  originalPrice?: number;
  stock: number;
  flowerLanguage?: string;
  materialDesc?: string;
  detailContent?: string;
  supportCustomize: boolean;
  status: number;
};

type ProductFormModel = {
  id: number | null;
  categoryId: number | null;
  productNo: string;
  productName: string;
  mainImage: string;
  images: string[];
  price: number;
  originalPrice?: number;
  stock: number;
  flowerLanguage: string;
  materialDesc: string;
  detailContent: string;
  supportCustomize: boolean;
};

const createEmptyForm = (): ProductFormModel => ({
  id: null,
  categoryId: null,
  productNo: '',
  productName: '',
  mainImage: '',
  images: [],
  price: 0,
  originalPrice: undefined,
  stock: 0,
  flowerLanguage: '',
  materialDesc: '',
  detailContent: '',
  supportCustomize: false
});

const loading = ref(false);
const dialogVisible = ref(false);
const dialogLoading = ref(false);
const submitLoading = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const activeTab = ref('basic');
const tableData = ref<ProductRow[]>([]);
const categoryOptions = ref<CategoryOption[]>([]);
const mainImageList = ref<UploadUserFile[]>([]);
const galleryFileList = ref<UploadUserFile[]>([]);
const formRef = ref<FormInstance>();

const searchForm = reactive<{ keyword: string; status?: number }>({
  keyword: '',
  status: undefined
});

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const formData = reactive<ProductFormModel>(createEmptyForm());

const rules: FormRules<ProductFormModel> = {
  categoryId: [{ required: true, message: () => t('merchantProduct.selectCategoryMsg'), trigger: 'change' }],
  productName: [{ required: true, message: () => t('merchantProduct.inputProductName'), trigger: 'blur' }],
  mainImage: [{ required: true, message: () => t('merchantProduct.uploadMainImage'), trigger: 'change' }],
  price: [
    { required: true, message: () => t('merchantProduct.inputPrice'), trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (Number(value) > 0) {
          callback();
          return;
        }
        callback(new Error(t('merchantProduct.priceMustPositive')));
      },
      trigger: 'change'
    }
  ],
  stock: [{ required: true, message: () => t('merchantProduct.inputStock'), trigger: 'change' }]
};

const buildUploadFile = (url: string, name: string): UploadUserFile => ({
  name,
  url,
  status: 'success'
});

const extractFileUrl = (response: unknown): string => {
  if (!response || typeof response !== 'object') {
    return '';
  }
  const uploadResponse = response as { data?: { fileUrl?: string } };
  return uploadResponse.data?.fileUrl || '';
};

const normalizeUploadFiles = (files: UploadUserFile[]): UploadUserFile[] =>
  files.map((file, index) => ({
    ...file,
    name: file.name || `image-${index + 1}`,
    url: extractFileUrl(file.response) || file.url || ''
  }));

const syncGalleryImages = () => {
  formData.images = galleryFileList.value
    .map((file) => extractFileUrl(file.response) || file.url || '')
    .filter(Boolean);
};

const resetFormState = async () => {
  Object.assign(formData, createEmptyForm());
  mainImageList.value = [];
  galleryFileList.value = [];
  activeTab.value = 'basic';
  await nextTick();
  formRef.value?.clearValidate();
};

const fillForm = (detail: MerchantProductDetail) => {
  Object.assign(formData, {
    id: detail.id,
    categoryId: detail.categoryId,
    productNo: detail.productNo || '',
    productName: detail.productName || '',
    mainImage: detail.mainImage || '',
    images: detail.images || [],
    price: Number(detail.price || 0),
    originalPrice: detail.originalPrice == null ? undefined : Number(detail.originalPrice),
    stock: detail.stock ?? 0,
    flowerLanguage: detail.flowerLanguage || '',
    materialDesc: detail.materialDesc || '',
    detailContent: detail.detailContent || '',
    supportCustomize: Boolean(detail.supportCustomize)
  });

  mainImageList.value = detail.mainImage ? [buildUploadFile(detail.mainImage, 'main-image')] : [];
  galleryFileList.value = (detail.images || []).map((url, index) => buildUploadFile(url, `gallery-${index + 1}`));
};

const fetchCategories = async () => {
  const res = await request.get('/common/categories');
  categoryOptions.value = res.data;
};

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/merchant/products', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword.trim() || undefined,
        status: searchForm.status
      }
    });

    tableData.value = res.data.records.map((item: ProductRow) => ({
      ...item,
      mainImage: item.mainImage || productPlaceholder
    }));
    pagination.total = res.data.total;
  } finally {
    loading.value = false;
  }
};

const openDialog = async (type: 'add' | 'edit', row?: ProductRow) => {
  dialogType.value = type;
  dialogVisible.value = true;
  dialogLoading.value = true;
  await resetFormState();

  try {
    if (!categoryOptions.value.length) {
      await fetchCategories();
    }

    if (type === 'edit' && row) {
      const res = await request.get(`/merchant/products/${row.id}`);
      fillForm(res.data);
    }
  } catch (error) {
    dialogVisible.value = false;
    throw error;
  } finally {
    dialogLoading.value = false;
  }
};

const handleDelete = async (id: number) => {
  await request.delete(`/merchant/products/${id}`);
  ElMessage.success(`${t('merchantProduct.deleted')} ${id}`);

  const shouldGoPrevPage = tableData.value.length === 1 && pagination.pageNum > 1;
  await fetchData(shouldGoPrevPage ? pagination.pageNum - 1 : pagination.pageNum);
};

const handleStatusChange = async (row: ProductRow, value: string | number | boolean) => {
  const nextStatus = Number(value);
  const previousStatus = row.status === 1 ? 0 : 1;
  row.status = nextStatus;

  try {
    await request.patch(`/merchant/products/${row.id}/status`, { status: nextStatus });
    ElMessage.success(nextStatus === 1 ? t('merchantProduct.onSaleNow') : t('merchantProduct.offShelfNow'));
  } catch (error) {
    row.status = previousStatus;
    throw error;
  }
};

const uploadProductImage = async (options: UploadRequestOptions) => {
  const uploadFormData = new FormData();
  uploadFormData.append('file', options.file);
  uploadFormData.append('type', 'product');

  try {
    const res = await request.post('/common/upload', uploadFormData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    options.onSuccess(res);
  } catch (error) {
    options.onError(error as Parameters<UploadRequestOptions['onError']>[0]);
  }
};

const handleMainImageSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  const fileUrl = extractFileUrl(response);
  if (!fileUrl) {
    ElMessage.error(t('merchantProduct.uploadFailed'));
    mainImageList.value = [];
    formData.mainImage = '';
    return;
  }

  formData.mainImage = fileUrl;
  mainImageList.value = [buildUploadFile(fileUrl, uploadFile.name)];
  formRef.value?.validateField('mainImage');
};

const handleMainImageRemove: UploadProps['onRemove'] = () => {
  formData.mainImage = '';
  mainImageList.value = [];
};

const handleMainImageExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning(t('merchantProduct.mainImageOnly'));
};

const handleGallerySuccess: UploadProps['onSuccess'] = (_response, _uploadFile, uploadFiles) => {
  galleryFileList.value = normalizeUploadFiles(uploadFiles);
  syncGalleryImages();
};

const handleGalleryRemove: UploadProps['onRemove'] = (_uploadFile, uploadFiles) => {
  galleryFileList.value = normalizeUploadFiles(uploadFiles);
  syncGalleryImages();
};

const handleGalleryExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning(t('merchantProduct.galleryLimit'));
};

const submitForm = async () => {
  if (!formRef.value) {
    return;
  }

  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitLoading.value = true;
  try {
    const payload = {
      categoryId: formData.categoryId as number,
      productName: formData.productName.trim(),
      mainImage: formData.mainImage,
      images: formData.images,
      price: formData.price,
      originalPrice: formData.originalPrice,
      stock: formData.stock,
      flowerLanguage: formData.flowerLanguage.trim() || undefined,
      materialDesc: formData.materialDesc.trim() || undefined,
      detailContent: formData.detailContent.trim() || undefined,
      supportCustomize: formData.supportCustomize
    };

    if (dialogType.value === 'add') {
      await request.post('/merchant/products', payload);
      ElMessage.success(t('merchantProduct.created'));
      dialogVisible.value = false;
      await fetchData(1);
      return;
    }

    await request.put(`/merchant/products/${formData.id}`, payload);
    ElMessage.success(t('merchantProduct.updated'));
    dialogVisible.value = false;
    await fetchData();
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
  fetchCategories();
});
</script>

<style scoped>
.header-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.filter-form {
  padding: 10px 0;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.uploader {
  width: 100%;
}
</style>
