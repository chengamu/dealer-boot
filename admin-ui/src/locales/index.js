import zhCN from './zh_CN'
import enUS from './en_US'
import { getLocale } from '@/utils/auth'

const messages = {
  zh_CN: zhCN,
  en_US: enUS
}

export function getMessage(path, locale = getLocale()) {
  return path.split('.').reduce((value, key) => value && value[key], messages[locale]) || path
}

export function installLocale(app) {
  app.config.globalProperties.$t = getMessage
}
