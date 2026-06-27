import { request, requestPage } from '@/utils/request'
import type {
  EditCheckResult,
  ProductCrudApi,
  ProductFormulaQuery,
  ProductFormulaSetupVO,
  ProductFormulaVersionVO,
  ProductFormulaVO,
  ReferenceCheckResult
} from '@/api/product-capability/types'

function referencesApi(url: string) {
  return request<ReferenceCheckResult>({ url, method: 'get' })
}

function editCheckApi(url: string) {
  return request<EditCheckResult>({ url, method: 'get' })
}

export const productFormulaApi: ProductCrudApi<ProductFormulaVO, ProductFormulaQuery> & {
  submitReview: (id: string | number) => Promise<unknown>
  approve: (id: string | number) => Promise<unknown>
  reject: (id: string | number, rejectReason: string) => Promise<unknown>
  stop: (id: string | number) => Promise<unknown>
  setup: (id: string | number) => Promise<{ data?: ProductFormulaSetupVO }>
  saveSetup: (id: string | number, data: ProductFormulaSetupVO) => Promise<unknown>
  validateSetup: (id: string | number) => Promise<unknown>
  validate: (id: string | number) => Promise<unknown>
  versions: (id: string | number) => Promise<{ data?: ProductFormulaVersionVO[] }>
  version: (id: string | number, versionId: string | number) => Promise<{ data?: ProductFormulaVersionVO }>
} = {
  list: (query?: ProductFormulaQuery) => requestPage<ProductFormulaVO>({ url: '/product-formula/formulas/list', method: 'get', params: query }),
  get: (id: string | number) => request<ProductFormulaVO>({ url: '/product-formula/formulas/' + id, method: 'get' }),
  add: (data: ProductFormulaVO) => request({ url: '/product-formula/formulas', method: 'post', data }),
  update: (data: ProductFormulaVO) => request({ url: '/product-formula/formulas', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-formula/formulas/' + ids, method: 'delete' }),
  editCheck: (id: string | number) => editCheckApi('/product-formula/formulas/' + id + '/edit-check'),
  references: (id: string | number) => referencesApi('/product-formula/formulas/' + id + '/references'),
  submitReview: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/submit-review', method: 'put' }),
  approve: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/approve', method: 'put' }),
  reject: (id: string | number, rejectReason: string) => request({ url: '/product-formula/formulas/' + id + '/reject', method: 'put', data: { rejectReason } }),
  stop: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/stop', method: 'put' }),
  setup: (id: string | number) => request<ProductFormulaSetupVO>({ url: '/product-formula/formulas/' + id + '/setup', method: 'get' }),
  saveSetup: (id: string | number, data: ProductFormulaSetupVO) => request({ url: '/product-formula/formulas/' + id + '/setup', method: 'put', data }),
  validateSetup: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/setup/validate', method: 'put' }),
  validate: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/validate', method: 'put' }),
  versions: (id: string | number) => request<ProductFormulaVersionVO[]>({ url: '/product-formula/formulas/' + id + '/versions', method: 'get' }),
  version: (id: string | number, versionId: string | number) => request<ProductFormulaVersionVO>({ url: '/product-formula/formulas/' + id + '/versions/' + versionId, method: 'get' })
}
