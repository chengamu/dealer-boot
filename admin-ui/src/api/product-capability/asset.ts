import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productMediaAssetApi = {
  list: (query?: ProductPageQuery) => requestPage<ProductRecord>({ url: '/product-capability/media-assets/list', method: 'get', params: query }),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/media-assets/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductRecord>({ url: '/product-capability/media-assets/' + id, method: 'get' }),
  add: (data: ProductRecord) => request({ url: '/product-capability/media-assets', method: 'post', data }),
  update: (data: ProductRecord) => request({ url: '/product-capability/media-assets', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/media-assets/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/media-assets/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/media-assets/' + id + '/references')
}

export const productMediaBindingApi = {
  list: (query?: ProductPageQuery) => requestPage<ProductRecord>({ url: '/product-capability/media-bindings/list', method: 'get', params: query }),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/media-bindings/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductRecord>({ url: '/product-capability/media-bindings/' + id, method: 'get' }),
  add: (data: ProductRecord) => request({ url: '/product-capability/media-bindings', method: 'post', data }),
  update: (data: ProductRecord) => request({ url: '/product-capability/media-bindings', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/media-bindings/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/media-bindings/change-status/' + id + '/' + status, method: 'put' }),
  batchBind: (data: ProductRecord) => request({ url: '/product-capability/media-bindings/batch', method: 'post', data }),
  references: (id: string | number) => referencesApi('/product-capability/media-bindings/' + id + '/references')
}

