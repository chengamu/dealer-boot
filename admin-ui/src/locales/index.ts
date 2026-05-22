import type { App } from 'vue'
import type { AppLocale } from '@/i18n'
import zhCN from './zh_CN'
import enUS from './en_US'
import { getLocale } from '@/utils/auth'

interface MessageTree {
  [key: string]: string | MessageTree
}

const messages: Record<AppLocale, MessageTree> = {
  zh_CN: zhCN,
  en_US: enUS
}

function normalizeLocale(locale: string): AppLocale {
  return locale === 'en_US' ? 'en_US' : 'zh_CN'
}

export function getMessage(path: string, locale: string = getLocale()) {
  const currentLocale = normalizeLocale(locale)
  const message = path.split('.').reduce<string | MessageTree | undefined>((value, key) => {
    return value && typeof value === 'object' ? value[key] : undefined
  }, messages[currentLocale])
  return typeof message === 'string' ? message : path
}

export function installLocale(app: App) {
  ;(app.config.globalProperties as Record<string, unknown>).$t = getMessage
}

export function useLocale() {
  return {
    t: getMessage
  }
}
