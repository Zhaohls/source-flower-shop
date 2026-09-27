import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import NProgress from 'nprogress';
import { useUserStore } from '@/stores/user';

const APP_TITLE = '花店管理系统';

const resolveRoleHome = (role?: string) => {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard';
    case 'MERCHANT':
      return '/merchant/dashboard';
    default:
      return '/mall';
  }
};

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/mall',
    meta: { title: '首页' }
  },
  {
    path: '/mall',
    component: () => import('@/layouts/MallLayout.vue'),
    meta: { title: '商城首页' },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/mall/home.vue'),
        meta: { title: '商城首页' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/mall/products.vue'),
        meta: { title: '全部商品' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/mall/product-detail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/mall/cart.vue'),
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '购物车' }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/mall/checkout.vue'),
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '确认订单' }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('@/views/mall/orders.vue'),
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '我的订单' }
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/mall/address.vue'),
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '收货地址' }
      },
      {
        path: 'customization',
        name: 'Customization',
        redirect: '/mall/checkout',
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '确认订单' }
      },
      {
        path: 'account',
        name: 'MallAccount',
        component: () => import('@/views/mall/account.vue'),
        meta: { requiresAuth: true, role: 'CUSTOMER', title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard.vue'),
        meta: { title: '管理员仪表盘' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/product/index.vue'),
        meta: { title: '管理员商品管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/order/index.vue'),
        meta: { title: '管理员订单管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/user/index.vue'),
        meta: { title: '管理员用户管理' }
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/merchant/index.vue'),
        meta: { title: '管理员商家管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/category/index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/notice/index.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/admin/banner/index.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/system/index.vue'),
        meta: { title: '系统管理' }
      }
    ],
    meta: { requiresAuth: true, role: 'ADMIN', title: '管理后台' }
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/merchant/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/dashboard.vue'),
        meta: { title: '商家工作台' }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/product/index.vue'),
        meta: { title: '商家商品管理' }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/order/index.vue'),
        meta: { title: '商家订单管理' }
      },
      {
        path: 'inventory',
        name: 'MerchantInventory',
        component: () => import('@/views/merchant/inventory/index.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/review/index.vue'),
        meta: { title: '客户评价' }
      },
      {
        path: 'delivery',
        name: 'MerchantDelivery',
        component: () => import('@/views/merchant/delivery/index.vue'),
        meta: { title: '配送跟踪' }
      },
      {
        path: 'statistics',
        name: 'MerchantStatistics',
        component: () => import('@/views/merchant/statistics/index.vue'),
        meta: { title: '销售统计' }
      },
      {
        path: 'settings',
        name: 'MerchantSettings',
        component: () => import('@/views/merchant/settings/index.vue'),
        meta: { title: '店铺设置' }
      }
    ],
    meta: { requiresAuth: true, role: 'MERCHANT', title: '商家后台' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    // 404兜底路由
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面未找到' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// 全局前置守卫
router.beforeEach((to) => {
  NProgress.start();
  
  const userStore = useUserStore();
  const currentRole = userStore.userInfo?.role || userStore.roles[0];

  if (userStore.token && (to.path === '/login' || to.path === '/register')) {
    return resolveRoleHome(currentRole);
  }

  if (to.meta.requiresAuth && !userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  if (to.meta.role && currentRole && to.meta.role !== currentRole) {
    return resolveRoleHome(currentRole);
  }

  return true;
});

router.afterEach((to) => {
  NProgress.done();
  const pageTitle = typeof to.meta.title === 'string' ? to.meta.title : APP_TITLE;
  document.title = pageTitle ? `${pageTitle} - ${APP_TITLE}` : APP_TITLE;
});

export default router;
