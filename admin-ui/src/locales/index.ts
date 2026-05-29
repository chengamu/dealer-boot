import type { App } from 'vue'
import { getCachedLocaleMessage, normalizeLocale, type AppLocale } from '@/i18n'
import { getLocale } from '@/utils/auth'

function resolveMessage(messages: Record<string, unknown> | undefined, path: string): string | undefined {
  const flatValue = messages?.[path]
  if (typeof flatValue === 'string') return flatValue

  let current: unknown = messages
  for (const segment of path.split('.')) {
    if (!current || typeof current !== 'object') return undefined
    current = (current as Record<string, unknown>)[segment]
  }
  return typeof current === 'string' ? current : undefined
}

export function getMessage(path: string, locale: string = getLocale()) {
  const currentLocale = normalizeLocale(locale)
  return resolveMessage(getCachedLocaleMessage(currentLocale), path)
    ?? resolveMessage(getCachedLocaleMessage('en_US'), path)
    ?? path
}

export function installLocale(app: App) {
  ;(app.config.globalProperties as Record<string, unknown>).$t = getMessage
}

export function useLocale() {
  return {
    t: getMessage
  }
}
