import { createI18n } from 'vue-i18n'
import zhCN from '@/locales/zh_CN'
import enUS from '@/locales/en_US'
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
  { labelKey: 'language.zhCN', value: 'zh_CN' },
  { labelKey: 'language.enUS', value: 'en_US' }
] as const
