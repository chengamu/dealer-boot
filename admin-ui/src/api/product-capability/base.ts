import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from './types'

function listApi<T extends ProductRecord>(url: string, query?: ProductPageQuery) {
  return requestPage<T>({ url, method: 'get', params: query })
}

function detailApi<T extends ProductRecord>(url: string) {
  return request<T>({ url, method: 'get' })
}

function saveApi(url: string, method: 'post' | 'put', data: ProductRecord) {
  return request({ url, method, data })
}

function removeApi(url: string) {
  return request({ url, method: 'delete' })
}

function changeStatusApi(url: string) {
  return request({ url, method: 'put' })
}

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productCategoryApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/categories/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/categories/options', method: 'get', params: query }),
  tree: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/categories/tree', method: 'get', params: query }),
  get: (id: string | number) => detailApi('/product-capability/categories/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/categories', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/categories', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/categories/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/categories/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/categories/' + id + '/references')
}

export const productMaterialApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/materials/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/materials/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi('/product-capability/materials/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/materials', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/materials', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/materials/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/materials/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/materials/' + id + '/references')
}

export const productComponentApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/components/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/components/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi('/product-capability/components/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/components', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/components', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/components/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/components/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/components/' + id + '/references')
}

