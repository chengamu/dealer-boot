/// <reference types="vite/client" />

declare interface Window {
  __APP_CONFIG__?: Record<string, string | boolean | number | undefined>
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}

declare module 'virtual:svg-icons-register'

declare module 'page-agent' {
  export class PageAgent {
    constructor(options: Record<string, unknown>)
    execute(task: string): Promise<unknown>
  }
}
