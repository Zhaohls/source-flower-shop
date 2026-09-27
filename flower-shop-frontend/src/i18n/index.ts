import { createI18n } from 'vue-i18n';
import zhCN from './locales/zh-CN';

const i18n = createI18n({
  legacy: false, // Composition API 模式
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN
  }
});

export default i18n;
