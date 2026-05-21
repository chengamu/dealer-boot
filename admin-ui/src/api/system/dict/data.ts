import { request } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface DictDataQuery extends PageQuery {
  dictName?: string
  dictType?: string
  dictLabel?: string
  status?: string
}

export interface DictData {
  dictCode?: number
  dictSort?: number
  dictLabel?: string
  dictValue?: string
  dictType?: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  status?: string
  remark?: string
  i18nKey?: string
  createTime?: string
}

export function listData(query?: DictDataQuery) {
  return request({
    url: '/system/dict/data/list',
    method: 'get',
    params: query
  }) as Promise<{ rows?: DictData[]; total?: number }>
}

export function getData(dictCode: number | string | Array<number | string>) {
  return request<DictData>({
    url: `/system/dict/data/${dictCode}`,
    method: 'get'
  })
}

export function getDicts(dictType: string) {
  return request<DictData[]>({
    url: `/system/dict/data/type/${dictType}`,
    method: 'get'
  })
}

export function addData(data: DictData) {
  return request({
    url: '/system/dict/data',
    method: 'post',
    data
  })
}

export function updateData(data: DictData) {
  return request({
    url: '/system/dict/data',
    method: 'put',
    data
  })
}

export function delData(dictCode: number | string | Array<number | string>) {
  return request({
    url: `/system/dict/data/${dictCode}`,
    method: 'delete'
  })
}
