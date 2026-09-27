<template>
  <div class="merchant-settings">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('merchantSettings.title') }}</span></div></template>
      <el-form ref="formRef" :model="form" label-width="120px" :rules="rules" style="max-width: 600px;">
        <el-form-item :label="$t('merchantSettings.shopName')" prop="shopName">
          <el-input v-model="form.shopName" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('merchantSettings.shopDesc')">
          <el-input v-model="form.shopDesc" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('merchantSettings.shopLogo')">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" :data="{ type: 'shop' }" :show-file-list="false" :on-success="handleLogoSuccess" accept="image/*">
            <el-avatar :size="80" :src="form.shopLogo || undefined" class="logo-preview">
              {{ form.shopName?.slice(0, 1) || '?' }}
            </el-avatar>
          </el-upload>
          <span class="upload-tip">{{ $t('merchantSettings.uploadTip') }}</span>
        </el-form-item>
        <el-form-item :label="$t('merchantSettings.contactPhone')" prop="contactPhone">
          <el-input
            v-model="form.contactPhone"
            maxlength="11"
            inputmode="numeric"
            @input="handleContactPhoneInput"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#E8567F" :loading="saving" @click="saveSettings">{{ $t('common.save') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useUserStore } from '@/stores/user';
import request from '@/api/request';
import { isValidPhone, sanitizePhoneInput } from '@/utils/phone';

const { t } = useI18n();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const saving = ref(false);

const uploadUrl = `${import.meta.env.DEV ? '/api' : import.meta.env.VITE_API_BASE_URL || '/api'}/common/upload`;
const uploadHeaders = { Authorization: `Bearer ${userStore.token}` };

const form = reactive({
  shopName: '',
  shopDesc: '',
  shopLogo: '',
  contactPhone: '',
});

const rules = {
  shopName: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  contactPhone: [{
    validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!value) {
        callback();
        return;
      }
      if (!isValidPhone(value)) {
        callback(new Error(t('common.phoneInvalid')));
        return;
      }
      callback();
    },
    trigger: ['blur', 'change']
  }]
};

const fetchSettings = async () => {
  try {
    const res = await request.get('/merchant/shop-info', { timeout: 3000 });
    if (res.data) {
      Object.assign(form, res.data);
      form.contactPhone = sanitizePhoneInput(form.contactPhone);
    }
  } catch {
    form.shopName = userStore.userInfo?.shopName || '';
    form.contactPhone = sanitizePhoneInput(userStore.userInfo?.phone || '');
  }
};

const handleLogoSuccess = (response: any) => {
  form.shopLogo = response?.data?.fileUrl || response?.data?.url || response?.data || '';
};

const handleContactPhoneInput = () => {
  form.contactPhone = sanitizePhoneInput(form.contactPhone);
};

const saveSettings = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    await request.put('/merchant/shop-info', form);
    ElMessage.success(t('common.success'));
  } finally { saving.value = false; }
};

onMounted(() => { fetchSettings(); });
</script>

<style scoped>
.merchant-settings { padding: 20px; }
.header-row { font-weight: 700; }
.logo-preview { background: linear-gradient(135deg, #E8567F, #FF8BA7); color: #fff; font-weight: 600; font-size: 28px; cursor: pointer; }
.upload-tip { font-size: 12px; color: #999; margin-left: 12px; }
</style>
