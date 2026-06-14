import { request, requestPage } from '@/utils/request'
import type {
  ConfigEvaluationRequest,
  ConfigEvaluationResultVO,
  ConfigOptionVO,
  ConfigQuestionVO,
  ConfigTemplateVersionVO,
  ConfigTemplateVO,
  ProductCrudApi,
  ProductPageQuery,
  ProductRecord,
  QuestionGroupVO,
  ReferenceCheckResult,
  SalesProductQuery,
  SalesProductVO
} from './types'

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

function changeStatusApi(url: string) {
  return request({ url, method: 'put' })
}

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

export const salesProductApi: ProductCrudApi<SalesProductVO, SalesProductQuery> = {
  list: (query?: SalesProductQuery) => requestPage<SalesProductVO>({ url: '/product-capability/sales-products/list', method: 'get', params: query }),
  options: (query?: SalesProductQuery) => request<SalesProductVO[]>({ url: '/product-capability/sales-products/options', method: 'get', params: query }),
  get: (id: string | number) => request<SalesProductVO>({ url: '/product-capability/sales-products/' + id, method: 'get' }),
  add: (data: SalesProductVO) => request({ url: '/product-capability/sales-products', method: 'post', data }),
  update: (data: SalesProductVO) => request({ url: '/product-capability/sales-products', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-capability/sales-products/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/sales-products/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/sales-products/' + id + '/references')
}

export const configTemplateApi: ProductCrudApi<ConfigTemplateVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/templates/list', query),
  options: (query?: ProductPageQuery) => request<ConfigTemplateVO[]>({ url: '/product-capability/templates/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/templates/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/templates', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/templates', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/templates/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/templates/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/templates/' + id + '/references')
}

export const configTemplateVersionApi: ProductCrudApi<ConfigTemplateVersionVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/template-versions/list', query),
  options: (query?: ProductPageQuery) => request<ConfigTemplateVersionVO[]>({ url: '/product-capability/template-versions/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/template-versions/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/template-versions', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/template-versions', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/template-versions/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/template-versions/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/template-versions/' + id + '/references')
}

export const questionGroupApi: ProductCrudApi<QuestionGroupVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/question-groups/list', query),
  options: (query?: ProductPageQuery) => request<QuestionGroupVO[]>({ url: '/product-capability/question-groups/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/question-groups/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/question-groups', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/question-groups', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/question-groups/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/question-groups/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/question-groups/' + id + '/references')
}

export const configQuestionApi: ProductCrudApi<ConfigQuestionVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/questions/list', query),
  options: (query?: ProductPageQuery) => request<ConfigQuestionVO[]>({ url: '/product-capability/questions/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/questions/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/questions', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/questions', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/questions/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/questions/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/questions/' + id + '/references')
}

export const configOptionApi: ProductCrudApi<ConfigOptionVO> = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/options/list', query),
  options: (query?: ProductPageQuery) => request<ConfigOptionVO[]>({ url: '/product-capability/options/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/options/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/options', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/options', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/options/' + ids),
  changeStatus: (id: string | number, status: string) => changeStatusApi('/product-capability/options/change-status/' + id + '/' + status),
  references: (id: string | number) => referencesApi('/product-capability/options/' + id + '/references')
}

export const configRuleApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/rules/list', query),
  options: (query?: ProductPageQuery) => request<ProductRecord[]>({ url: '/product-capability/rules/options', method: 'get', params: query }),
  get: (id: string | number) => getApi('/product-capability/rules/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/rules', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/rules', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/rules/' + ids)
}

export function evaluateConfig(data: ConfigEvaluationRequest) {
  return request<ConfigEvaluationResultVO>({
    url: '/product-capability/config/evaluate',
    method: 'post',
    data
  })
}
