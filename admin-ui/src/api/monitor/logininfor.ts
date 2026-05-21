import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface LoginInfoQuery extends PageQuery {
  ipaddr?: string
  userName?: string
  status?: string
  orderByColumn?: string
  isAsc?: string
  params?: {
    beginTime?: string
    endTime?: string
  }
}

export interface LoginInfo {
  infoId: number
  userName?: string
  ipaddr?: string
  loginLocation?: string
  os?: string
  browser?: string
  status?: string
  msg?: string
  loginTime?: string
}

export function list(query: LoginInfoQuery) {
  return requestPage<LoginInfo>({
    url: '/monitor/logininfor/list',
    method: 'get',
    params: query
  })
}

export function delLogininfor(infoId: number | string | Array<number | string>) {
  return request({
    url: `/monitor/logininfor/${infoId}`,
    method: 'delete'
  })
}

export function unlockLogininfor(userName: string) {
  return request({
    url: `/monitor/logininfor/unlock/${userName}`,
    method: 'get'
  })
}

export function cleanLogininfor() {
  return request({
    url: '/monitor/logininfor/clean',
    method: 'delete'
  })
}
