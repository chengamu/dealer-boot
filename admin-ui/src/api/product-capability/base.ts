import { request, requestPage } from '@/utils/request'
import type {
  ProductBaseAttributeQuery,
  ProductBaseAttributeVO,
  ProductCategoryVO,
  ProductCrudApi,
  EditCheckResult,
  ProductMaterialTypeGroupQuery,
  ProductMaterialTypeGroupVO,
  ProductMaterialTypeQuery,
  ProductMaterialTypeVO,
  ProductManufacturerQuery,
  ProductManufacturerVO,
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

export const productManufacturerApi: ProductCrudApi<ProductManufacturerVO, ProductManufacturerQuery> = {
  list: (query?: ProductManufacturerQuery) => listApi('/product-capability/manufacturers/list', query),
  options: (query?: ProductManufacturerQuery) => request<ProductManufacturerVO[]>({ url: '/product-capability/manufacturers/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductManufacturerVO>('/product-capability/manufacturers/' + id),
  add: (data: ProductManufacturerVO) => saveApi('/product-capability/manufacturers', 'post', data),
  update: (data: ProductManufacturerVO) => saveApi('/product-capability/manufacturers', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/manufacturers/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/manufacturers/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/manufacturers/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/manufacturers/' + id + '/references')
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

export const productMaterialTypeGroupApi: ProductCrudApi<ProductMaterialTypeGroupVO, ProductMaterialTypeGroupQuery> = {
  list: (query?: ProductMaterialTypeGroupQuery) => listApi('/product-capability/material-type-groups/list', query),
  options: (query?: ProductMaterialTypeGroupQuery) => request<ProductMaterialTypeGroupVO[]>({ url: '/product-capability/material-type-groups/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductMaterialTypeGroupVO>('/product-capability/material-type-groups/' + id),
  add: (data: ProductMaterialTypeGroupVO) => saveApi('/product-capability/material-type-groups', 'post', data),
  update: (data: ProductMaterialTypeGroupVO) => saveApi('/product-capability/material-type-groups', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/material-type-groups/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/material-type-groups/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/material-type-groups/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/material-type-groups/' + id + '/references')
}

export const productMaterialTypeApi: ProductCrudApi<ProductMaterialTypeVO, ProductMaterialTypeQuery> = {
  list: (query?: ProductMaterialTypeQuery) => listApi('/product-capability/material-types/list', query),
  options: (query?: ProductMaterialTypeQuery) => request<ProductMaterialTypeVO[]>({ url: '/product-capability/material-types/options', method: 'get', params: query }),
  get: (id: string | number) => detailApi<ProductMaterialTypeVO>('/product-capability/material-types/' + id),
  add: (data: ProductMaterialTypeVO) => saveApi('/product-capability/material-types', 'post', data),
  update: (data: ProductMaterialTypeVO) => saveApi('/product-capability/material-types', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/material-types/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/material-types/change-status/' + id + '/' + status),
  editCheck: (id: string | number) => editCheckApi('/product-capability/material-types/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/material-types/' + id + '/references')
}
