import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface MerchantLevel {
  levelId?: number
  levelCode?: string
  levelName?: string
  defaultDiscountRate?: number
  defaultCreditLimit?: number
  defaultFlag?: boolean
  sortOrder?: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface MerchantLevelQuery extends PageQuery {
  levelCode?: string
  levelName?: string
  status?: string
}

export function listMerchantLevels(query?: MerchantLevelQuery) {
  return requestPage<MerchantLevel>({ url: '/merchant/levels/list', method: 'get', params: query })
}

export function optionsMerchantLevels(query?: MerchantLevelQuery) {
  return request<MerchantLevel[]>({ url: '/merchant/levels/options', method: 'get', params: query })
}

export function getMerchantLevel(id: number | string) {
  return request<MerchantLevel>({ url: `/merchant/levels/${id}`, method: 'get' })
}

export function addMerchantLevel(data: MerchantLevel) {
  return request({ url: '/merchant/levels', method: 'post', data })
}

export function updateMerchantLevel(data: MerchantLevel) {
  return request({ url: '/merchant/levels', method: 'put', data })
}

export function deleteMerchantLevel(ids: number | string | Array<number | string>) {
  return request({ url: `/merchant/levels/${ids}`, method: 'delete' })
}

export function changeMerchantLevelStatus(id: number | string, status: string) {
  return request({ url: `/merchant/levels/change-status/${id}/${status}`, method: 'put' })
}
