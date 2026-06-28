import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

export function useAiSettingsI18n() {
  const localeStore = useLocaleStore()
  return (key: string, params?: Record<string, string | number>) => {
    const message = getMessage(key, localeStore.language)
    if (!params) return message
    return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
  }
}
