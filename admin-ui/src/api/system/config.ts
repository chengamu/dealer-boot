import { request, requestData, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface ConfigQuery extends PageQuery {
  configName?: string
  configKey?: string
  configType?: string
  beginTime?: string
  endTime?: string
}

export interface Config {
  configId?: number
  configName?: string
  configKey?: string
  configValue?: string
  configType?: string
  remark?: string
  createTime?: string
}

export function listConfig(query: ConfigQuery) {
  return requestPage<Config>({
    url: '/system/config/list',
    method: 'get',
    params: query
  })
}

export function getConfig(configId: number | string) {
  return requestData<Config>({
    url: `/system/config/${configId}`,
    method: 'get'
  })
}

export function getConfigKey(configKey: string) {
  return request({
    url: `/system/config/configKey/${configKey}`,
    method: 'get'
  })
}

export function addConfig(data: Config) {
  return request({
    url: '/system/config',
    method: 'post',
    data
  })
}

export function updateConfig(data: Config) {
  return request({
    url: '/system/config',
    method: 'put',
    data
  })
}

export function updateConfigByKey(key: string, value: string) {
  return request({
    url: '/system/config/updateByKey',
    method: 'put',
    data: {
      configKey: key,
      configValue: value
    }
  })
}

export function delConfig(configId: number | string | Array<number | string>) {
  return request({
    url: `/system/config/${configId}`,
    method: 'delete'
  })
}

export function refreshCache() {
  return request({
    url: '/system/config/refreshCache',
    method: 'delete'
  })
}
