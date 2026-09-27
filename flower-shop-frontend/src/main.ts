import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import pinia from './stores';
import i18n from './i18n';
import 'element-plus/es/components/message/style/css';
import 'vant/es/toast/style';

// Global Styles
import '@/assets/styles/global.scss';

const app = createApp(App);

app.use(pinia);
app.use(router);
app.use(i18n);

app.mount('#app');
