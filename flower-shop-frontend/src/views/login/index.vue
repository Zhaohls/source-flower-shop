<template>
  <div class="login-container">
    <div class="login-left">
      <div class="logo">
        <svg viewBox="0 0 1024 1024" width="32" height="32" fill="#fff"><path d="M512 128c-212.1 0-384 171.9-384 384s171.9 384 384 384 384-171.9 384-384S724.1 128 512 128zm0 704c-176.7 0-320-143.3-320-320S335.3 192 512 192s320 143.3 320 320-143.3 320-320 320z"/><path d="M512 366c-26.5 0-48 21.5-48 48v128c0 26.5 21.5 48 48 48s48-21.5 48-48V414c0-26.5-21.5-48-48-48z"/><circle cx="512" cy="640" r="40"/></svg>
        <span>{{ $t('login.brandName') }}</span>
      </div>
      <div class="slogan">
        <h2>{{ $t('login.slogan') }}</h2>
        <p>{{ $t('login.sloganDesc') }}</p>
      </div>
    </div>
    
    <div class="login-right">
      <div class="login-form-box">
        <h2 class="title">{{ $t('login.welcomeBack') }}</h2>
        <p class="login-tip">{{ $t('login.autoDetectRole') }}</p>

        <el-form ref="formRef" :model="loginForm" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" :placeholder="$t('login.username')" :prefix-icon="User" />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" :placeholder="$t('login.password')" show-password :prefix-icon="Lock" />
          </el-form-item>
          
          <div class="options">
            <el-checkbox v-model="loginForm.rememberMe">{{ $t('login.rememberMe') }}</el-checkbox>
            <el-link type="primary" underline="never" @click="openForgotDialog">{{ $t('login.forgotPassword') }}</el-link>
          </div>
          
          <el-button type="primary" color="#E8567F" class="submit-btn" :loading="loading" @click="handleLogin">
            {{ $t('login.loginBtn') }}
          </el-button>
          
          <div class="register-link">
            {{ $t('login.noAccount') }} 
            <router-link to="/register">{{ $t('login.createOne') }}</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <el-dialog v-model="forgotVisible" :title="$t('login.forgotPasswordTitle')" width="420px" destroy-on-close>
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-position="top">
        <el-form-item prop="username" :label="$t('login.username')">
          <el-input v-model="forgotForm.username" :placeholder="$t('login.username')" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone" :label="$t('login.resetPhone')">
          <el-input
            v-model="forgotForm.phone"
            :maxlength="11"
            inputmode="numeric"
            :placeholder="$t('login.resetPhonePlaceholder')"
            @input="forgotForm.phone = sanitizePhoneInput(forgotForm.phone)"
          />
        </el-form-item>
        <el-form-item prop="password" :label="$t('login.newPassword')">
          <el-input v-model="forgotForm.password" type="password" show-password :placeholder="$t('login.newPassword')" />
        </el-form-item>
        <el-form-item prop="confirmPassword" :label="$t('login.confirmPassword')">
          <el-input v-model="forgotForm.confirmPassword" type="password" show-password :placeholder="$t('login.confirmPassword')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="forgotLoading" @click="handleForgotPassword">
          {{ $t('login.resetPasswordBtn') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';
import { isValidPhone, sanitizePhoneInput } from '@/utils/phone';

const { t } = useI18n();
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const forgotFormRef = ref<FormInstance>();

const loading = ref(false);
const forgotVisible = ref(false);
const forgotLoading = ref(false);

const resolveRoleHome = (role: string) => {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard';
    case 'MERCHANT':
      return '/merchant/products';
    default:
      return '/mall';
  }
};

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
});

const forgotForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

const rules = {
  username: [{ required: true, message: () => t('login.enterUsername'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.enterPassword'), trigger: 'blur' }]
};

const forgotRules = {
  username: [{ required: true, message: () => t('login.enterUsername'), trigger: 'blur' }],
  phone: [
    { required: true, message: () => t('login.enterPhone'), trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => (
        isValidPhone(value) ? callback() : callback(new Error(t('common.phoneInvalid')))
      ),
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: () => t('login.enterNewPassword'), trigger: 'blur' },
    { min: 8, max: 72, message: () => t('login.passwordMinLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: () => t('login.enterConfirmPassword'), trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => (
        value === forgotForm.password ? callback() : callback(new Error(t('login.passwordMismatch')))
      ),
      trigger: 'blur'
    }
  ]
};

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    const res = await request.post('/auth/login', {
      username: loginForm.username.trim(),
      password: loginForm.password
    });

    const loginData = res.data;
    const userInfo = loginData.userInfo;

    userStore.setToken(loginData.token);
    userStore.setUserInfo(userInfo);

    if (loginForm.rememberMe) {
      localStorage.setItem('remember_username', loginForm.username.trim());
    } else {
      localStorage.removeItem('remember_username');
    }

    ElMessage.success(t('login.loginSuccess'));
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : resolveRoleHome(userInfo.role);
    router.push(redirect);
  } finally {
    loading.value = false;
  }
};

const openForgotDialog = () => {
  forgotForm.username = loginForm.username.trim();
  forgotForm.phone = '';
  forgotForm.password = '';
  forgotForm.confirmPassword = '';
  forgotVisible.value = true;
};

const handleForgotPassword = async () => {
  const valid = await forgotFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  forgotLoading.value = true;
  try {
    await request.post('/auth/forgot-password', {
      username: forgotForm.username.trim(),
      phone: forgotForm.phone.trim(),
      password: forgotForm.password,
      confirmPassword: forgotForm.confirmPassword
    });
    loginForm.username = forgotForm.username.trim();
    forgotVisible.value = false;
    ElMessage.success(t('login.resetPasswordSuccess'));
  } finally {
    forgotLoading.value = false;
  }
};

onMounted(() => {
  const remembered = localStorage.getItem('remember_username');
  if (remembered) {
    loginForm.username = remembered;
    loginForm.rememberMe = true;
  }
  if (typeof route.query.username === 'string') {
    loginForm.username = route.query.username;
  }
});
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  
  .login-left {
    flex: 1;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 60px;
    position: relative;
    overflow: hidden;

    @media (max-width: 768px) {
      display: none;
    }

    .logo {
      position: absolute;
      top: 40px;
      left: 40px;
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 24px;
      font-weight: bold;
    }

    .slogan {
      z-index: 1;
      h2 {
        font-size: 3rem;
        margin-bottom: 20px;
      }
      p {
        font-size: 1.2rem;
        opacity: 0.9;
        max-width: 80%;
      }
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: -100px;
      right: -100px;
      width: 400px;
      height: 400px;
      border-radius: 50%;
      background: rgba(255,255,255,0.1);
    }
  }

  .login-right {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: var(--bg-color);

    .login-form-box {
      width: 400px;
      padding: 40px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.05);

      .title {
        text-align: center;
        margin-bottom: 12px;
        color: #333;
      }

      .login-tip {
        margin: 0 0 24px;
        text-align: center;
        font-size: 14px;
        color: #909399;
      }

      .options {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20px;
      }

      .submit-btn {
        width: 100%;
        margin-bottom: 20px;
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
}
</style>
