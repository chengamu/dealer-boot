import { request, requestData, requestPage } from '@/utils/request'
import type { DecimalValue, PageQuery } from '@/types/api'

export const SALES_STORE_STATUS = {
  DISABLED: '0',
  ENABLED: '1'
} as const

export interface SalesStore {
  salesStoreId?: number
  tenantId?: number
  storeCode?: string
  storeName?: string
  deptId?: number
  deptName?: string
  contactName?: string
  contactPhone?: string
  country?: string
  state?: string
  city?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  address?: string
  currencyCode?: string
  creditLimit?: DecimalValue
  paymentTermDays?: number
  customerCount?: number
  unfinishedOrderCount?: number
  status?: string
  remark?: string
  updateTime?: string
  updateBy?: string
}

export interface SalesStoreQuery extends PageQuery {
  storeCode?: string
  storeName?: string
  deptId?: number | string
  status?: string
}

export interface SalesStoreDeptOption {
  deptId: number
  deptName: string
  linked: boolean
}

export interface SalesStoreReferenceCheck {
  customerCount: number
  unfinishedOrderCount: number
}

export function listSalesStores(query?: SalesStoreQuery) {
  return requestPage<SalesStore>({
    url: '/system/sales-store/list',
    method: 'get',
    params: query
  })
}

export function getSalesStore(salesStoreId: number | string) {
  return requestData<SalesStore>({
    url: `/system/sales-store/${salesStoreId}`,
    method: 'get'
  })
}

export function createSalesStore(data: SalesStore) {
  return request({
    url: '/system/sales-store',
    method: 'post',
    data
  })
}

export function updateSalesStore(data: SalesStore) {
  return request({
    url: '/system/sales-store',
    method: 'put',
    data
  })
}

export function listSalesStoreOptions() {
  return requestData<SalesStore[]>({
    url: '/system/sales-store/options',
    method: 'get'
  })
}

export function listSalesStoreDeptOptions() {
  return requestData<SalesStoreDeptOption[]>({
    url: '/system/sales-store/dept-options',
    method: 'get'
  })
}

export function getSalesStoreDisableCheck(salesStoreId: number | string) {
  return requestData<SalesStoreReferenceCheck>({
    url: `/system/sales-store/${salesStoreId}/disable-check`,
    method: 'get'
  })
}

export function changeSalesStoreStatus(salesStoreId: number | string, status: string) {
  return request({
    url: `/system/sales-store/change-status/${salesStoreId}/${status}`,
    method: 'put'
  })
}
