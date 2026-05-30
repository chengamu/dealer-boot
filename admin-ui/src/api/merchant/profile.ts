import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface MerchantProfile {
  merchantId?: number
  tenantId?: number
  merchantName?: string
  companyName?: string
  contactFirstName?: string
  contactLastName?: string
  contactName?: string
  primaryEmail?: string
  officePhone?: string
  mobilePhone?: string
  country?: string
  state?: string
  city?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  status?: string
  auditStatus?: string
  auditBy?: string
  auditTime?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface MerchantProfileQuery extends PageQuery {
  merchantId?: number | string
  tenantId?: number | string
  merchantName?: string
  companyName?: string
  primaryEmail?: string
  status?: string
}

export function listMerchantProfiles(query?: MerchantProfileQuery) {
  return requestPage<MerchantProfile>({
    url: '/system/merchant/profile/list',
    method: 'get',
    params: query
  })
}

export function getMerchantProfile(merchantId: number | string) {
  return request<MerchantProfile>({
    url: `/system/merchant/profile/${merchantId}`,
    method: 'get'
  })
}

export function updateMerchantProfile(data: MerchantProfile) {
  return request({
    url: '/system/merchant/profile',
    method: 'put',
    data
  })
}

export function getCurrentMerchantProfile() {
  return request<MerchantProfile>({
    url: '/system/merchant/profile/current',
    method: 'get'
  })
}

export function updateCurrentMerchantProfile(data: MerchantProfile) {
  return request({
    url: '/system/merchant/profile/current',
    method: 'put',
    data
  })
}
