<template>
  <div class="login-container">
    <div class="login-right">
      <div class="login-form-box">
        <h2 class="title">{{ $t('register.title') }}</h2>

        <el-tabs v-model="registerForm.roleType" stretch>
          <el-tab-pane :label="$t('register.buyerReg')" name="CUSTOMER"></el-tab-pane>
          <el-tab-pane :label="$t('register.merchantReg')" name="MERCHANT"></el-tab-pane>
        </el-tabs>

        <el-form ref="formRef" :model="registerForm" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" :placeholder="$t('register.username')" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              :placeholder="$t('register.phone')"
              maxlength="11"
              inputmode="numeric"
              @input="handlePhoneInput"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              :placeholder="$t('register.password')"
              show-password
              @input="checkStrength"
            />
            <div v-if="registerForm.password" class="password-strength">
              <span class="strength-label">{{ $t('register.passwordStrength') }}</span>
              <div class="strength-bars" :class="`is-${strengthLevel}`">
                <span
                  v-for="item in 3"
                  :key="item"
                  class="strength-bar"
                  :class="{ active: item <= strengthStep }"
                />
              </div>
              <span class="strength-text">{{ strengthText }}</span>
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" :placeholder="$t('register.confirmPassword')" show-password />
          </el-form-item>

          <template v-if="registerForm.roleType === 'MERCHANT'">
            <el-form-item prop="shopName" :label="$t('register.shopName')">
              <el-input v-model="registerForm.shopName" :placeholder="$t('register.shopNamePlaceholder')" />
            </el-form-item>
          </template>

          <el-button type="primary" color="#E8567F" class="submit-btn" :loading="loading" @click="handleRegister">
            {{ $t('register.signUp') }}
          </el-button>

          <div class="register-link">
            {{ $t('register.hasAccount') }}
            <router-link to="/login">{{ $t('register.signIn') }}</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';
import { User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';
import { isValidPhone, sanitizePhoneInput } from '@/utils/phone';

const { t } = useI18n();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const pwdStrength = ref(0);

const strengthLevel = computed(() => {
  if (!registerForm.password) {
    return 'weak';
  }
  if (pwdStrength.value < 40) return 'weak';
  if (pwdStrength.value < 80) return 'medium';
  return 'strong';
});

const strengthStep = computed(() => {
  if (!registerForm.password) {
    return 0;
  }
  if (pwdStrength.value < 40) return 1;
  if (pwdStrength.value < 80) return 2;
  return 3;
});

const strengthText = computed(() => {
  if (strengthLevel.value === 'strong') return t('register.passwordStrong');
  if (strengthLevel.value === 'medium') return t('register.passwordMedium');
  return t('register.passwordWeak');
});

const validatePass2 = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error(t('register.reenterPassword')));
  } else if (value !== registerForm.password) {
    callback(new Error(t('register.passwordNotMatch')));
  } else {
    callback();
  }
};

const validateShopName = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (registerForm.roleType === 'MERCHANT' && !value.trim()) {
    callback(new Error(t('register.shopNameRequired')));
    return;
  }
  callback();
};

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error(t('common.required')));
    return;
  }
  if (value.length < 8 || value.length > 72) {
    callback(new Error(t('register.passwordMinLength')));
    return;
  }
  callback();
};

const validatePhone = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error(t('common.required')));
    return;
  }
  if (!isValidPhone(value)) {
    callback(new Error(t('common.phoneInvalid')));
    return;
  }
  callback();
};

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  roleType: 'CUSTOMER',
  shopName: ''
});

const rules = {
  username: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: ['blur', 'change'] }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  shopName: [{ validator: validateShopName, trigger: 'blur' }]
};

const handlePhoneInput = () => {
  registerForm.phone = sanitizePhoneInput(registerForm.phone);
};

const checkStrength = (val: string) => {
  let score = 0;
  if (!val) return (pwdStrength.value = 0);
  if (val.length > 5) score += 20;
  if (val.length > 8) score += 20;
  if (/[a-z]/.test(val)) score += 20;
  if (/[A-Z]/.test(val)) score += 20;
  if (/[0-9]/.test(val)) score += 20;
  pwdStrength.value = score;
};

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    await request.post('/auth/register', {
      username: registerForm.username.trim(),
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      phone: sanitizePhoneInput(registerForm.phone),
      role: registerForm.roleType,
      shopName: registerForm.roleType === 'MERCHANT' ? registerForm.shopName.trim() : undefined
    });

    ElMessage.success(registerForm.roleType === 'MERCHANT' ? t('register.merchantRegSuccess') : t('register.regSuccess'));
    router.push({ path: '/login', query: { username: registerForm.username.trim() } });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(232, 86, 127, 0.12), transparent 35%),
    linear-gradient(180deg, #fffafc 0%, #f7f8fb 100%);

  .login-right {
    width: min(100%, 520px);
    padding: 20px;
  }

  .login-form-box {
    padding: 40px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.05);

    .title {
      text-align: center;
      margin-bottom: 20px;
      color: #333;
    }

    .submit-btn {
      width: 100%;
      margin-top: 20px;
      margin-bottom: 20px;
    }

    .password-strength {
      display: flex;
      align-items: center;
      gap: 10px;
      width: 100%;
      margin-top: 10px;
      font-size: 12px;
      color: #909399;
      line-height: 1;
    }

    .strength-label,
    .strength-text {
      flex-shrink: 0;
    }

    .strength-bars {
      display: flex;
      align-items: center;
      gap: 6px;
      width: 96px;
    }

    .strength-bar {
      flex: 1;
      height: 6px;
      border-radius: 999px;
      background: #ebeef5;
      transition: background-color 0.2s ease;
    }

    .strength-bars.is-weak .strength-bar.active {
      background: #f56c6c;
    }

    .strength-bars.is-medium .strength-bar.active {
      background: #e6a23c;
    }

    .strength-bars.is-strong .strength-bar.active {
      background: #67c23a;
    }

    .register-link {
      text-align: center;
      font-size: 14px;

      a {
        color: var(--primary-color);
        font-weight: bold;
      }
    }
  }
}
</style>
