import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import createAutoImport from '../../admin-ui/vite/plugins/auto-import'
import createSvgIcon from '../../admin-ui/vite/plugins/svg-icon'
import createSetupExtend from '../../admin-ui/vite/plugins/setup-extend'

const adminRoot = fileURLToPath(new URL('../../admin-ui', import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, adminRoot)
  const baseApi = env.VITE_APP_BASE_API || '/dev-api'
  return {
    root: adminRoot,
    base: env.VITE_APP_CONTEXT_PATH || '/',
    plugins: [vue(), createAutoImport(), createSetupExtend(), createSvgIcon(mode === 'production')],
    resolve: {
      alias: { '@': fileURLToPath(new URL('../../admin-ui/src', import.meta.url)) },
      extensions: ['.ts', '.tsx', '.mjs', '.js', '.vue', '.json']
    },
    server: {
      port: 18083,
      host: true,
      proxy: {
        [baseApi]: {
          target: 'http://127.0.0.1:18081',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${baseApi}`), '')
        }
      }
    }
  }
})