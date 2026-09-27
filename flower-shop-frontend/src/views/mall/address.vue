<template>
  <div class="address-page">
    <div class="page-header">
      <h2 class="page-title">{{ $t('addressPage.title') }}</h2>
      <el-button type="primary" color="#E8567F" @click="openDialog('add')">{{ $t('addressPage.addAddress') }}</el-button>
    </div>

    <div v-if="addresses.length" class="address-list">
      <div v-for="addr in addresses" :key="addr.id" :class="['address-card', { 'is-default': addr.isDefault }]">
        <div class="address-main">
          <div class="addr-info">
            <span class="addr-name">{{ addr.receiverName }}</span>
            <span class="addr-phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault" type="danger" size="small" effect="dark">{{ $t('addressPage.default') }}</el-tag>
          </div>
          <p class="addr-detail">{{ formatAddress(addr) }}</p>
        </div>
        <div class="address-actions">
          <el-button link type="primary" @click="openDialog('edit', addr)">{{ $t('common.edit') }}</el-button>
          <el-button link type="primary" :disabled="addr.isDefault" @click="setDefault(addr.id)">{{ $t('addressPage.setDefault') }}</el-button>
          <el-popconfirm :title="$t('addressPage.confirmDelete')" @confirm="deleteAddress(addr.id)">
            <template #reference>
              <el-button link type="danger">{{ $t('common.delete') }}</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>
    <el-empty v-else :description="$t('addressPage.noAddress')" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? $t('addressPage.addAddress') : $t('addressPage.editAddress')" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item :label="$t('addressPage.receiverName')" prop="receiverName">
          <el-input v-model="formData.receiverName" maxlength="20" />
        </el-form-item>
        <el-form-item :label="$t('addressPage.receiverPhone')" prop="receiverPhone">
          <el-input
            v-model="formData.receiverPhone"
            maxlength="11"
            inputmode="numeric"
            @input="handleReceiverPhoneInput"
          />
        </el-form-item>
        <el-form-item :label="$t('addressPage.detailAddress')" prop="detailAddress">
          <el-input v-model="formData.detailAddress" type="textarea" :rows="3" maxlength="200" />
        </el-form-item>
        <el-form-item :label="$t('addressPage.setAsDefault')">
          <el-switch v-model="formData.isDefault" />
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
import { formatAddress } from '@/utils/address';
import { isValidPhone, sanitizePhoneInput } from '@/utils/phone';

const { t } = useI18n();

type Address = {
  id: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
  isDefault: boolean;
};

const addresses = ref<Address[]>([]);
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitting = ref(false);
const formRef = ref<FormInstance>();
const editId = ref<number | null>(null);

const formData = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false,
});

const rules = {
  receiverName: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  receiverPhone: [{
    validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!value) {
        callback(new Error(t('common.required')));
        return;
      }
      if (!isValidPhone(value)) {
        callback(new Error(t('common.phoneInvalid')));
        return;
      }
      callback();
    },
    trigger: ['blur', 'change']
  }],
  detailAddress: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
};

const handleReceiverPhoneInput = () => {
  formData.receiverPhone = sanitizePhoneInput(formData.receiverPhone);
};

const resetForm = () => {
  Object.assign(formData, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false });
  editId.value = null;
};

const fetchAddresses = async () => {
  try {
    const res = await request.get('/customer/addresses', { timeout: 3000 });
    addresses.value = res.data || [];
  } catch {
    addresses.value = [];
  }
};

const openDialog = (type: 'add' | 'edit', addr?: Address) => {
  dialogType.value = type;
  resetForm();
    if (type === 'edit' && addr) {
      editId.value = addr.id;
      Object.assign(formData, addr);
      formData.receiverPhone = sanitizePhoneInput(formData.receiverPhone);
    }
  dialogVisible.value = true;
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (dialogType.value === 'add') {
      await request.post('/customer/addresses', formData);
      ElMessage.success(t('common.success'));
    } else {
      await request.put(`/customer/addresses/${editId.value}`, formData);
      ElMessage.success(t('common.success'));
    }
    dialogVisible.value = false;
    await fetchAddresses();
  } catch {
    // 统一错误提示由请求拦截器处理
  } finally {
    submitting.value = false;
  }
};

const setDefault = async (id: number) => {
  try {
    await request.patch(`/customer/addresses/${id}/default`);
    ElMessage.success(t('common.success'));
    await fetchAddresses();
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

const deleteAddress = async (id: number) => {
  try {
    await request.delete(`/customer/addresses/${id}`);
    ElMessage.success(t('common.success'));
    await fetchAddresses();
  } catch {
    // 统一错误提示由请求拦截器处理
  }
};

onMounted(() => {
  fetchAddresses();
});
</script>

<style scoped lang="scss">
.address-page { min-height: 60vh; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  border: 2px solid transparent;
  transition: all 0.2s;
  &.is-default { border-color: #E8567F; }
  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06); }
}

.addr-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.addr-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.addr-phone { font-size: 14px; color: #888; }

.addr-detail {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
