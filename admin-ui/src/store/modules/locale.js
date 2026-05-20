import { getLocale, setLocale } from '@/utils/auth'

const useLocaleStore = defineStore(
  'locale',
  {
    state: () => ({
      language: getLocale()
    }),
    actions: {
      setLanguage(language) {
        this.language = language
        setLocale(language)
      }
    }
  })

export default useLocaleStore
