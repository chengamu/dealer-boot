import { request } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface MerchantApplication {
  applyId?: number
  tenantId?: number
  merchantName?: string
  companyName?: string
  contactFirstName?: string
  contactLastName?: string
  contactName?: string
  email?: string
  officePhone?: string
  mobilePhone?: string
  country?: string
  state?: string
  city?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  remark?: string
  status?: string
  auditBy?: string
  auditTime?: string
  rejectReason?: string
  createTime?: string
}

export interface MerchantApplicationQuery extends PageQuery {
  applyId?: number | string
  merchantName?: string
  companyName?: string
  email?: string
  status?: string
}

export type MerchantApplicationPayload = Omit<MerchantApplication, 'applyId' | 'tenantId' | 'status' | 'auditBy' | 'auditTime' | 'createTime'>

export function submitMerchantApplication(data: MerchantApplicationPayload) {
  return request({
    url: '/merchant/applications',
    method: 'post',
    headers: { isToken: false },
    data
  })
}

export function listMerchantApplications(query?: MerchantApplicationQuery) {
  return request({
    url: '/system/tenant/applications',
    method: 'get',
    params: query
  }) as unknown as Promise<{ rows?: MerchantApplication[]; total?: number }>
}

export function getMerchantApplication(applyId: number | string) {
  return request<MerchantApplication>({
    url: `/system/tenant/applications/${applyId}`,
    method: 'get'
  })
}

export function approveMerchantApplication(applyId: number | string) {
  return request({
    url: `/system/tenant/applications/${applyId}/approve`,
    method: 'post'
  })
}

export function rejectMerchantApplication(applyId: number | string, rejectReason?: string) {
  return request({
    url: `/system/tenant/applications/${applyId}/reject`,
    method: 'post',
    data: { rejectReason }
  })
}
