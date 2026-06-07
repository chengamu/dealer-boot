import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord } from './types'

function listApi(url: string, query?: ProductPageQuery) {
  return requestPage<ProductRecord>({ url, method: 'get', params: query })
}

function getApi(url: string) {
  return request<ProductRecord>({ url, method: 'get' })
}

function saveApi(url: string, method: 'post' | 'put', data: ProductRecord) {
  return request({ url, method, data })
}

function removeApi(url: string) {
  return request({ url, method: 'delete' })
}

export const pricePlanApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/price-plans/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/price-plans/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/price-plans/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/price-plans', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/price-plans', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/price-plans/' + ids)
}

export const pricePlanVersionApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/price-plan-versions/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/price-plan-versions/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/price-plan-versions/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/price-plan-versions', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/price-plan-versions', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/price-plan-versions/' + ids)
}

export const priceRuleItemApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/price-rule-items/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/price-rule-items/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/price-rule-items/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/price-rule-items', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/price-rule-items', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/price-rule-items/' + ids)
}

export function calculatePrice(data: ProductRecord) {
  return request<ProductRecord>({
    url: '/product-capability/pricing/calculate',
    method: 'post',
    data
  })
}
