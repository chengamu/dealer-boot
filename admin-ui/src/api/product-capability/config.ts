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

export const configTemplateApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/templates/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/templates/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/templates/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/templates', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/templates', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/templates/' + ids)
}

export const configTemplateVersionApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/template-versions/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/template-versions/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/template-versions/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/template-versions', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/template-versions', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/template-versions/' + ids)
}

export const questionGroupApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/question-groups/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/question-groups/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/question-groups/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/question-groups', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/question-groups', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/question-groups/' + ids)
}

export const configQuestionApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/questions/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/questions/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/questions/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/questions', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/questions', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/questions/' + ids)
}

export const configOptionApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/options/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/options/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/options/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/options', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/options', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/options/' + ids)
}

export const configRuleApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/rules/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/rules/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/rules/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/rules', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/rules', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/rules/' + ids)
}

export function evaluateConfig(data: ProductRecord) {
  return request<ProductRecord>({
    url: '/product-capability/config/evaluate',
    method: 'post',
    data
  })
}
