import { request, requestPage } from '@/utils/request'
import type { EditCheckResult, FabricSeriesQuery, FabricSeriesVO, ProductCrudApi, ReferenceCheckResult } from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

function editCheckApi(url: string) {
  return request<EditCheckResult>({ url, method: 'get' })
}

export const fabricSeriesApi: ProductCrudApi<FabricSeriesVO, FabricSeriesQuery> = {
  list: (query?: FabricSeriesQuery) => requestPage<FabricSeriesVO>({ url: '/product-capability/fabric-series/list', method: 'get', params: query }),
  options: (query?: FabricSeriesQuery) => request<FabricSeriesVO[]>({ url: '/product-capability/fabric-series/options', method: 'get', params: query }),
  get: (id: string | number) => request<FabricSeriesVO>({ url: '/product-capability/fabric-series/' + id, method: 'get' }),
  add: (data: FabricSeriesVO) => request({ url: '/product-capability/fabric-series', method: 'post', data }),
  update: (data: FabricSeriesVO) => request({ url: '/product-capability/fabric-series', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/fabric-series/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/fabric-series/change-status/' + id + '/' + status, method: 'put' }),
  editCheck: (id: string | number) => editCheckApi('/product-capability/fabric-series/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-capability/fabric-series/' + id + '/references')
}
