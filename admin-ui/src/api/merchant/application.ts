import { request, requestPage } from '@/utils/request'
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
  verificationCode?: string
  termsAccepted?: boolean
}

export interface MerchantApplicationQuery extends PageQuery {
  applyId?: number | string
  merchantName?: string
  companyName?: string
  email?: string
  status?: string
}

export type MerchantApplicationPayload = Omit<MerchantApplication, 'applyId' | 'tenantId' | 'status' | 'auditBy' | 'auditTime' | 'createTime'>

export interface MerchantEmailCodePayload {
  email: string
}

export function sendMerchantApplicationEmailCode(data: MerchantEmailCodePayload) {
  return request({
    url: '/merchant/applications/email-code',
    method: 'post',
    headers: { isToken: false },
    data
  })
}

export function submitMerchantApplication(data: MerchantApplicationPayload) {
  return request({
    url: '/merchant/applications',
    method: 'post',
    headers: { isToken: false },
    data
  })
}

export function listMerchantApplications(query?: MerchantApplicationQuery) {
  return requestPage<MerchantApplication>({
    url: '/system/tenant/applications',
    method: 'get',
    params: query
  })
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
