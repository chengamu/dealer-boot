import type { PageQuery } from '@/types/api'

export interface ProductRecord {
  [key: string]: unknown
}

export interface ProductPageQuery extends PageQuery {
  [key: string]: unknown
}

export interface ProductOption {
  label: string
  value: string | number
}

export interface ProductDictOption {
  label?: string
  labelCn?: string
  labelEn?: string
  value: string
  dictTypeCode?: string
  parentValue?: string
}

export interface ProductDictTypeVO extends ProductRecord {
  dictTypeId?: number
  dictTypeCode?: string
  dictTypeNameCn?: string
  dictTypeNameEn?: string
  businessDomain?: string
  systemFlag?: boolean
  editableFlag?: boolean
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductDictTypeQuery extends ProductPageQuery {
  dictTypeCode?: string
  dictTypeNameCn?: string
  businessDomain?: string
  status?: string
}

export interface ProductDictItemVO extends ProductRecord {
  dictItemId?: number
  dictTypeCode?: string
  dictItemValue?: string
  dictItemLabelCn?: string
  dictItemLabelEn?: string
  parentValue?: string
  systemFlag?: boolean
  editableFlag?: boolean
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductDictItemQuery extends ProductPageQuery {
  dictTypeCode?: string
  dictItemValue?: string
  dictItemLabelCn?: string
  parentValue?: string
  status?: string
}

export interface ProductCrudApi<TRecord extends ProductRecord = ProductRecord, TQuery extends ProductPageQuery = ProductPageQuery> {
  list: (query?: TQuery) => Promise<{ rows?: TRecord[]; total?: number }>
  options?: (query?: TQuery) => Promise<{ data?: TRecord[] } | TRecord[]>
  tree?: (query?: TQuery) => Promise<{ data?: TRecord[] } | TRecord[]>
  get: (id: string | number) => Promise<{ data?: TRecord }>
  add: (data: TRecord) => Promise<unknown>
  update: (data: TRecord) => Promise<unknown>
  superUpdate?: (data: TRecord) => Promise<unknown>
  remove: (ids: Array<string | number> | string | number) => Promise<unknown>
  changeStatus?: (id: string | number, status: string) => Promise<unknown>
  editCheck?: (id: string | number) => Promise<{ data?: EditCheckResult }>
  references?: (id: string | number) => Promise<{ data?: ReferenceCheckResult }>
}

export interface EditCheckResult {
  editable?: boolean
  reason?: string
  reasonKey?: string
  status?: string
  impactSummary?: string[]
}

export interface ReferenceCheckResult {
  allowed?: boolean
  canRemove?: boolean
  canDisable?: boolean
  referenceCount?: number
  blockerReasonKey?: string
  messageKey?: string
  referenceSummaries?: string[]
  references?: Array<Record<string, unknown>>
}

export interface ProductCategoryVO extends ProductRecord {
  categoryId?: number
  categoryCode?: string
  categoryNameCn?: string
  categoryNameEn?: string
  parentId?: number
  categoryPath?: string
  categoryLevel?: number
  businessType?: string
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductUnitVO extends ProductRecord {
  unitId?: number
  unitCode?: string
  unitNameCn?: string
  unitNameEn?: string
  unitType?: string
  precisionScale?: number
  roundingMode?: string
  baseUnitCode?: string
  conversionRate?: number
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductUnitQuery extends ProductPageQuery {
  unitCode?: string
  unitNameCn?: string
  unitType?: string
  status?: string
}

export interface ProductManufacturerVO extends ProductRecord {
  manufacturerId?: number
  manufacturerCode?: string
  manufacturerName?: string
  manufacturerShortName?: string
  manufacturerFlag?: boolean
  supplierFlag?: boolean
  contactName?: string
  contactPhone?: string
  address?: string
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductManufacturerQuery extends ProductPageQuery {
  manufacturerCode?: string
  manufacturerName?: string
  manufacturerFlag?: boolean
  supplierFlag?: boolean
  status?: string
}

export interface ProductBaseAttributeVO extends ProductRecord {
  attributeId?: number
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  attributeCode?: string
  attributeNameCn?: string
  attributeNameEn?: string
  valueType?: string
  unitCode?: string
  extraJson?: string
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductBaseAttributeQuery extends ProductPageQuery {
  attributeGroupCode?: string
  attributeCode?: string
  attributeNameCn?: string
  status?: string
}

export interface ProductMaterialTypeGroupVO extends ProductRecord {
  groupId?: number
  groupCode?: string
  groupNameCn?: string
  groupNameEn?: string
  systemFlag?: boolean
  editableFlag?: boolean
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductMaterialTypeGroupQuery extends ProductPageQuery {
  groupCode?: string
  groupNameCn?: string
  status?: string
}

export interface ProductMaterialTypeVO extends ProductRecord {
  materialTypeId?: number
  materialTypeCode?: string
  materialTypeNameCn?: string
  materialTypeNameEn?: string
  attributeGroupId?: number
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  systemFlag?: boolean
  editableFlag?: boolean
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductMaterialTypeQuery extends ProductPageQuery {
  materialTypeCode?: string
  materialTypeNameCn?: string
  attributeGroupCode?: string
  status?: string
}

export interface ProductMaterialAttributeVO extends ProductRecord {
  attributeValueId?: number
  materialId?: number
  materialCode?: string
  attributeId?: number
  attributeCode?: string
  attributeNameCn?: string
  valueText?: string
  valueNumber?: number
  valueBool?: string | boolean
  valueUnitCode?: string
  sortOrder?: number
  status?: string
}

export interface ProductMaterialAttributeQuery extends ProductPageQuery {
  materialId?: number
  materialCode?: string
  attributeCode?: string
  attributeNameCn?: string
  status?: string
}

export interface ProductMaterialVO extends ProductRecord {
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  materialNameEn?: string
  materialType?: string
  materialTypeId?: number
  materialTypeCode?: string
  materialTypeNameCn?: string
  attributeGroupId?: number
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  unitCode?: string
  secondaryUnitCode?: string
  manufacturerId?: number
  manufacturerCode?: string
  manufacturerName?: string
  manufacturerItemNo?: string
  model?: string
  spec?: string
  specModelText?: string
  colorName?: string
  weightValue?: number
  unitPrice?: number
  auditBy?: string
  auditTime?: string
  sortOrder?: number
  status?: string
  remark?: string
  attributeList?: ProductMaterialAttributeVO[]
}

export interface ProductMaterialQuery extends ProductPageQuery {
  materialCode?: string
  materialNameCn?: string
  materialType?: string
  materialTypeCode?: string
  attributeGroupCode?: string
  manufacturerId?: number
  manufacturerCode?: string
  manufacturerName?: string
  manufacturerItemNo?: string
  model?: string
  spec?: string
  status?: string
}

export interface ProductChangeLogVO extends ProductRecord {
  changeLogId?: number
  tenantId?: number
  bizModule?: string
  bizType?: string
  bizId?: number
  bizCode?: string
  actionType?: string
  actionName?: string
  beforeJson?: string
  afterJson?: string
  diffJson?: string
  operatorId?: number
  operatorName?: string
  operateTime?: string
  remark?: string
}

export interface ProductChangeLogQuery extends ProductPageQuery {
  bizModule?: string
  bizType?: string
  bizId?: number | string
  bizCode?: string
  actionType?: string
  operatorName?: string
}

export interface ProductMediaAssetVO extends ProductRecord {
  assetId?: number
  assetCode?: string
  assetNameCn?: string
  assetNameEn?: string
  assetType?: string
  usageType?: string
  languageCode?: string
  visibility?: string
  url?: string
  ossId?: number
  versionNo?: number
  legacySource?: string
  legacyId?: string
  legacyPath?: string
  legacyUrl?: string
  status?: string
  remark?: string
}

export interface ProductMediaBindingVO extends ProductRecord {
  bindingId?: number
  assetId?: number
  assetCode?: string
  targetType?: string
  targetId?: number
  targetCode?: string
  usageType?: string
  visibility?: string
  languageCode?: string
  requiredForPublish?: string | boolean
  sortOrder?: number
  status?: string
  remark?: string
}

