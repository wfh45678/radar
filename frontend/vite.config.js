import { fileURLToPath } from 'node:url';
import path from 'node:path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// dev 代理对齐原 webpack devServer：3000 -> localhost:6580
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    host: 'localhost',
    proxy: {
      '/services': {
        // 可用环境变量覆盖联调目标，如 RADAR_API_TARGET=http://测试服务器:6580
        target: process.env.RADAR_API_TARGET || 'http://localhost:6580',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'static',
  },
});
