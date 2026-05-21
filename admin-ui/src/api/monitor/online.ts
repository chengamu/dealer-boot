import { request, requestPage } from '@/utils/request'

export interface OnlineUserQuery {
  ipaddr?: string
  userName?: string
}

export interface OnlineUser {
  tokenId: string
  userName?: string
  deptName?: string
  ipaddr?: string
  loginLocation?: string
  os?: string
  browser?: string
  loginTime?: string
}

export function list(query: OnlineUserQuery) {
  return requestPage<OnlineUser>({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  })
}

export function forceLogout(tokenId: string) {
  return request({
    url: `/monitor/online/${tokenId}`,
    method: 'delete'
  })
}
