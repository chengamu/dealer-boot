import { request } from '@/utils/request'

export interface StandardCountry {
  countryId?: number
  countryCode: string
  name: string
  nameEn: string
  nameZh: string
}

export interface StandardCurrency {
  currencyId?: number
  currencyCode: string
  name: string
  nameEn: string
  nameZh: string
  symbol?: string
  decimalPlaces?: number
}

export interface StandardLanguage {
  languageId?: number
  languageCode: string
  name: string
  nameEn: string
  nameNative: string
}

export function listCountries(keyword?: string) {
  return request<StandardCountry[]>({
    url: '/system/standard/countries',
    method: 'get',
    params: { keyword }
  })
}

export function listCurrencies(keyword?: string) {
  return request<StandardCurrency[]>({
    url: '/system/standard/currencies',
    method: 'get',
    params: { keyword }
  })
}

export function listLanguages(keyword?: string) {
  return request<StandardLanguage[]>({
    url: '/system/standard/languages',
    method: 'get',
    params: { keyword }
  })
}
