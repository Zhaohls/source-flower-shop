<template>
  <div class="mall-layout">
    <!-- 顶部导航栏 - PC端 -->
    <header class="mall-header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/mall')">
          <svg viewBox="0 0 1024 1024" width="24" height="24" fill="#E8567F"><path d="M512 128c-212.1 0-384 171.9-384 384s171.9 384 384 384 384-171.9 384-384S724.1 128 512 128zm0 704c-176.7 0-320-143.3-320-320S335.3 192 512 192s320 143.3 320 320-143.3 320-320 320z"/><path d="M512 366c-26.5 0-48 21.5-48 48v128c0 26.5 21.5 48 48 48s48-21.5 48-48V414c0-26.5-21.5-48-48-48z"/><circle cx="512" cy="640" r="40"/></svg>
          <span>Bloomy 花店</span>
        </div>

        <nav class="nav-links">
          <router-link to="/mall" exact-active-class="active">{{ $t('mall.home') }}</router-link>
          <router-link to="/mall/products" active-class="active">{{ $t('mall.shop') }}</router-link>
          <router-link to="/mall/cart" active-class="active">
            <el-badge :value="cartBadge" :hidden="!cartBadge" :max="99">
              {{ $t('mall.cart') }}
            </el-badge>
          </router-link>
          <router-link to="/mall/orders" active-class="active">{{ $t('ordersPage.title') }}</router-link>
          <router-link to="/mall/account" active-class="active">{{ $t('mall.myAccount') }}</router-link>
        </nav>

        <div class="header-actions">
          <el-input
            v-model="searchKey"
            :placeholder="$t('common.search')"
            class="search-box"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M909.6 854.5L649.9 594.8C690.2 542.7 714 478.4 714 408c0-167.4-135.6-303-303-303S108 240.6 108 408s135.6 303 303 303c70.4 0 134.7-23.8 186.8-64.1l259.7 259.7c3.1 3.1 7.2 4.7 11.3 4.7s8.2-1.6 11.3-4.7l29.5-29.5c6.2-6.3 6.2-16.4 0-22.6zM411 676c-147.3 0-267-119.7-267-267S263.7 142 411 142s267 119.7 267 267-119.7 267-267 267z"/></svg></el-icon>
            </template>
          </el-input>
          <el-badge :value="cartBadge" :hidden="!cartBadge" :max="99" class="cart-badge">
            <el-button circle @click="router.push('/mall/cart')">
              <el-icon size="18"><svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M922.9 335.2H324.3L292.6 162c-2.8-15.2-16.1-26.2-31.6-26.2H120.6c-17.7 0-32 14.3-32 32s14.3 32 32 32h114.5l31.7 173.2L331 684.4c2.8 15.2 16.1 26.2 31.6 26.2h443.8c13.3 0 25.3-8.3 29.9-20.9l110.6-316.4c7.9-22.5-8.5-38.1-24-38.1zM793.2 646.6H389.4L335 399.2h548.4L793.2 646.6z"/><path d="M416 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/><path d="M736 790.4m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z"/></svg></el-icon>
            </el-button>
          </el-badge>
          <template v-if="!userStore.token">
            <el-button text @click="router.push('/register')">注册</el-button>
            <el-button type="primary" color="#E8567F" @click="router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })">
              登录
            </el-button>
          </template>
          <el-dropdown v-if="userStore.token" trigger="click">
            <el-avatar size="small" class="user-avatar">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '?').slice(0, 1) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/mall/account')">{{ $t('mall.myAccount') }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">{{ $t('account.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="mall-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <h3>Bloomy 花店</h3>
          <p>每一刻的鲜花花束，用心传递你的爱意</p>
        </div>
        <div class="footer-links">
          <router-link to="/mall">{{ $t('mall.home') }}</router-link>
          <router-link to="/mall/cart">{{ $t('mall.cart') }}</router-link>
          <router-link to="/mall/account">{{ $t('mall.myAccount') }}</router-link>
        </div>
        <div class="footer-copy">
          &copy; 2026 Bloomy 花店
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const searchKey = ref('');

const currentRole = computed(() => userStore.userInfo?.role || userStore.roles[0]);
const cartBadge = computed(() => (
  userStore.token && currentRole.value === 'CUSTOMER' && cartStore.totalCount > 0
    ? cartStore.totalCount
    : undefined
));

watch(
  [() => userStore.token, currentRole],
  ([token, role]) => {
    if (token && role === 'CUSTOMER') {
      void cartStore.loadCart().catch(() => undefined);
    } else if (cartStore.items.length > 0) {
      cartStore.clearCart();
    }
  },
  { immediate: true }
);

const handleSearch = () => {
  if (searchKey.value.trim()) {
    router.push({ path: '/mall/products', query: { keyword: searchKey.value.trim() } });
  } else {
    router.push('/mall/products');
  }
};

const handleLogout = () => {
  cartStore.clearCart();
  userStore.logout();
  router.replace('/login');
};
</script>

<style scoped lang="scss">
.mall-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fb;
}

/* ====== 顶部导航 ====== */
.mall-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #E8567F;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;

  &:hover {
    opacity: 0.85;
  }
}

.nav-links {
  display: flex;
  gap: 8px;

  a {
    padding: 8px 16px;
    border-radius: 8px;
    color: #555;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.25s;

    &:hover {
      color: #E8567F;
      background: rgba(232, 86, 127, 0.06);
    }

    &.active {
      color: #E8567F;
      background: rgba(232, 86, 127, 0.1);
      font-weight: 600;
    }
  }
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  width: 220px;

  :deep(.el-input__wrapper) {
    border-radius: 20px;
    background: #f5f5f5;
    box-shadow: none !important;

    &:hover, &.is-focus {
      background: #fff;
      box-shadow: 0 0 0 1px #E8567F !important;
    }
  }
}

.cart-badge {
  :deep(.el-badge__content) {
    background-color: #E8567F;
  }
}

.user-avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #E8567F, #FF8BA7);
  color: #fff;
  font-weight: 600;
}

/* ====== 主内容 ====== */
.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 32px;
}

/* ====== 页脚 ====== */
.mall-footer {
  background: #1E1E2D;
  color: rgba(255, 255, 255, 0.7);
  margin-top: auto;
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.footer-brand {
  h3 {
    color: #E8567F;
    margin: 0 0 8px;
    font-size: 18px;
  }
  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.7;
  }
}

.footer-links {
  display: flex;
  gap: 24px;

  a {
    color: rgba(255, 255, 255, 0.6);
    text-decoration: none;
    font-size: 14px;
    transition: color 0.25s;

    &:hover {
      color: #E8567F;
    }
  }
}

.footer-copy {
  font-size: 13px;
  opacity: 0.5;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  width: 100%;
}

/* ====== 动画 ====== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
