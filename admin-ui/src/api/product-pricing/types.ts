import type { PageQuery } from '@/types/api'
import type { ProductFormulaMaterialVO, ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

export interface SaleProductVO {
  saleProductId?: string | number
  tenantId?: number
  saleProductCode?: string
  saleProductName?: string
  categoryId?: number
  categoryCode?: string
  categoryNameCn?: string
  productTypeCode?: string
  productTypeNameCn?: string
  formulaId?: number
  formulaCode?: string
  formulaName?: string
  formulaVersionId?: number
  formulaVersionNo?: number
  formulaVersionLabel?: string
  minWidthInch?: number
  minHeightInch?: number
  maxWidthInch?: number
  maxHeightInch?: number
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
  categoryId?: number
  categoryCode?: string
  formulaId?: number
  formulaVersionId?: number
  priceStatus?: string
  status?: string
}

export interface PriceFabricCandidate {
  materialId?: string | number
  materialCode?: string
  materialNameCn?: string
  materialNameEn?: string
  materialTypeCode?: string
  materialTypeNameCn?: string
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  unitCode?: string
  specModelText?: string
  unitPrice?: number
  salesPrice?: number
}

export interface PriceSettingVO {
  priceSettingId?: number
  tenantId?: number
  saleProductId?: string | number
  saleProductCode?: string
  saleProductName?: string
  formulaId?: number
  formulaVersionId?: number
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

export interface FabricPriceRule {
  fabricRuleId?: number
  tenantId?: number
  priceFabricId?: number
  priceSettingId?: number
  saleProductId?: string | number
  formulaVersionId?: number
  conditionType?: string
  conditionJson?: string
  conditionExpression?: string
  conditionText?: string
  conditionKey?: string
  priceMode?: string
  unitPrice?: number
  priceFormula?: string
  defaultRuleFlag?: boolean
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
}

export interface PriceFabricVO {
  priceFabricId?: number
  tenantId?: number
  priceSettingId?: number
  saleProductId?: string | number
  formulaVersionId?: number
  materialId?: string | number
  materialCode?: string
  materialNameCn?: string
  unitCode?: string
  status?: string
  sortOrder?: number
  delFlag?: string
  remark?: string
  ruleCount?: number
  defaultRuleFlag?: boolean
}

export interface PriceOptionCombination {
  optionCombinationKey?: string
  optionCombinationName?: string
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
  fabricCandidates?: PriceFabricCandidate[]
  fabricPriceColumns?: PriceOptionCombination[]
  priceFabrics?: PriceFabricVO[]
  fabricRules?: FabricPriceRule[]
  issues?: PriceValidationIssue[]
  formulaMaterials?: ProductFormulaMaterialVO[]
  formulaOptions?: ProductFormulaOptionVO[]
  formulaOptionValues?: ProductFormulaOptionValueVO[]
  formulaOptionMaterials?: ProductFormulaOptionMaterialVO[]
  materialGroupCounts?: Record<string, number>
}

export interface ShippingTemplateRuleVO {
  shippingTemplateRuleId?: string | number
  tenantId?: number
  shippingTemplateId?: string | number
  feeCode?: string
  feeName?: string
  minAreaSqft?: number
  maxAreaSqft?: number
  feeAmount?: number
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
