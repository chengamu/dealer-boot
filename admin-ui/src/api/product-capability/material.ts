import { request, requestPage } from '@/utils/request'
import type {
  ProductCrudApi,
  ProductMaterialAttributeQuery,
  ProductMaterialAttributeVO,
  ProductMaterialQuery,
  ProductMaterialVO,
  ReferenceCheckResult
} from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productMaterialApi: ProductCrudApi<ProductMaterialVO, ProductMaterialQuery> = {
  list: (query?: ProductMaterialQuery) => requestPage<ProductMaterialVO>({ url: '/product-capability/materials/list', method: 'get', params: query }),
  options: (query?: ProductMaterialQuery) => request<ProductMaterialVO[]>({ url: '/product-capability/materials/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductMaterialVO>({ url: '/product-capability/materials/' + id, method: 'get' }),
  add: (data: ProductMaterialVO) => request({ url: '/product-capability/materials', method: 'post', data }),
  update: (data: ProductMaterialVO) => request({ url: '/product-capability/materials', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/materials/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/materials/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/materials/' + id + '/references')
}

export const productMaterialAttributeApi: ProductCrudApi<ProductMaterialAttributeVO, ProductMaterialAttributeQuery> & {
  listByParent: (materialIdOrCode: string | number) => Promise<{ rows?: ProductMaterialAttributeVO[]; total?: number }>
} = {
  list: (query?: ProductMaterialAttributeQuery) => requestPage<ProductMaterialAttributeVO>({ url: '/product-capability/material-attributes/list', method: 'get', params: query }),
  listByParent: (materialIdOrCode: string | number) => requestPage<ProductMaterialAttributeVO>({
    url: '/product-capability/material-attributes/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 999,
      materialCode: typeof materialIdOrCode === 'string' ? materialIdOrCode : undefined,
      materialId: typeof materialIdOrCode === 'number' ? materialIdOrCode : undefined
    }
  }),
  options: (query?: ProductMaterialAttributeQuery) => request<ProductMaterialAttributeVO[]>({ url: '/product-capability/material-attributes/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductMaterialAttributeVO>({ url: '/product-capability/material-attributes/' + id, method: 'get' }),
  add: (data: ProductMaterialAttributeVO) => request({ url: '/product-capability/material-attributes', method: 'post', data }),
  update: (data: ProductMaterialAttributeVO) => request({ url: '/product-capability/material-attributes', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/material-attributes/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/material-attributes/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/material-attributes/' + id + '/references')
}
