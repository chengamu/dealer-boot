import defaultSettings from '@/settings'
import useSettingsStore from '@/stores/settings'

export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  if (settingsStore.dynamicTitle) {
    document.title = `${settingsStore.title} - ${defaultSettings.title}`
  } else {
    document.title = defaultSettings.title
  }
}
