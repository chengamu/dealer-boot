import { request, requestPage } from '@/utils/request'
import type {
  EngineeringCheckCaseVO,
  EngineeringItemScopeVO,
  EngineeringItemVO,
  EngineeringOutputRuleVO,
  EngineeringPlanVO,
  EngineeringPlanVersionVO,
  EngineeringRuleVO,
  ProductPageQuery,
  StandardSkuEngineeringVO
} from './types'

function listApi<T>(url: string, query?: ProductPageQuery) {
  return requestPage<T>({ url, method: 'get', params: query })
}

function saveApi(url: string, method: 'post' | 'put', data: Record<string, unknown>) {
  return request({ url, method, data })
}

const baseUrl = '/product-engineering'

export const engineeringPlanApi = {
  list: (query?: ProductPageQuery) => listApi<EngineeringPlanVO>(`${baseUrl}/plans/list`, query),
  options: (query?: ProductPageQuery) => request<EngineeringPlanVO[]>({ url: `${baseUrl}/plans/options`, method: 'get', params: query }),
  get: (id: string | number) => request<EngineeringPlanVO>({ url: `${baseUrl}/plans/${id}`, method: 'get' }),
  add: (data: EngineeringPlanVO) => saveApi(`${baseUrl}/plans`, 'post', data as Record<string, unknown>),
  update: (data: EngineeringPlanVO) => saveApi(`${baseUrl}/plans`, 'put', data as Record<string, unknown>),
  remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/plans/${ids}`, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: `${baseUrl}/plans/change-status/${id}/${status}`, method: 'put' })
}

export const engineeringApi = {
  workbench: (versionId: string | number) => request<Record<string, unknown>>({ url: `${baseUrl}/workbench/${versionId}`, method: 'get' }),
  preview: (data: Record<string, unknown>) => request<Record<string, unknown>>({ url: `${baseUrl}/preview`, method: 'post', data }),
  check: (versionId: string | number) => request<Record<string, unknown>>({ url: `${baseUrl}/check/${versionId}`, method: 'get' }),
  versions: {
    list: (query?: ProductPageQuery) => listApi<EngineeringPlanVersionVO>(`${baseUrl}/versions/list`, query),
    get: (id: string | number) => request<EngineeringPlanVersionVO>({ url: `${baseUrl}/versions/${id}`, method: 'get' }),
    add: (data: EngineeringPlanVersionVO) => saveApi(`${baseUrl}/versions`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringPlanVersionVO) => saveApi(`${baseUrl}/versions`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/versions/${ids}`, method: 'delete' })
  },
  items: {
    list: (query?: ProductPageQuery) => listApi<EngineeringItemVO>(`${baseUrl}/items/list`, query),
    get: (id: string | number) => request<EngineeringItemVO>({ url: `${baseUrl}/items/${id}`, method: 'get' }),
    add: (data: EngineeringItemVO) => saveApi(`${baseUrl}/items`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringItemVO) => saveApi(`${baseUrl}/items`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/items/${ids}`, method: 'delete' })
  },
  scopes: {
    list: (query?: ProductPageQuery) => listApi<EngineeringItemScopeVO>(`${baseUrl}/scopes/list`, query),
    get: (id: string | number) => request<EngineeringItemScopeVO>({ url: `${baseUrl}/scopes/${id}`, method: 'get' }),
    add: (data: EngineeringItemScopeVO) => saveApi(`${baseUrl}/scopes`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringItemScopeVO) => saveApi(`${baseUrl}/scopes`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/scopes/${ids}`, method: 'delete' })
  },
  rules: {
    list: (query?: ProductPageQuery) => listApi<EngineeringRuleVO>(`${baseUrl}/rules/list`, query),
    get: (id: string | number) => request<EngineeringRuleVO>({ url: `${baseUrl}/rules/${id}`, method: 'get' }),
    add: (data: EngineeringRuleVO) => saveApi(`${baseUrl}/rules`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringRuleVO) => saveApi(`${baseUrl}/rules`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/rules/${ids}`, method: 'delete' })
  },
  outputRules: {
    list: (query?: ProductPageQuery) => listApi<EngineeringOutputRuleVO>(`${baseUrl}/output-rules/list`, query),
    get: (id: string | number) => request<EngineeringOutputRuleVO>({ url: `${baseUrl}/output-rules/${id}`, method: 'get' }),
    add: (data: EngineeringOutputRuleVO) => saveApi(`${baseUrl}/output-rules`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringOutputRuleVO) => saveApi(`${baseUrl}/output-rules`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/output-rules/${ids}`, method: 'delete' })
  },
  standardSkus: {
    list: (query?: ProductPageQuery) => listApi<StandardSkuEngineeringVO>(`${baseUrl}/standard-skus/list`, query),
    get: (id: string | number) => request<StandardSkuEngineeringVO>({ url: `${baseUrl}/standard-skus/${id}`, method: 'get' }),
    add: (data: StandardSkuEngineeringVO) => saveApi(`${baseUrl}/standard-skus`, 'post', data as Record<string, unknown>),
    update: (data: StandardSkuEngineeringVO) => saveApi(`${baseUrl}/standard-skus`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/standard-skus/${ids}`, method: 'delete' })
  },
  checkCases: {
    list: (query?: ProductPageQuery) => listApi<EngineeringCheckCaseVO>(`${baseUrl}/check-cases/list`, query),
    get: (id: string | number) => request<EngineeringCheckCaseVO>({ url: `${baseUrl}/check-cases/${id}`, method: 'get' }),
    add: (data: EngineeringCheckCaseVO) => saveApi(`${baseUrl}/check-cases`, 'post', data as Record<string, unknown>),
    update: (data: EngineeringCheckCaseVO) => saveApi(`${baseUrl}/check-cases`, 'put', data as Record<string, unknown>),
    remove: (ids: Array<string | number> | string | number) => request({ url: `${baseUrl}/check-cases/${ids}`, method: 'delete' })
  }
}
