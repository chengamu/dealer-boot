import { request, requestPage } from '@/utils/request'
import type {
  ProductBaseAttributeQuery,
  ProductBaseAttributeVO,
  ProductCategoryVO,
  ProductCrudApi,
  EditCheckResult,
  ProductPageQuery,
  ProductRecord,
  ProductUnitQuery,
  ProductUnitVO,
  ReferenceCheckResult
} from './types'

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

function editCheckApi(url: string) {
  return request<EditCheckResult>({ url, method: 'get' })
}

export const productCategoryApi: ProductCrudApi<ProductCategoryVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/categories/list', query),
  options: (query?: ProductPageQuery) => request<ProductCategoryVO[]>({ url: '/product-capability/categories/options', method: 'get', params: query }),
  tree: (query?: ProductPageQuery) => request<ProductCategoryVO[]>({ url: '/product-capability/categories/tree', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductCategoryVO>('/product-capability/categories/' + id),
  add: (data: ProductCategoryVO) => saveApi('/product-capability/categories', 'post', data),
  update: (data: ProductCategoryVO) => saveApi('/product-capability/categories', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/categories/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/categories/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/categories/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/categories/' + id + '/references')
}

export const productUnitApi: ProductCrudApi<ProductUnitVO, ProductUnitQuery> = {
  list: (query?: ProductUnitQuery) => listApi('/product-capability/units/list', query),
  options: (query?: ProductUnitQuery) => request<ProductUnitVO[]>({ url: '/product-capability/units/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductUnitVO>('/product-capability/units/' + id),
  add: (data: ProductUnitVO) => saveApi('/product-capability/units', 'post', data),
  update: (data: ProductUnitVO) => saveApi('/product-capability/units', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/units/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/units/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/units/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/units/' + id + '/references')
}

export const productBaseAttributeApi: ProductCrudApi<ProductBaseAttributeVO, ProductBaseAttributeQuery> = {
  list: (query?: ProductBaseAttributeQuery) => listApi('/product-capability/base-attributes/list', query),
  options: (query?: ProductBaseAttributeQuery) => request<ProductBaseAttributeVO[]>({ url: '/product-capability/base-attributes/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductBaseAttributeVO>('/product-capability/base-attributes/' + id),
  add: (data: ProductBaseAttributeVO) => saveApi('/product-capability/base-attributes', 'post', data),
  update: (data: ProductBaseAttributeVO) => saveApi('/product-capability/base-attributes', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/base-attributes/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/base-attributes/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/base-attributes/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/base-attributes/' + id + '/references')
}
