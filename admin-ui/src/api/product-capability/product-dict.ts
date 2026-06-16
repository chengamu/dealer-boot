import { request, requestPage } from '@/utils/request'
import type {
  ProductCrudApi,
  ProductDictItemQuery,
  ProductDictItemVO,
  ProductDictOption,
  ProductDictTypeQuery,
  ProductDictTypeVO,
  ReferenceCheckResult
} from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productDictTypeApi: ProductCrudApi<ProductDictTypeVO, ProductDictTypeQuery> = {
  list: (query?: ProductDictTypeQuery) => requestPage<ProductDictTypeVO>({ url: '/product-capability/product-dict-types/list', method: 'get', params: query }),
  options: (query?: ProductDictTypeQuery) => request<ProductDictTypeVO[]>({ url: '/product-capability/product-dict-types/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductDictTypeVO>({ url: '/product-capability/product-dict-types/' + id, method: 'get' }),
  add: (data: ProductDictTypeVO) => request({ url: '/product-capability/product-dict-types', method: 'post', data }),
  update: (data: ProductDictTypeVO) => request({ url: '/product-capability/product-dict-types', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/product-dict-types/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/product-dict-types/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/product-dict-types/' + id + '/references')
}

export const productDictItemApi: ProductCrudApi<ProductDictItemVO, ProductDictItemQuery> = {
  list: (query?: ProductDictItemQuery) => requestPage<ProductDictItemVO>({ url: '/product-capability/product-dict-items/list', method: 'get', params: query }),
  options: (query?: ProductDictItemQuery) => request<ProductDictItemVO[]>({ url: '/product-capability/product-dict-items/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductDictItemVO>({ url: '/product-capability/product-dict-items/' + id, method: 'get' }),
  add: (data: ProductDictItemVO) => request({ url: '/product-capability/product-dict-items', method: 'post', data }),
  update: (data: ProductDictItemVO) => request({ url: '/product-capability/product-dict-items', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/product-dict-items/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/product-dict-items/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/product-dict-items/' + id + '/references')
}

export function getProductDictItems(dictTypeCode: string) {
  return request<ProductDictOption[]>({ url: '/product-capability/product-dict-items/type/' + dictTypeCode, method: 'get' })
}
