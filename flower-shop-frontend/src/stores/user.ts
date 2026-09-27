import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(null);
  const userInfo = ref<any>(null);
  const roles = ref<string[]>([]);

  const setToken = (newToken: string) => {
    token.value = newToken;
  };
  
  const setUserInfo = (info: any) => {
    userInfo.value = info;
    roles.value = Array.isArray(info?.roles) ? info.roles : info?.role ? [info.role] : [];
  };

  const logout = () => {
    token.value = null;
    userInfo.value = null;
    roles.value = [];
    localStorage.removeItem('user');
  };

  return { token, userInfo, roles, setToken, setUserInfo, logout };
}, {
  persist: true // pinia-plugin-persistedstate configuration
});
