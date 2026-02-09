import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      '@styles': fileURLToPath(new URL('./src/styles', import.meta.url)),
    },
  },
  plugins: [react()],
})
