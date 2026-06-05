import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'
import type { SysUser, UserDetail, UserQuery } from '@/api/system/user'

function parseStrEmpty(value?: number | string) {
  return value === undefined || value === null || value === '' ? '' : value
}

export type MerchantUserQuery = UserQuery & PageQuery

export function listMerchantUser(query?: MerchantUserQuery) {
  return requestPage<SysUser>({
    url: '/merchant/user/list',
    method: 'get',
    params: query
  })
}

export function getMerchantUser(userId?: number | string) {
  return request<UserDetail>({
    url: `/merchant/user/${parseStrEmpty(userId)}`,
    method: 'get'
  })
}

export function addMerchantUser(data: SysUser) {
  return request({
    url: '/merchant/user',
    method: 'post',
    data
  })
}

export function updateMerchantUser(data: SysUser) {
  return request({
    url: '/merchant/user',
    method: 'put',
    data
  })
}

export function delMerchantUser(userId: number | string | Array<number | string>) {
  return request({
    url: `/merchant/user/${userId}`,
    method: 'delete'
  })
}

export function resetMerchantUserPwd(userId: number | string, password: string) {
  return request({
    url: '/merchant/user/resetPwd',
    method: 'put',
    data: {
      userId,
      password
    }
  })
}

export function changeMerchantUserStatus(userId: number | string, status: string) {
  return request({
    url: '/merchant/user/changeStatus',
    method: 'put',
    data: {
      userId,
      status
    }
  })
}
