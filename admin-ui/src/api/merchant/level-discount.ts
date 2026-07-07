import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface MerchantLevelDiscount {
  discountId?: number
  levelId?: number
  levelCode?: string
  levelName?: string
  categoryId?: number
  categoryCode?: string
  categoryNameCn?: string
  productTypeCode?: string
  productTypeNameCn?: string
  discountRate?: number
  sortOrder?: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface MerchantLevelDiscountQuery extends PageQuery {
  levelId?: number | string
  levelName?: string
  categoryId?: number | string
  productTypeCode?: string
  status?: string
}

export function listMerchantLevelDiscounts(query?: MerchantLevelDiscountQuery) {
  return requestPage<MerchantLevelDiscount>({ url: '/merchant/level-discounts/list', method: 'get', params: query })
}

export function getMerchantLevelDiscount(id: number | string) {
  return request<MerchantLevelDiscount>({ url: `/merchant/level-discounts/${id}`, method: 'get' })
}

export function addMerchantLevelDiscount(data: MerchantLevelDiscount) {
  return request({ url: '/merchant/level-discounts', method: 'post', data })
}

export function updateMerchantLevelDiscount(data: MerchantLevelDiscount) {
  return request({ url: '/merchant/level-discounts', method: 'put', data })
}

export function deleteMerchantLevelDiscount(ids: number | string | Array<number | string>) {
  return request({ url: `/merchant/level-discounts/${ids}`, method: 'delete' })
}

export function changeMerchantLevelDiscountStatus(id: number | string, status: string) {
  return request({ url: `/merchant/level-discounts/change-status/${id}/${status}`, method: 'put' })
}
