import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token-MES'
const TenantIdKey = 'Tenant-Id'
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

export function getTenantId() {
  return Cookies.get(TenantIdKey) || '0'
}

export function setTenantId(tenantId) {
  return Cookies.set(TenantIdKey, tenantId)
}

export function removeTenantId() {
  return Cookies.remove(TenantIdKey)
}

export function getLocale() {
  return Cookies.get(LocaleKey) || 'zh_CN'
}

export function setLocale(locale) {
  return Cookies.set(LocaleKey, locale)
}
