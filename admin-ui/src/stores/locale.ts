import { defineStore } from 'pinia'
import { setI18nLanguage, type AppLocale } from '@/i18n'
import { getLocaleCookie, setLocaleCookie } from '@/utils/auth'

export const useLocaleStore = defineStore('locale', {
  state: () => ({
    locale: getLocaleCookie() as AppLocale
  }),
  getters: {
    language: (state) => state.locale
  },
  actions: {
    async setLocale(locale: AppLocale) {
      this.locale = locale
      await setI18nLanguage(locale)
      setLocaleCookie(locale)
    },
    async setLanguage(locale: AppLocale) {
      await this.setLocale(locale)
    }
  }
})

export default useLocaleStore
