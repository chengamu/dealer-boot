import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface OssQuery extends PageQuery {
  fileName?: string
  originalName?: string
  fileSuffix?: string
  url?: string
  createBy?: string
  service?: string
  beginCreateTime?: string
  endCreateTime?: string
  orderByColumn?: string
  isAsc?: string
}

export interface OssFile {
  ossId?: number
  fileName?: string
  originalName?: string
  fileSuffix?: string
  url?: string
  createTime?: string
  createBy?: string
  service?: string
}

export function listOss(query?: OssQuery) {
  return requestPage<OssFile>({
    url: '/system/oss/list',
    method: 'get',
    params: query
  })
}

export function listByIds(ossId: number | string | Array<number | string>) {
  return request<OssFile[]>({
    url: `/system/oss/listByIds/${ossId}`,
    method: 'get'
  })
}

export function delOss(ossId: number | string | Array<number | string>) {
  return request({
    url: `/system/oss/${ossId}`,
    method: 'delete'
  })
}

export function delOwnedOss(ossId: number | string | Array<number | string>) {
  return request({
    url: `/system/oss/owned/${ossId}`,
    method: 'delete'
  })
}
