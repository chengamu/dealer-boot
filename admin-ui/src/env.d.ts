/// <reference types="vite/client" />

declare interface Window {
  __APP_CONFIG__?: Record<string, string>
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}

declare module 'virtual:svg-icons-register'
declare module '@/api/*'
declare module '@/api/system/*'
declare module '@/api/system/dict/*'
declare module '@/api/monitor/*'
declare module '@/api/tool/*'
declare module '@/api/**/*.js'
declare module '@/components/SvgIcon/svgicon'
declare module '@/directive'
declare module '@/locales'
declare module '@/plugins'
declare module '@/settings'
declare module '@/utils/dict'
declare module '@/utils/ruoyi'
