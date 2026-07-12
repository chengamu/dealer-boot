import type { DecimalValue, PageQuery } from '@/types/api'
import type { ProductFormulaMaterialVO, ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

export interface SaleProductVO {
  saleProductId?: string
  tenantId?: number
  saleProductCode?: string
  saleProductName?: string
  categoryId?: string
  categoryCode?: string
  categoryNameCn?: string
  productTypeCode?: string
  productTypeNameCn?: string
  formulaId?: string
  formulaCode?: string
  formulaName?: string
  formulaVersionId?: string
  formulaVersionNo?: number
  formulaVersionLabel?: string
  minWidthInch?: DecimalValue
  minHeightInch?: DecimalValue
  maxWidthInch?: DecimalValue
  maxHeightInch?: DecimalValue
  sizeSummary?: string
  priceStatus?: string
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateTime?: string
  updateBy?: string
}

export interface SaleProductQuery extends PageQuery {
  saleProductCode?: string
  saleProductName?: string
  categoryId?: string
  categoryCode?: string
  formulaId?: string
  formulaVersionId?: string
  priceStatus?: string
  status?: string
}

export interface PriceSettingVO {
  priceSettingId?: string
  tenantId?: number
  saleProductId?: string
  saleProductCode?: string
  saleProductName?: string
  formulaId?: string
  formulaVersionId?: string
  formulaVersionLabel?: string
  currencyCode?: string
  validationStatus?: string
  validationMessage?: string
  validationTime?: string
  status?: string
  remark?: string
  updateTime?: string
  updateBy?: string
}

export interface MaterialPriceRule {
  materialRuleId?: string
  tenantId?: number
  priceMaterialId?: string
  priceSettingId?: string
  saleProductId?: string
  formulaVersionId?: string
  conditionType?: string
  conditionJson?: string
  conditionExpression?: string
  conditionText?: string
  conditionKey?: string
  priceMode?: string
  unitPrice?: DecimalValue
  priceFormula?: string
  defaultRuleFlag?: boolean
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
}

export interface PriceMaterialVO {
  priceMaterialId?: string
  tenantId?: number
  priceSettingId?: string
  saleProductId?: string
  formulaVersionId?: string
  formulaMaterialId?: string
  materialId?: string
  materialCode?: string
  materialNameCn?: string
  specModelText?: string
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  materialTypeCode?: string
  materialTypeNameCn?: string
  unitCode?: string
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
  ruleCount?: number
  defaultRuleFlag?: boolean
}

export interface PriceValidationIssue {
  level: 'PASS' | 'WARNING' | 'ERROR'
  sourceType?: string
  sourceCode?: string
  messageKey?: string
  message?: string
}

export interface PriceSetupVO {
  saleProduct?: SaleProductVO
  setting?: PriceSettingVO
  priceMaterials?: PriceMaterialVO[]
  materialRules?: MaterialPriceRule[]
  issues?: PriceValidationIssue[]
  formulaMaterials?: ProductFormulaMaterialVO[]
  formulaOptions?: ProductFormulaOptionVO[]
  formulaOptionValues?: ProductFormulaOptionValueVO[]
  formulaOptionMaterials?: ProductFormulaOptionMaterialVO[]
  materialGroupCounts?: Record<string, number>
}

export interface MaterialPriceBatchRule {
  priceMaterialIds: string[]
  rules: MaterialPriceRule[]
}

export interface ProductPriceQuoteRequest {
  orderWidth: number
  orderHeight: number
  orderQuantity?: number
  selectedOptionValues?: Record<string, string>
}

export interface ProductPriceQuoteItem {
  priceMaterialId?: string
  materialId?: string
  materialCode?: string
  materialNameCn?: string
  attributeGroupNameCn?: string
  unitCode?: string
  usageQty?: DecimalValue
  unitPrice?: DecimalValue
  matchedConditionText?: string
  priceFormula?: string
  amount?: DecimalValue
}

export interface ProductPriceQuote {
  saleProductId?: string
  formulaVersionId?: string
  currencyCode?: string
  orderQuantity?: number
  singleAmount?: DecimalValue
  totalAmount?: DecimalValue
  items?: ProductPriceQuoteItem[]
}

export interface ShippingTemplateRuleVO {
  shippingTemplateRuleId?: string | number
  tenantId?: number
  shippingTemplateId?: string | number
  feeCode?: string
  feeName?: string
  minAreaSqft?: DecimalValue
  maxAreaSqft?: DecimalValue
  feeAmount?: DecimalValue
  sortOrder?: number
  delFlag?: string
  remark?: string
}

export interface ShippingTemplateVO {
  shippingTemplateId?: string | number
  tenantId?: number
  templateCode?: string
  templateName?: string
  currencyCode?: string
  defaultFlag?: boolean
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
  updateTime?: string
  updateBy?: string
  ruleCount?: number
  rules?: ShippingTemplateRuleVO[]
}

export interface ShippingTemplateQuery extends PageQuery {
  templateCode?: string
  templateName?: string
  status?: string
}
