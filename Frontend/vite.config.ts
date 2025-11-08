import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(() => {
  // Vite automatically loads .env files and injects variables prefixed with VITE_
  // We read VITE_BACKEND_PORT to decide the proxy target.
  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: process.env.VITE_BACKEND_PORT ? `http://localhost:${process.env.VITE_BACKEND_PORT}` : 'http://localhost',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path,
        },
      },
    },
    // Apply same proxy for preview mode (vite preview)
    preview: {
      proxy: {
        '/api': {
          target: process.env.VITE_BACKEND_PORT ? `http://localhost:${process.env.VITE_BACKEND_PORT}` : 'http://localhost',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
