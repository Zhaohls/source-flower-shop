<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!isCollapse">{{ portalTitle }}</span>
        <span v-else>🌸</span>
      </div>
      <el-menu :collapse="isCollapse" class="el-menu-vertical" background-color="#1E1E2D" text-color="#fff" active-text-color="#E8567F" router>
        <el-menu-item
          v-for="item in menuItems"
          :key="item.index"
          :index="item.index"
        >
          <el-icon><span v-html="item.icon"></span></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button text @click="isCollapse = !isCollapse" class="toggle-btn">
            <el-icon size="20">
              <svg v-if="!isCollapse" viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M408 442h480c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H408c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8zm-8 204c0 4.4 3.6 8 8 8h480c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H408c-4.4 0-8 3.6-8 8v56zm504-486H120c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h784c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm0 632H120c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h784c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z"/></svg>
              <svg v-else viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M408 442h480c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H408c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8zm-8 204c0 4.4 3.6 8 8 8h480c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H408c-4.4 0-8 3.6-8 8v56zm504-486H120c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h784c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm0 632H120c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h784c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z"/></svg>
            </el-icon>
          </el-button>
        </div>
        <div class="right">

          <el-switch v-model="isDark" :active-text="$t('adminLayout.dark')" :inactive-text="$t('adminLayout.light')" @change="toggleTheme"></el-switch>
          <span class="username">{{ displayName }}</span>
          <el-button link type="danger" @click="handleLogout">{{ $t('adminLayout.logoutBtn') }}</el-button>
          <el-avatar size="small" style="margin-left: 15px;" />
        </div>
      </el-header>
      
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const isCollapse = ref(false);
const isDark = ref(false);
const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();

const portalTitle = computed(() => (userStore.userInfo?.role === 'MERCHANT' ? t('adminLayout.flowerMerchant') : t('adminLayout.flowerAdmin')));

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || t('adminLayout.guest'));

// SVG 图标映射 - 解决无 UnoCSS 的图标显示问题
const svgIcons: Record<string, string> = {
  'dashboard': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M924.8 385.6a446.7 446.7 0 00-96-142.4 446.7 446.7 0 00-142.4-96C631.1 123.8 572.5 112 512 112s-119.1 11.8-174.4 35.2a446.7 446.7 0 00-142.4 96 446.7 446.7 0 00-96 142.4C75.8 440.9 64 499.5 64 560c0 132.7 58.3 257.7 159.9 343.1l1.7 1.4c5.8 4.8 13.1 7.5 20.6 7.5h531.7c7.5 0 14.8-2.7 20.6-7.5l1.7-1.4C901.7 817.7 960 692.7 960 560c0-60.5-11.9-119.1-35.2-174.4zM512 880H210.8c-73.9-69.4-117.6-167-117.6-272 0-96.8 38.2-188 107.6-256.6C270.4 282 361.2 244 458 244h108c96.8 0 187.6 38 256.6 107.4 69.4 68.6 107.6 159.8 107.6 256.6 0 105-43.7 202.6-117.6 272H512z"/><path d="M484 380v264h56V380h-56z"/></svg>',
  'goods': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M832 312H696v-16c0-101.6-82.4-184-184-184s-184 82.4-184 184v16H192c-17.7 0-32 14.3-32 32v536c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V344c0-17.7-14.3-32-32-32zm-432-16c0-66.2 53.8-120 120-120s120 53.8 120 120v16H400v-16zm368 584H256V376h72v80h56v-80h256v80h56v-80h72v504z"/></svg>',
  'sell': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M928 160H96c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h832c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zm-40 632H136V232h752v560z"/><path d="M304.8 524h50.7c3.7 0 6.8-3 6.8-6.8v-79.5c0-3.7-3-6.8-6.8-6.8h-50.7c-3.7 0-6.8 3-6.8 6.8v79.5c0 3.7 3.1 6.8 6.8 6.8zM616 422H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h144c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm144 0H688c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h72c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zM304.8 668h50.7c3.7 0 6.8-3 6.8-6.8v-79.5c0-3.7-3-6.8-6.8-6.8h-50.7c-3.7 0-6.8 3-6.8 6.8v79.5c0 3.7 3.1 6.8 6.8 6.8zM616 566H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h144c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm144 0H688c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h72c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z"/></svg>',
  'user': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M858.5 763.6a374 374 0 00-80.6-119.5 375.6 375.6 0 00-119.5-80.6c-.4-.2-.8-.3-1.2-.5C719.5 518 760 444.7 760 362c0-137-111-248-248-248S264 225 264 362c0 82.7 40.5 156 102.8 201.1-.4.2-.8.3-1.2.5-44.8 18.9-85 46-119.5 80.6a375.6 375.6 0 00-80.6 119.5A371.7 371.7 0 00136 901.8a8 8 0 008 8.2h60c4.4 0 7.9-3.5 8-7.8 2-77.2 33-149.5 87.8-204.3 56.7-56.7 132-87.9 212.2-87.9s155.5 31.2 212.2 87.9C beckoned 670.5 755 742.8 757 820c.1 4.4 3.6 7.8 8 7.8h60a8 8 0 008-8.2c-1-47.8-10.9-94.3-29.5-138zM512 534c-45.9 0-89.1-17.9-121.6-50.4S340 407.9 340 362c0-45.9 17.9-89.1 50.4-121.6S466.1 190 512 190s89.1 17.9 121.6 50.4S684 316.1 684 362c0 45.9-17.9 89.1-50.4 121.6S557.9 534 512 534z"/></svg>',
  'building': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M832 64H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32zm-40 824H232V136h560v752z"/><path d="M342 472h72v56h-72zm0-120h72v56h-72zm0 240h72v56h-72zm164-240h72v56h-72zm0 120h72v56h-72zm0 240h72v56h-72zm164-360h72v56h-72zm0 120h72v56h-72zm0 240h72v56h-72z"/></svg>',
  'inventory': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M896 128H128c-35.3 0-64 28.7-64 64v640c0 35.3 28.7 64 64 64h768c35.3 0 64-28.7 64-64V192c0-35.3-28.7-64-64-64zm0 704H128V192h768v640z"/><path d="M480 464H288v-64h192v64zm256 0H544v-64h192v64zm-256 192H288v-64h192v64zm256 0H544v-64h192v64z"/></svg>',
  'star': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M908.1 353.1l-253.9-36.9L540.7 86.1c-3.1-6.3-8.2-11.4-14.5-14.5-15.8-7.8-35-1.3-42.9 14.5L369.8 316.2l-253.9 36.9c-7 1-13.4 4.3-18.3 9.3a32.05 32.05 0 00.6 45.3l183.7 179.1-43.4 252.9a31.95 31.95 0 0046.4 33.7L512 754l227.1 119.4c6.2 3.3 13.4 4.4 20.3 3.2 17.4-3 29.1-19.5 26.1-36.9l-43.4-252.9 183.7-179.1c5-4.9 8.3-11.3 9.3-18.3 2.7-17.5-9.5-33.7-27-36.3z"/></svg>',
  'truck': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M765 432h-48V304c0-17.7-14.3-32-32-32H192c-17.7 0-32 14.3-32 32v448c0 17.7 14.3 32 32 32h44c4.4 0 8-3.6 8-8V352h432v448c0 4.4 3.6 8 8 8h44c17.7 0 32-14.3 32-32V497l5-65z"/><path d="M696 752a64 64 0 10128 0 64 64 0 10-128 0zM296 752a64 64 0 10128 0 64 64 0 10-128 0z"/></svg>',
  'chart': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M888 792H200V168c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v688c0 4.4 3.6 8 8 8h752c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z"/><path d="M305.8 637.7c3.1 3.1 8.1 3.1 11.3 0l138.3-137.6L583 628.5c3.1 3.1 8.2 3.1 11.3 0l275.4-275.3c3.1-3.1 3.1-8.2 0-11.3l-39.6-39.6a8.03 8.03 0 00-11.3 0l-230 229.9L460.5 404a8.03 8.03 0 00-11.3 0L266.3 586.7a8.03 8.03 0 000 11.3l39.5 39.7z"/></svg>',
  'setting': '<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/><path d="M512 336c-97.2 0-176 78.8-176 176s78.8 176 176 176 176-78.8 176-176-78.8-176-176-176zm0 288c-61.9 0-112-50.1-112-112s50.1-112 112-112 112 50.1 112 112-50.1 112-112 112z"/></svg>'
};

const menuItems = computed(() => {
  if (userStore.userInfo?.role === 'MERCHANT') {
    return [
      { index: '/merchant/dashboard', label: t('merchantDash.workspace'), icon: svgIcons['dashboard'] },
      { index: '/merchant/products', label: t('adminLayout.productManagement'), icon: svgIcons['goods'] },
      { index: '/merchant/orders', label: t('merchantOrder.title'), icon: svgIcons['sell'] },
      { index: '/merchant/inventory', label: t('merchantInventory.title'), icon: svgIcons['inventory'] },
      { index: '/merchant/reviews', label: t('merchantReview.title'), icon: svgIcons['star'] },
      { index: '/merchant/delivery', label: t('merchantDelivery.title'), icon: svgIcons['truck'] },
      { index: '/merchant/statistics', label: t('merchantStats.title'), icon: svgIcons['chart'] },
      { index: '/merchant/settings', label: t('merchantSettings.title'), icon: svgIcons['setting'] }
    ];
  }

  return [
    { index: '/admin/dashboard', label: t('admin.dashboard'), icon: svgIcons['dashboard'] },
    { index: '/admin/products', label: t('admin.products'), icon: svgIcons['goods'] },
    { index: '/admin/orders', label: t('admin.orders'), icon: svgIcons['sell'] },
    { index: '/admin/users', label: t('admin.users'), icon: svgIcons['user'] },
    { index: '/admin/merchants', label: t('admin.merchants'), icon: svgIcons['building'] },
    { index: '/admin/categories', label: t('adminCategory.title'), icon: svgIcons['star'] },
    { index: '/admin/notices', label: t('adminNotice.title'), icon: svgIcons['chart'] },
    { index: '/admin/banners', label: t('adminBanner.title'), icon: svgIcons['truck'] },
    { index: '/admin/system', label: t('adminSystem.title'), icon: svgIcons['setting'] }
  ];
});

const toggleTheme = (val: string | number | boolean) => {
  if (Boolean(val)) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
};



const handleLogout = () => {
  userStore.logout();
  router.replace('/login');
};
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}
.aside {
  background-color: #1E1E2D;
  transition: width 0.3s;
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #E8567F;
    font-weight: bold;
    font-size: 18px;
  }
  .el-menu-vertical {
    border-right: none;
  }
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .left {
    .toggle-btn {
      font-size: 20px;
    }
  }
  .right {
    display: flex;
    align-items: center;
    gap: 15px;
  }
}

.username {
  color: #606266;
  font-size: 14px;
}
</style>
