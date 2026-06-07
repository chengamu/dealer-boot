import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord } from './types'

function listApi(url: string, query?: ProductPageQuery) {
  return requestPage<ProductRecord>({ url, method: 'get', params: query })
}

function getApi(url: string) {
  return request<ProductRecord>({ url, method: 'get' })
}

function saveApi(url: string, method: 'post' | 'put', data: ProductRecord) {
  return request({ url, method, data })
}

function removeApi(url: string) {
  return request({ url, method: 'delete' })
}

function readonlyWrite() {
  return Promise.reject(new Error('Readonly resource'))
}

export const publishCheckResultApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/publish-check-results/list', query),
  get: (id: string | number) => getApi('/product-capability/publish-check-results/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/publish-check-results', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/publish-check-results', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/publish-check-results/' + ids)
}

export const gapTaskApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/gap-tasks/list', query),
  get: (id: string | number) => getApi('/product-capability/gap-tasks/' + id),
  resolve: (id: string | number) => request({ url: '/product-capability/gap-tasks/' + id + '/resolve', method: 'post' }),
  add: readonlyWrite,
  update: readonlyWrite,
  remove: readonlyWrite
}

export const publishApprovalApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/publish-approvals/list', query),
  get: (id: string | number) => getApi('/product-capability/publish-approvals/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/publish-approvals', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/publish-approvals', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/publish-approvals/' + ids),
  submit: (data: ProductRecord) => request<ProductRecord>({ url: '/product-capability/publish-approvals/submit', method: 'post', data }),
  approve: (id: string | number, data?: ProductRecord) => request({ url: '/product-capability/publish-approvals/' + id + '/approve', method: 'post', data }),
  reject: (id: string | number, data?: ProductRecord) => request({ url: '/product-capability/publish-approvals/' + id + '/reject', method: 'post', data })
}

export const publishPackageApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/publish-packages/list', query),
  get: (id: string | number) => getApi('/product-capability/publish-packages/' + id),
  add: readonlyWrite,
  update: readonlyWrite,
  remove: readonlyWrite
}

export const salesViewPackageApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/sales-view/publish-packages/list', query),
  get: (id: string | number) => getApi('/product-capability/sales-view/publish-packages/' + id),
  add: readonlyWrite,
  update: readonlyWrite,
  remove: readonlyWrite
}

export const syncOutboxApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/sync-outbox/list', query),
  get: (id: string | number) => getApi('/product-capability/sync-outbox/' + id),
  retry: (id: string | number) => request({ url: '/product-capability/sync-outbox/' + id + '/retry', method: 'post' }),
  add: readonlyWrite,
  update: readonlyWrite,
  remove: readonlyWrite
}

export function runPublishCheck(data: ProductRecord) {
  return request<ProductRecord>({
    url: '/product-capability/publish/check',
    method: 'post',
    data
  })
}

export function createPublishPackage(data: ProductRecord) {
  return request<ProductRecord>({
    url: '/product-capability/publish/packages/create',
    method: 'post',
    data
  })
}
