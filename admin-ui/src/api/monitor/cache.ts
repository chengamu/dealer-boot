import { request, requestData } from '@/utils/request'

export interface CacheInfo {
  redis_version?: string
  redis_mode?: string
  tcp_port?: string | number
  connected_clients?: string | number
  uptime_in_days?: string | number
  used_memory_human?: string
  used_cpu_user_children?: string | number
  maxmemory_human?: string
  aof_enabled?: string | number
  rdb_last_bgsave_status?: string
  instantaneous_input_kbps?: string | number
  instantaneous_output_kbps?: string | number
}

export interface CommandStat {
  name: string
  value: number
}

export interface CacheMonitor {
  info: CacheInfo
  dbSize: number
  commandStats: CommandStat[]
}

export interface CacheName {
  cacheName: string
  remark?: string
}

export interface CacheValue {
  cacheName?: string
  cacheKey?: string
  cacheValue?: string
}

const encodePath = (value: string) => encodeURIComponent(value)

export function getCache() {
  return requestData<CacheMonitor>({
    url: '/monitor/cache',
    method: 'get'
  })
}

export function listCacheName() {
  return requestData<CacheName[]>({
    url: '/monitor/cache/getNames',
    method: 'get'
  })
}

export function listCacheKey(cacheName: string) {
  return requestData<string[]>({
    url: `/monitor/cache/getKeys/${encodePath(cacheName)}`,
    method: 'get'
  })
}

export function getCacheValue(cacheName: string, cacheKey: string) {
  return requestData<CacheValue>({
    url: `/monitor/cache/getValue/${encodePath(cacheName)}/${encodePath(cacheKey)}`,
    method: 'get'
  })
}

export function clearCacheName(cacheName: string) {
  return request({
    url: `/monitor/cache/clearCacheName/${encodePath(cacheName)}`,
    method: 'delete'
  })
}

export function clearCacheKey(cacheName: string, cacheKey: string) {
  return request({
    url: `/monitor/cache/clearCacheKey/${encodePath(cacheName)}/${encodePath(cacheKey)}`,
    method: 'delete'
  })
}

export function clearCacheAll() {
  return request({
    url: '/monitor/cache/clearCacheAll',
    method: 'delete'
  })
}
