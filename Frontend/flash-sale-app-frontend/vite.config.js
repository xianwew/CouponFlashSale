import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import vueDevTools from 'vite-plugin-vue-devtools';
import { fileURLToPath, URL } from 'url';

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/ws': {
        target: process.env.VITE_API_BASE_WS_URL || 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      }
    }
  },
  define: {
    global: {}, // Ensures global is defined
  }
});
