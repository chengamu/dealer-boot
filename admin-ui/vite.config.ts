import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import createAutoImport from './vite/plugins/auto-import'
import createSvgIcon from './vite/plugins/svg-icon'
import createSetupExtend from './vite/plugins/setup-extend'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const baseApi = env.VITE_APP_BASE_API || '/dev-api'
  const aiRuntimeApi = '/ai-runtime'

  return {
    base: env.VITE_APP_CONTEXT_PATH || '/',
    plugins: [vue(), createAutoImport(), createSetupExtend(), createSvgIcon(mode === 'production')],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
      extensions: ['.ts', '.tsx', '.mjs', '.js', '.vue', '.json']
    },
    server: {
      port: 8083,
      host: true,
      proxy: {
        [baseApi]: {
          target: 'http://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${baseApi}`), '')
        },
        [aiRuntimeApi]: {
          target: env.VITE_AI_RUNTIME_PROXY_TARGET || 'http://127.0.0.1:18088',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${aiRuntimeApi}`), '')
        }
      }
    },
    build: {
      outDir: 'dist',
      chunkSizeWarningLimit: 1100,
      rollupOptions: {
        onwarn(warning, warn) {
          if (warning.code === 'INVALID_ANNOTATION') return
          warn(warning)
        },
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia', 'vue-i18n'],
            element: ['element-plus', '@element-plus/icons-vue'],
            network: ['axios', 'qs', 'js-cookie'],
            charts: ['echarts/core', 'echarts/charts', 'echarts/components', 'echarts/renderers'],
            editor: ['@vueup/vue-quill'],
            media: ['vue-cropper']
          }
        }
      }
    }
  }
})
