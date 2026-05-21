import { request } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface DictTypeQuery extends PageQuery {
  dictName?: string
  dictType?: string
  status?: string
  beginTime?: string
  endTime?: string
}

export interface DictType {
  dictId?: number
  dictName?: string
  dictType?: string
  status?: string
  remark?: string
  createTime?: string
}

export function listType(query?: DictTypeQuery) {
  return request({
    url: '/system/dict/type/list',
    method: 'get',
    params: query
  }) as Promise<{ rows?: DictType[]; total?: number }>
}

export function getType(dictId: number | string | Array<number | string>) {
  return request<DictType>({
    url: `/system/dict/type/${dictId}`,
    method: 'get'
  })
}

export function addType(data: DictType) {
  return request({
    url: '/system/dict/type',
    method: 'post',
    data
  })
}

export function updateType(data: DictType) {
  return request({
    url: '/system/dict/type',
    method: 'put',
    data
  })
}

export function delType(dictId: number | string | Array<number | string>) {
  return request({
    url: `/system/dict/type/${dictId}`,
    method: 'delete'
  })
}

export function refreshCache() {
  return request({
    url: '/system/dict/type/refreshCache',
    method: 'delete'
  })
}

export function optionselect() {
  return request<DictType[]>({
    url: '/system/dict/type/optionselect',
    method: 'get'
  })
}
