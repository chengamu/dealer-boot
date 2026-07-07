import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface CustomerProfile {
  customerId?: number
  tenantId?: number
  merchantId?: number
  merchantName?: string
  customerName?: string
  companyName?: string
  email?: string
  phone?: string
  customerType?: string
  country?: string
  state?: string
  city?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  ownerUserId?: number
  ownerName?: string
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface CustomerOwnerOption {
  userId?: number
  userName?: string
  nickName?: string
}

export interface CustomerProfileQuery extends PageQuery {
  tenantId?: number | string
  merchantId?: number | string
  merchantName?: string
  customerName?: string
  companyName?: string
  email?: string
  ownerUserId?: number | string
  status?: string
}

export function listCustomers(query?: CustomerProfileQuery) {
  return requestPage<CustomerProfile>({ url: '/customer/customers/list', method: 'get', params: query })
}

export function listPlatformCustomers(query?: CustomerProfileQuery) {
  return requestPage<CustomerProfile>({ url: '/platform/customers/list', method: 'get', params: query })
}

export function listCustomerOwnerOptions() {
  return request<CustomerOwnerOption[]>({ url: '/customer/customers/owner-options', method: 'get' })
}

export function listPlatformCustomerOwnerOptions(tenantId: number | string) {
  return request<CustomerOwnerOption[]>({ url: '/platform/customers/owner-options', method: 'get', params: { tenantId } })
}

export function getCustomer(id: number | string) {
  return request<CustomerProfile>({ url: `/customer/customers/${id}`, method: 'get' })
}

export function getPlatformCustomer(id: number | string) {
  return request<CustomerProfile>({ url: `/platform/customers/${id}`, method: 'get' })
}

export function addCustomer(data: CustomerProfile) {
  return request({ url: '/customer/customers', method: 'post', data })
}

export function updateCustomer(data: CustomerProfile) {
  return request({ url: '/customer/customers', method: 'put', data })
}

export function deleteCustomer(ids: number | string | Array<number | string>) {
  return request({ url: `/customer/customers/${ids}`, method: 'delete' })
}

export function changeCustomerStatus(id: number | string, status: string) {
  return request({ url: `/customer/customers/change-status/${id}/${status}`, method: 'put' })
}
