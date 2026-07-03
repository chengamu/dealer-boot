import { request, requestPage } from '@/utils/request'
import type {
  EditCheckResult,
  ProductCrudApi,
  ProductFormulaQuery,
  ProductFormulaSimulationBO,
  ProductFormulaSimulationVO,
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
  stop: (id: string | number) => Promise<unknown>
  materials: (id: string | number) => Promise<{ data?: ProductFormulaSetupVO }>
  saveMaterials: (id: string | number, data: ProductFormulaSetupVO) => Promise<unknown>
  validateMaterials: (id: string | number) => Promise<unknown>
  getFormulaOptions: (id: string | number) => Promise<{ data?: ProductFormulaSetupVO }>
  saveOptions: (id: string | number, data: ProductFormulaSetupVO) => Promise<unknown>
  validateOptions: (id: string | number) => Promise<unknown>
  variables: (id: string | number) => Promise<{ data?: ProductFormulaSetupVO }>
  saveVariables: (id: string | number, data: ProductFormulaSetupVO) => Promise<unknown>
  copyVariables: (id: string | number, sourceFormulaCode: string) => Promise<unknown>
  validateVariables: (id: string | number) => Promise<unknown>
  simulation: (id: string | number) => Promise<{ data?: ProductFormulaSimulationVO }>
  runSimulation: (id: string | number, data?: ProductFormulaSimulationBO) => Promise<{ data?: ProductFormulaSimulationVO }>
  validateSimulation: (id: string | number, data?: ProductFormulaSimulationBO) => Promise<unknown>
  reviews: (query?: Record<string, unknown>) => Promise<{ rows?: ProductFormulaVersionVO[]; total?: number }>
  review: (reviewId: string | number) => Promise<{ data?: ProductFormulaVersionVO }>
  approveReview: (reviewId: string | number) => Promise<unknown>
  rejectReview: (reviewId: string | number, rejectReason: string) => Promise<unknown>
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
  stop: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/stop', method: 'put' }),
  materials: (id: string | number) => request<ProductFormulaSetupVO>({ url: '/product-formula/formulas/' + id + '/materials', method: 'get' }),
  saveMaterials: (id: string | number, data: ProductFormulaSetupVO) => request({ url: '/product-formula/formulas/' + id + '/materials', method: 'put', data }),
  validateMaterials: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/materials/validate', method: 'put' }),
  getFormulaOptions: (id: string | number) => request<ProductFormulaSetupVO>({ url: '/product-formula/formulas/' + id + '/options', method: 'get' }),
  saveOptions: (id: string | number, data: ProductFormulaSetupVO) => request({ url: '/product-formula/formulas/' + id + '/options', method: 'put', data }),
  validateOptions: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/options/validate', method: 'put' }),
  variables: (id: string | number) => request<ProductFormulaSetupVO>({ url: '/product-formula/formulas/' + id + '/variables', method: 'get' }),
  saveVariables: (id: string | number, data: ProductFormulaSetupVO) => request({ url: '/product-formula/formulas/' + id + '/variables', method: 'put', data }),
  copyVariables: (id: string | number, sourceFormulaCode: string) => request({ url: '/product-formula/formulas/' + id + '/variables/copy-from-code/' + encodeURIComponent(sourceFormulaCode), method: 'post' }),
  validateVariables: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/variables/validate', method: 'put' }),
  simulation: (id: string | number) => request<ProductFormulaSimulationVO>({ url: '/product-formula/formulas/' + id + '/simulation', method: 'get' }),
  runSimulation: (id: string | number, data?: ProductFormulaSimulationBO) => request<ProductFormulaSimulationVO>({ url: '/product-formula/formulas/' + id + '/simulation/run', method: 'post', data }),
  validateSimulation: (id: string | number, data?: ProductFormulaSimulationBO) => request({ url: '/product-formula/formulas/' + id + '/simulation/validate', method: 'put', data }),
  reviews: (query?: Record<string, unknown>) => requestPage<ProductFormulaVersionVO>({ url: '/product-formula/reviews/list', method: 'get', params: query }),
  review: (reviewId: string | number) => request<ProductFormulaVersionVO>({ url: '/product-formula/reviews/' + reviewId, method: 'get' }),
  approveReview: (reviewId: string | number) => request({ url: '/product-formula/reviews/' + reviewId + '/approve', method: 'put' }),
  rejectReview: (reviewId: string | number, rejectReason: string) => request({ url: '/product-formula/reviews/' + reviewId + '/reject', method: 'put', data: { rejectReason } }),
  validate: (id: string | number) => request({ url: '/product-formula/formulas/' + id + '/validate', method: 'put' }),
  versions: (id: string | number) => request<ProductFormulaVersionVO[]>({ url: '/product-formula/formulas/' + id + '/versions', method: 'get' }),
  version: (id: string | number, versionId: string | number) => request<ProductFormulaVersionVO>({ url: '/product-formula/formulas/' + id + '/versions/' + versionId, method: 'get' })
}

export const productFormulaArchiveApi = {
  list: productFormulaApi.list,
  get: productFormulaApi.get,
  add: productFormulaApi.add,
  update: productFormulaApi.update,
  remove: productFormulaApi.remove,
  editCheck: productFormulaApi.editCheck,
  references: productFormulaApi.references,
  submitReview: productFormulaApi.submitReview,
  stop: productFormulaApi.stop,
  validate: productFormulaApi.validate,
  versions: productFormulaApi.versions,
  version: productFormulaApi.version
}

export const productFormulaMaterialsApi = {
  get: productFormulaApi.materials,
  save: productFormulaApi.saveMaterials,
  validate: productFormulaApi.validateMaterials
}

export const productFormulaOptionsApi = {
  get: productFormulaApi.getFormulaOptions,
  save: productFormulaApi.saveOptions,
  validate: productFormulaApi.validateOptions
}

export const productFormulaVariablesApi = {
  get: productFormulaApi.variables,
  save: productFormulaApi.saveVariables,
  copy: productFormulaApi.copyVariables,
  validate: productFormulaApi.validateVariables
}

export const productFormulaSimulationApi = {
  get: productFormulaApi.simulation,
  run: productFormulaApi.runSimulation,
  validate: productFormulaApi.validateSimulation
}

export const productFormulaReviewApi = {
  list: productFormulaApi.reviews,
  get: productFormulaApi.review,
  approve: productFormulaApi.approveReview,
  reject: productFormulaApi.rejectReview
}
