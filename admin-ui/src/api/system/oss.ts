import { request } from '@/utils/request'
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
  return request({
    url: '/system/oss/list',
    method: 'get',
    params: query
  }) as unknown as Promise<{ rows?: OssFile[]; total?: number }>
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
