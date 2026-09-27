import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { fileURLToPath } from 'node:url';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver, VantResolver } from 'unplugin-vue-components/resolvers';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const rootDir = fileURLToPath(new URL('.', import.meta.url));
  const backendTarget = env.VITE_BACKEND_TARGET || 'http://127.0.0.1:5303';
  const frontendPort = Number(env.FRONTEND_PORT || 5302);
  const proxy = {
    '/api': {
      target: backendTarget,
      changeOrigin: true
    },
    '/uploads': {
      target: backendTarget,
      changeOrigin: true
    }
  };
  return {
    plugins: [
      vue(),
      Components({
        dts: false,
        resolvers: [
          ElementPlusResolver({ importStyle: 'css' }),
          VantResolver({ importStyle: 'css' })
        ]
      })
    ],
    resolve: {
      alias: {
        '@': path.resolve(rootDir, 'src')
      }
    },
    server: {
      port: frontendPort,
      host: '0.0.0.0',
      strictPort: true,
      proxy
    },
    preview: {
      port: frontendPort,
      host: '0.0.0.0',
      strictPort: true,
      proxy
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/assets/styles/variables.scss" as *;`
        }
      }
    }
  };
});
