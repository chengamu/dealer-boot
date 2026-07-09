import { request, requestPage } from '@/utils/request'
import type {
  ExtraFeeRule,
  FabricPriceRule,
  PriceSetupVO,
  PriceValidationIssue,
  SaleProductQuery,
  SaleProductVO,
  ShippingTemplateQuery,
  ShippingTemplateVO
} from './types'

export const saleProductApi = {
  list: (query?: SaleProductQuery) => requestPage<SaleProductVO>({ url: '/product-pricing/sale-products/list', method: 'get', params: query }),
  options: (query?: SaleProductQuery) => request<SaleProductVO[]>({ url: '/product-pricing/sale-products/options', method: 'get', params: query }),
  get: (id: string | number) => request<SaleProductVO>({ url: '/product-pricing/sale-products/' + id, method: 'get' }),
  add: (data: SaleProductVO) => request({ url: '/product-pricing/sale-products', method: 'post', data }),
  update: (data: SaleProductVO) => request({ url: '/product-pricing/sale-products', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-pricing/sale-products/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-pricing/sale-products/change-status/' + id + '/' + status, method: 'put' }),
  references: (id: string | number) => request({ url: '/product-pricing/sale-products/' + id + '/references', method: 'get' })
}

export const productPriceApi = {
  setup: (saleProductId: string | number) => request<PriceSetupVO>({ url: '/product-pricing/price-settings/' + saleProductId, method: 'get' }),
  generateFabricPrices: (saleProductId: string | number, overwrite = false) => request({ url: '/product-pricing/price-settings/' + saleProductId + '/generate-fabric-prices/' + overwrite, method: 'put' }),
  saveFabricRules: (saleProductId: string | number, priceFabricId: string | number, data: FabricPriceRule[]) => request({ url: '/product-pricing/price-settings/' + saleProductId + '/fabrics/' + priceFabricId + '/rules', method: 'put', data }),
  saveExtraFeeRules: (saleProductId: string | number, data: ExtraFeeRule[]) => request({ url: '/product-pricing/price-settings/' + saleProductId + '/fee-rules', method: 'put', data }),
  importShippingTemplate: (saleProductId: string | number, shippingTemplateId: string | number) => request({ url: '/product-pricing/price-settings/' + saleProductId + '/shipping-template/' + shippingTemplateId + '/import', method: 'put' }),
  validate: (saleProductId: string | number) => request<PriceValidationIssue[]>({ url: '/product-pricing/price-settings/' + saleProductId + '/validate', method: 'put' })
}

export const shippingTemplateApi = {
  list: (query?: ShippingTemplateQuery) => requestPage<ShippingTemplateVO>({ url: '/product-pricing/shipping-templates/list', method: 'get', params: query }),
  options: (query?: ShippingTemplateQuery) => request<ShippingTemplateVO[]>({ url: '/product-pricing/shipping-templates/options', method: 'get', params: query }),
  get: (id: string | number) => request<ShippingTemplateVO>({ url: '/product-pricing/shipping-templates/' + id, method: 'get' }),
  add: (data: ShippingTemplateVO) => request({ url: '/product-pricing/shipping-templates', method: 'post', data }),
  update: (data: ShippingTemplateVO) => request({ url: '/product-pricing/shipping-templates', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: '/product-pricing/shipping-templates/' + ids, method: 'delete' }),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-pricing/shipping-templates/change-status/' + id + '/' + status, method: 'put' })
}
