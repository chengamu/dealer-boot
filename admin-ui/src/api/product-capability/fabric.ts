import { request, requestPage } from '@/utils/request'
import type { FabricProfileQuery, FabricProfileVO, FabricSeriesQuery, FabricSeriesVO, ProductCrudApi, ReferenceCheckResult } from './types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const fabricSeriesApi: ProductCrudApi<FabricSeriesVO, FabricSeriesQuery> = {
  list: (query?: FabricSeriesQuery) => requestPage<FabricSeriesVO>({ url: '/product-capability/fabric-series/list', method: 'get', params: query }),
  options: (query?: FabricSeriesQuery) => request<FabricSeriesVO[]>({ url: '/product-capability/fabric-series/options', method: 'get', params: query }),
  get: (id: string | number) => request<FabricSeriesVO>({ url: '/product-capability/fabric-series/' + id, method: 'get' }),
  add: (data: FabricSeriesVO) => request({ url: '/product-capability/fabric-series', method: 'post', data }),
  update: (data: FabricSeriesVO) => request({ url: '/product-capability/fabric-series', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/fabric-series/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/fabric-series/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/fabric-series/' + id + '/references')
}

export const fabricProfileApi: ProductCrudApi<FabricProfileVO, FabricProfileQuery> & {
  listByParent: (seriesIdOrCode: string | number) => Promise<{ rows?: FabricProfileVO[]; total?: number }>
} = {
  list: (query?: FabricProfileQuery) => requestPage<FabricProfileVO>({ url: '/product-capability/fabric-profiles/list', method: 'get', params: query }),
  listByParent: (seriesIdOrCode: string | number) => requestPage<FabricProfileVO>({
    url: '/product-capability/fabric-profiles/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 999,
      seriesCode: typeof seriesIdOrCode === 'string' ? seriesIdOrCode : undefined,
      seriesId: typeof seriesIdOrCode === 'number' ? seriesIdOrCode : undefined
    }
  }),
  options: (query?: FabricProfileQuery) => request<FabricProfileVO[]>({ url: '/product-capability/fabric-profiles/options', method: 'get', params: query }),
  get: (id: string | number) => request<FabricProfileVO>({ url: '/product-capability/fabric-profiles/' + id, method: 'get' }),
  add: (data: FabricProfileVO) => request({ url: '/product-capability/fabric-profiles', method: 'post', data }),
  update: (data: FabricProfileVO) => request({ url: '/product-capability/fabric-profiles', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/fabric-profiles/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/fabric-profiles/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => referencesApi('/product-capability/fabric-profiles/' + id + '/references')
}
