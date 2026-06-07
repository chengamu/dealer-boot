import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, WorkbenchPriority, WorkbenchProgress, WorkbenchSummary, WorkbenchSyncEvent } from './types'

export function getWorkbenchSummary(query?: ProductPageQuery) {
  return request<WorkbenchSummary>({
    url: '/product-capability/workbench/summary',
    method: 'get',
    params: query
  })
}

export function listWorkbenchProgress(query?: ProductPageQuery) {
  return requestPage<WorkbenchProgress>({
    url: '/product-capability/workbench/progress/list',
    method: 'get',
    params: query
  })
}

export function listWorkbenchPriority(query?: ProductPageQuery) {
  return requestPage<WorkbenchPriority>({
    url: '/product-capability/workbench/priority/list',
    method: 'get',
    params: query
  })
}

export function listWorkbenchSyncEvents(query?: ProductPageQuery) {
  return requestPage<WorkbenchSyncEvent>({
    url: '/product-capability/workbench/sync-events',
    method: 'get',
    params: query
  })
}

