import axios from 'axios';
import { ElMessage } from 'element-plus';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { useUserStore } from '@/stores/user';
import router from '@/router';

// 创建 Axios 实例
const apiBaseURL = import.meta.env.DEV ? '/api' : import.meta.env.VITE_API_BASE_URL || '/api';

const request = axios.create({
  baseURL: apiBaseURL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 配置 NProgress 进度条
NProgress.configure({ showSpinner: false });

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    NProgress.start();
    const userStore = useUserStore();
    const token = userStore.token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    NProgress.done();
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    NProgress.done();
    const res = response.data;
    
    // 检查自定义业务状态码，通常依赖 HTTP 状态码
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '操作出错');
      if (res.code === 401) {
        useUserStore().logout();
        router.push('/login');
      }
      return Promise.reject(new Error(res.message || '操作出错'));
    }
    return res;
  },
  (error) => {
    NProgress.done();
    let message = error.message;
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录';
          useUserStore().logout();
          router.push('/login');
          break;
        case 403:
          message = '拒绝访问，权限不足';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `网络错误: ${error.response.status}`;
      }
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default request;
