<template>
  <div class="account-page">
    <h2 class="page-title">{{ $t('account.title') }}</h2>

    <el-row :gutter="24">
      <!-- 左侧个人信息卡 -->
      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <div class="profile-header">
            <el-avatar :size="80" class="avatar">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '?').slice(0, 1) }}
            </el-avatar>
            <h3 class="profile-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h3>
            <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
          </div>
          <el-divider />
          <div class="profile-actions">
            <el-button type="primary" color="#E8567F" @click="$router.push('/mall/cart')">
              <svg viewBox="0 0 1024 1024" width="14" height="14" fill="currentColor" style="margin-right: 6px;">
                <path d="M922.9 335.2H324.3L292.6 162c-2.8-15.2-16.1-26.2-31.6-26.2H120.6c-17.7 0-32 14.3-32 32s14.3 32 32 32h114.5l31.7 173.2L331 684.4c2.8 15.2 16.1 26.2 31.6 26.2h443.8c13.3 0 25.3-8.3 29.9-20.9l110.6-316.4c7.9-22.5-8.5-38.1-24-38.1zM793.2 646.6H389.4L335 399.2h548.4L793.2 646.6z"/>
                <path d="M416 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/>
                <path d="M736 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/>
              </svg>
              {{ $t('account.viewCart') }}
            </el-button>
            <el-button plain @click="openPasswordDialog">
              {{ $t('account.changePassword') }}
            </el-button>
            <el-button plain @click="handleLogout">
              {{ $t('account.logout') }}
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详细信息 -->
      <el-col :span="16">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item :label="$t('account.username')">
              {{ userStore.userInfo?.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('account.phone')">
              {{ userStore.userInfo?.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('account.email')">
              {{ userStore.userInfo?.email || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('account.shop')">
              {{ userStore.userInfo?.shopName || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="passwordDialogVisible" :title="$t('account.changePasswordTitle')" width="460px" destroy-on-close>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item :label="$t('account.oldPassword')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('account.newPassword')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('account.confirmPassword')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="passwordSubmitting" @click="submitPasswordChange">
          {{ $t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useI18n } from 'vue-i18n';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';
import request from '@/api/request';

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();
const passwordDialogVisible = ref(false);
const passwordSubmitting = ref(false);
const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const roleLabel = computed(() => {
  switch (userStore.userInfo?.role) {
    case 'ADMIN': return t('account.roleAdmin');
    case 'MERCHANT': return t('account.roleMerchant');
    default: return t('account.roleBuyer');
  }
});

const roleTagType = computed(() => {
  switch (userStore.userInfo?.role) {
    case 'ADMIN': return 'danger';
    case 'MERCHANT': return 'warning';
    default: return 'primary';
  }
});

const passwordRules = {
  oldPassword: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  newPassword: [{
    validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!value) {
        callback(new Error(t('common.required')));
        return;
      }
      if (value.length < 8 || value.length > 72) {
        callback(new Error(t('account.passwordMinLength')));
        return;
      }
      callback();
    },
    trigger: 'blur'
  }],
  confirmPassword: [{
    validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!value) {
        callback(new Error(t('common.required')));
        return;
      }
      if (value !== passwordForm.newPassword) {
        callback(new Error(t('register.passwordNotMatch')));
        return;
      }
      callback();
    },
    trigger: 'blur'
  }]
};

const resetPasswordForm = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

const openPasswordDialog = () => {
  resetPasswordForm();
  passwordDialogVisible.value = true;
};

const submitPasswordChange = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  passwordSubmitting.value = true;
  try {
    await request.put('/customer/profile/password', passwordForm);
    ElMessage.success(t('account.passwordChanged'));
    passwordDialogVisible.value = false;
    resetPasswordForm();
  } finally {
    passwordSubmitting.value = false;
  }
};

const handleLogout = () => {
  userStore.logout();
  router.replace('/login');
};
</script>

<style scoped lang="scss">
.account-page {
  min-height: 60vh;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2f2a33;
  margin: 0 0 24px;
}

.profile-card, .info-card {
  border-radius: 12px;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

.avatar {
  background: linear-gradient(135deg, #E8567F, #FF8BA7);
  color: #fff;
  font-size: 32px;
  font-weight: 700;
}

.profile-name {
  margin: 0;
  font-size: 20px;
  color: #2f2a33;
}

.profile-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}
</style>
