import request from '@/utils/request'

export function submitMerchantApplication(data) {
  return request({
    url: '/merchant/applications',
    headers: {
      isToken: false
    },
    method: 'post',
    data
  })
}

export function listTenantApplications(query) {
  return request({
    url: '/system/tenant/applications',
    method: 'get',
    params: query
  })
}

export function getTenantApplication(id) {
  return request({
    url: `/system/tenant/applications/${id}`,
    method: 'get'
  })
}

export function approveTenantApplication(id) {
  return request({
    url: `/system/tenant/applications/${id}/approve`,
    method: 'post'
  })
}

export function rejectTenantApplication(id, data) {
  return request({
    url: `/system/tenant/applications/${id}/reject`,
    method: 'post',
    data
  })
}
