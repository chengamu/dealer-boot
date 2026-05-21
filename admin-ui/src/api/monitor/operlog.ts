import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface OperLogQuery extends PageQuery {
  operIp?: string
  title?: string
  operName?: string
  businessType?: string
  status?: string
  orderByColumn?: string
  isAsc?: string
  params?: {
    beginTime?: string
    endTime?: string
  }
}

export interface OperLog {
  operId: number
  title?: string
  businessType?: string | number
  requestMethod?: string
  operName?: string
  deptName?: string
  operIp?: string
  operLocation?: string
  status?: string | number
  operTime?: string
  costTime?: number
  operUrl?: string
  method?: string
  operParam?: string
  jsonResult?: string
  errorMsg?: string
}

export function list(query: OperLogQuery) {
  return requestPage<OperLog>({
    url: '/monitor/operlog/list',
    method: 'get',
    params: query
  })
}

export function delOperlog(operId: number | string | Array<number | string>) {
  return request({
    url: `/monitor/operlog/${operId}`,
    method: 'delete'
  })
}

export function cleanOperlog() {
  return request({
    url: '/monitor/operlog/clean',
    method: 'delete'
  })
}
