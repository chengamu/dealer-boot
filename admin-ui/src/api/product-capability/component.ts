import { request, requestPage } from '@/utils/request'
import type {
  ProductComponentItemQuery,
  ProductComponentItemVO,
  ProductComponentQuery,
  ProductComponentVO,
  ProductCrudApi,
  ReferenceCheckResult
} from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const productComponentItemApi: ProductCrudApi<ProductComponentItemVO, ProductComponentItemQuery> & {
  listByParent: (componentIdOrCode: string | number) => Promise<{ rows?: ProductComponentItemVO[]; total?: number }>
} = {
  list: (query?: ProductComponentItemQuery) => requestPage<ProductComponentItemVO>({ url: '/product-capability/component-items/list', method: 'get', params: query }),
  listByParent: (componentIdOrCode: string | number) => requestPage<ProductComponentItemVO>({
    url: '/product-capability/component-items/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 999,
      componentCode: typeof componentIdOrCode === 'string' ? componentIdOrCode : undefined,
      componentId: typeof componentIdOrCode === 'number' ? componentIdOrCode : undefined
    }
  }),
  options: (query?: ProductComponentItemQuery) => request<ProductComponentItemVO[]>({ url: '/product-capability/component-items/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductComponentItemVO>({ url: '/product-capability/component-items/' + id, method: 'get' }),
  add: (data: ProductComponentItemVO) => request({ url: '/product-capability/component-items', method: 'post', data }),
  update: (data: ProductComponentItemVO) => request({ url: '/product-capability/component-items', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/component-items/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/component-items/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/component-items/' + id + '/references')
}

export const productComponentApi: ProductCrudApi<ProductComponentVO, ProductComponentQuery> = {
  list: (query?: ProductComponentQuery) => requestPage<ProductComponentVO>({ url: '/product-capability/components/list', method: 'get', params: query }),
  options: (query?: ProductComponentQuery) => request<ProductComponentVO[]>({ url: '/product-capability/components/options', method: 'get', params: query }),
  get: (id: string | number) => request<ProductComponentVO>({ url: '/product-capability/components/' + id, method: 'get' }),
  add: (data: ProductComponentVO) => request({ url: '/product-capability/components', method: 'post', data }),
  update: (data: ProductComponentVO) => request({ url: '/product-capability/components', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/components/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/components/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/components/' + id + '/references')
}
