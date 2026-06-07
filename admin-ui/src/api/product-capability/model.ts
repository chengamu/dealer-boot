import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from './types'

function listApi<T extends ProductRecord>(url: string, query?: ProductPageQuery) {
  return requestPage<T>({ url, method: 'get', params: query })
}

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productModelApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/models/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/models/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductRecord>({ url: '/product-capability/models/' + id, method: 'get' }),
  add: (data: ProductRecord) => request({ url: '/product-capability/models', method: 'post', data }),
  update: (data: ProductRecord) => request({ url: '/product-capability/models', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/models/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/models/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/models/' + id + '/references'),
  variants: (modelId: string | number) => request<ProductRecord[]>({ url: '/product-capability/models/' + modelId + '/variants', method: 'get' }),
  addVariant: (modelId: string | number, data: ProductRecord) => request({ url: '/product-capability/models/' + modelId + '/variants', method: 'post', data })
}

export const salesVariantApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/sales-variants/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/sales-variants/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductRecord>({ url: '/product-capability/sales-variants/' + id, method: 'get' }),
  add: (data: ProductRecord) => request({ url: '/product-capability/sales-variants', method: 'post', data }),
  update: (data: ProductRecord) => request({ url: '/product-capability/sales-variants', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/sales-variants/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/sales-variants/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/sales-variants/' + id + '/references')
}

