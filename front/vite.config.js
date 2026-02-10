import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'

const proxyTarget = process.env.VITE_PROXY_TARGET || 'http://localhost:9090'

// https://vite.dev/config/
export default defineConfig({
  server: {
    host: true,
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: proxyTarget,
        changeOrigin: true,
      },
      '/images': {
        target: proxyTarget,
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      '@styles': fileURLToPath(new URL('./src/styles', import.meta.url)),
    },
  },
  plugins: [react()],
})
