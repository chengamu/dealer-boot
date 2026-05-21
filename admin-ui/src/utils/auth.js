import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token-MES'
const LocaleKey = 'Content-Language'

export const StringKey = 'MES'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getLocale() {
  return Cookies.get(LocaleKey) || 'zh_CN'
}

export function setLocale(locale) {
  return Cookies.set(LocaleKey, locale, { path: '/' })
}
