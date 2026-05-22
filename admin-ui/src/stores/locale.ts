import { defineStore } from 'pinia'
import { i18n, type AppLocale } from '@/i18n'
import { getLocaleCookie, setLocaleCookie } from '@/utils/auth'

export const useLocaleStore = defineStore('locale', {
  state: () => ({
    locale: getLocaleCookie() as AppLocale
  }),
  getters: {
    language: (state) => state.locale
  },
  actions: {
    setLocale(locale: AppLocale) {
      this.locale = locale
      i18n.global.locale.value = locale
      document.documentElement.lang = locale === 'zh_CN' ? 'zh-CN' : 'en'
      setLocaleCookie(locale)
    },
    setLanguage(locale: AppLocale) {
      this.setLocale(locale)
    }
  }
})

export default useLocaleStore
