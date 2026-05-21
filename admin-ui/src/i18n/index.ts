import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh_CN'
import enUS from './locales/en_US'
import { getLocaleCookie } from '@/utils/auth'

export type AppLocale = 'zh_CN' | 'en_US'

export const messages = {
  zh_CN: zhCN,
  en_US: enUS
}

export const i18n = createI18n({
  legacy: false,
  locale: getLocaleCookie() as AppLocale,
  fallbackLocale: 'en_US',
  messages,
  missingWarn: false,
  fallbackWarn: false
})

export const localeOptions = [
  { label: '中文', value: 'zh_CN' },
  { label: 'English', value: 'en_US' }
] as const
