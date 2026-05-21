import Cookies from 'js-cookie'

const TOKEN_KEY = 'Admin-Token-MES'
const LOCALE_KEY = 'Content-Language'

export const getToken = () => Cookies.get(TOKEN_KEY)
export const setToken = (token: string) => Cookies.set(TOKEN_KEY, token)
export const removeToken = () => Cookies.remove(TOKEN_KEY)

export const getLocaleCookie = () => Cookies.get(LOCALE_KEY) || 'zh_CN'
export const setLocaleCookie = (locale: string) => Cookies.set(LOCALE_KEY, locale)
export const getLocale = getLocaleCookie
export const setLocale = setLocaleCookie
export const StringKey = 'MES'
