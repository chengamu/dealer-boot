import { createI18n } from 'vue-i18n'
import { getLocaleCookie } from '@/utils/auth'

export type AppLocale = 'zh_CN' | 'en_US'
export type LocaleMessages = Record<string, string>

const fallbackLocale: AppLocale = 'en_US'
const loadedMessages: Partial<Record<AppLocale, LocaleMessages>> = {}

function sanitizeVueI18nMessages(messages: LocaleMessages) {
  const keys = new Set(Object.keys(messages))
  return Object.fromEntries(Object.entries(messages).filter(([key]) => {
    const parts = key.split('.')
    return !parts.some((_, index) => index > 0 && keys.has(parts.slice(0, index).join('.')))
  })) as LocaleMessages
}

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
  i18n.global.setLocaleMessage(locale, sanitizeVueI18nMessages(messages))
  return messages
}

function setEmptyLocaleMessages(locale: AppLocale) {
  const messages: LocaleMessages = {}
  loadedMessages[locale] = messages
  i18n.global.setLocaleMessage(locale, sanitizeVueI18nMessages(messages))
  return messages
}

async function loadLocaleMessagesSafely(locale: AppLocale) {
  try {
    return await loadLocaleMessages(locale)
  } catch (error) {
    console.warn(error)
    return undefined
  }
}

async function ensureFallbackMessages() {
  const messages = await loadLocaleMessagesSafely(fallbackLocale)
  return messages || loadedMessages[fallbackLocale] || setEmptyLocaleMessages(fallbackLocale)
}

function applyI18nLanguage(locale: AppLocale) {
  i18n.global.locale.value = locale
  document.documentElement.lang = locale === 'zh_CN' ? 'zh-CN' : 'en'
}

export async function setI18nLanguage(locale: AppLocale) {
  const currentLocale = normalizeLocale(locale)
  const messages = await loadLocaleMessagesSafely(currentLocale)
  if (messages) {
    applyI18nLanguage(currentLocale)
    return
  }
  await ensureFallbackMessages()
  applyI18nLanguage(fallbackLocale)
}

export async function setupI18n(locale: AppLocale = normalizeLocale(getLocaleCookie())) {
  const currentLocale = normalizeLocale(locale)
  const [fallbackMessages, currentMessages] = await Promise.all([
    ensureFallbackMessages(),
    currentLocale === fallbackLocale ? Promise.resolve() : loadLocaleMessagesSafely(currentLocale)
  ])
  if (currentMessages) {
    applyI18nLanguage(currentLocale)
    return
  }
  if (!fallbackMessages) {
    setEmptyLocaleMessages(fallbackLocale)
  }
  applyI18nLanguage(fallbackLocale)
}
