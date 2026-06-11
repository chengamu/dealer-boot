import { defineStore } from 'pinia'
import defaultSettings from '@/settings'

type SettingValue = string | boolean
type SettingKey = 'theme' | 'sideTheme' | 'showSettings' | 'topNav' | 'tagsView' | 'fixedHeader' | 'sidebarLogo' | 'dynamicTitle'

interface DefaultSettings {
  title: string
  theme: string
  sideTheme: string
  showSettings: boolean
  topNav: boolean
  tagsView: boolean
  fixedHeader: boolean
  sidebarLogo: boolean
  dynamicTitle: boolean
}

interface StorageSettings {
  theme?: string
  sideTheme?: string
  topNav?: boolean
  tagsView?: boolean
  fixedHeader?: boolean
  sidebarLogo?: boolean
  dynamicTitle?: boolean
}

function readStorageSettings(): StorageSettings {
  const raw = localStorage.getItem('layout-setting')
  if (!raw) return {}
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

const defaults = defaultSettings as DefaultSettings
const storageSetting = readStorageSettings()

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    title: '',
    theme: storageSetting.theme || defaults.theme,
    sideTheme: storageSetting.sideTheme || defaults.sideTheme,
    showSettings: defaults.showSettings,
    topNav: storageSetting.topNav === undefined ? defaults.topNav : storageSetting.topNav,
    tagsView: defaults.tagsView,
    fixedHeader: storageSetting.fixedHeader === undefined ? defaults.fixedHeader : storageSetting.fixedHeader,
    sidebarLogo: storageSetting.sidebarLogo === undefined ? defaults.sidebarLogo : storageSetting.sidebarLogo,
    dynamicTitle: storageSetting.dynamicTitle === undefined ? defaults.dynamicTitle : storageSetting.dynamicTitle
  }),
  actions: {
    changeSetting({ key, value }: { key: SettingKey; value: SettingValue }) {
      if (Object.prototype.hasOwnProperty.call(this.$state, key)) {
        ;(this.$state[key] as SettingValue) = value
      }
    },
    setTitle(title: string) {
      this.title = title
      document.title = this.dynamicTitle ? `${this.title} - ${defaults.title}` : defaults.title
    }
  }
})

export default useSettingsStore
