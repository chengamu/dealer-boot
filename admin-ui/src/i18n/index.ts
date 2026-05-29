import { createI18n } from 'vue-i18n'
import { getLocaleCookie } from '@/utils/auth'

export type AppLocale = 'zh_CN' | 'en_US'
export type LocaleMessages = Record<string, string>

const fallbackLocale: AppLocale = 'en_US'
const loadedMessages: Partial<Record<AppLocale, LocaleMessages>> = {}

export const i18n = createI18n({
  legacy: false,
  locale: getLocaleCookie() as AppLocale,
  fallbackLocale,
  messages: {},
  flatJson: true,
  missingWarn: false,
  fallbackWarn: false
})

export const localeOptions = [
  { labelKey: 'language.zhCN', value: 'zh_CN' },
  { labelKey: 'language.enUS', value: 'en_US' }
] as const

export function normalizeLocale(locale?: string): AppLocale {
  return locale === 'en_US' ? 'en_US' : 'zh_CN'
}

export function getCachedLocaleMessage(locale: string): LocaleMessages | undefined {
  return loadedMessages[normalizeLocale(locale)]
}

export async function loadLocaleMessages(locale: AppLocale) {
  if (loadedMessages[locale]) {
    return loadedMessages[locale]
  }

  const baseUrl = import.meta.env.BASE_URL || '/'
  const response = await fetch(`${baseUrl}i18n/${locale}.json`, { cache: 'no-cache' })
  if (!response.ok) {
    throw new Error(`Failed to load i18n locale ${locale}: ${response.status}`)
  }
  const messages = await response.json() as LocaleMessages
  loadedMessages[locale] = messages
  i18n.global.setLocaleMessage(locale, messages)
  return messages
}

export async function setI18nLanguage(locale: AppLocale) {
  await loadLocaleMessages(locale)
  i18n.global.locale.value = locale
  document.documentElement.lang = locale === 'zh_CN' ? 'zh-CN' : 'en'
}

export async function setupI18n(locale: AppLocale = normalizeLocale(getLocaleCookie())) {
  const currentLocale = normalizeLocale(locale)
  await Promise.all([
    loadLocaleMessages(fallbackLocale),
    currentLocale === fallbackLocale ? Promise.resolve() : loadLocaleMessages(currentLocale)
  ])
  await setI18nLanguage(currentLocale)
}
