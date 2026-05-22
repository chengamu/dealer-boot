import Cookies from 'js-cookie'
import type { AppLocale } from '@/i18n'

const TokenKey = 'Admin-Token-MES'
const LocaleKey = 'Content-Language'

export const StringKey = 'MES'

function secureCookieOptions() {
  return {
    path: '/',
    sameSite: 'lax' as const,
    secure: typeof window !== 'undefined' && window.location.protocol === 'https:'
  }
}

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token: string) {
  return Cookies.set(TokenKey, token, secureCookieOptions())
}

export function removeToken() {
  return Cookies.remove(TokenKey, { path: '/' })
}

export function getLocale(): AppLocale {
  return (Cookies.get(LocaleKey) || 'zh_CN') as AppLocale
}

export function getLocaleCookie(): AppLocale {
  return getLocale()
}

export function setLocale(locale: AppLocale) {
  return Cookies.set(LocaleKey, locale, secureCookieOptions())
}

export function setLocaleCookie(locale: AppLocale) {
  return setLocale(locale)
}
