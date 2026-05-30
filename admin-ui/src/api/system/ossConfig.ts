import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface OssConfigQuery extends PageQuery {
  configKey?: string
  bucketName?: string
  status?: string
}

export interface OssConfig {
  ossConfigId?: number
  configKey?: string
  accessKey?: string
  secretKey?: string
  bucketName?: string
  prefix?: string
  endpoint?: string
  domain?: string
  isHttps?: string
  accessPolicy?: string
  region?: string
  status?: string
  remark?: string
}

export function listOssConfig(query?: OssConfigQuery) {
  return requestPage<OssConfig>({
    url: '/system/oss/config/list',
    method: 'get',
    params: query
  })
}

export function getOssConfig(ossConfigId: number | string | Array<number | string>) {
  return request<OssConfig>({
    url: `/system/oss/config/${ossConfigId}`,
    method: 'get'
  })
}

export function addOssConfig(data: OssConfig) {
  return request({
    url: '/system/oss/config',
    method: 'post',
    data
  })
}

export function updateOssConfig(data: OssConfig) {
  return request({
    url: '/system/oss/config',
    method: 'put',
    data
  })
}

export function delOssConfig(ossConfigId: number | string | Array<number | string>) {
  return request({
    url: `/system/oss/config/${ossConfigId}`,
    method: 'delete'
  })
}

export function changeOssConfigStatus(ossConfigId: number | string, status: string, configKey?: string) {
  return request({
    url: '/system/oss/config/changeStatus',
    method: 'put',
    data: {
      ossConfigId,
      status,
      configKey
    }
  })
}
