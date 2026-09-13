import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 端口必须是 5173：后端 GlobalMvcConfig 里 CORS 只放行了 http://localhost:5173
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    strictPort: true,
  },
})
