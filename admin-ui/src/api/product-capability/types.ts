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
  formulaSummaryVisibleFlag?: boolean
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

export interface ProductFormulaVO extends ProductRecord {
  formulaId?: number
  formulaCode?: string
  formulaName?: string
  categoryId?: number
  categoryCode?: string
  categoryNameCn?: string
  productTypeCode?: string
  productTypeNameCn?: string
  minWidthInch?: number
  minHeightInch?: number
  maxWidthInch?: number
  maxHeightInch?: number
  sizeSummary?: string
  materialLineCount?: number
  configuredFlag?: boolean
  currentVersionId?: number
  currentVersionNo?: number
  currentVersionLabel?: string
  draftVersionNo?: number
  latestValidationStatus?: string
  latestValidationMessage?: string
  latestValidationTime?: string
  materialValidationStatus?: string
  materialValidationMessage?: string
  materialValidationTime?: string
  optionValidationStatus?: string
  optionValidationMessage?: string
  optionValidationTime?: string
  simulationValidationStatus?: string
  simulationValidationMessage?: string
  simulationValidationTime?: string
  status?: string
  auditBy?: string
  auditTime?: string
  rejectReason?: string
  sortOrder?: number
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface ProductFormulaQuery extends ProductPageQuery {
  formulaCode?: string
  formulaName?: string
  categoryId?: number
  categoryCode?: string
  productTypeCode?: string
  status?: string
}

export interface ProductFormulaMaterialVO extends ProductRecord {
  formulaMaterialId?: number
  formulaId?: number
  lineNo?: number
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  materialNameEn?: string
  specModelText?: string
  attributeGroupId?: number
  attributeGroupCode?: string
  attributeGroupNameCn?: string
  materialTypeId?: number
  materialTypeCode?: string
  materialTypeNameCn?: string
  unitCode?: string
  defaultFlag?: boolean
  requiredFlag?: boolean
  usageMode?: string
  lengthFormula?: string
  lengthFormulaText?: string
  widthFormula?: string
  widthFormulaText?: string
  heightFormula?: string
  heightFormulaText?: string
  weightFormula?: string
  weightFormulaText?: string
  usageFormula?: string
  fixedUsageQty?: number
  calculationUnitCode?: string
  roundingMode?: string
  minUsageQty?: number
  maxUsageQty?: number
  lossRate?: number
  productionRemark?: string
  status?: string
  sortOrder?: number
  remark?: string
  attributeList?: ProductMaterialAttributeVO[]
}

export interface ProductFormulaOptionVO extends ProductRecord {
  optionId?: number
  formulaId?: number
  optionCode?: string
  optionNameCn?: string
  optionNameEn?: string
  sourceType?: string
  sourceScope?: string
  selectionMode?: string
  defaultValueCode?: string
  defaultValueNameCn?: string
  visibilityMode?: string
  visibleConditionOptionCode?: string
  visibleConditionOptionNameCn?: string
  visibleConditionValueCode?: string
  visibleConditionValueNameCn?: string
  requiredFlag?: boolean
  businessVisibleFlag?: boolean
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaOptionValueVO extends ProductRecord {
  optionValueId?: number
  formulaId?: number
  optionId?: number
  optionCode?: string
  valueCode?: string
  valueNameCn?: string
  valueNameEn?: string
  defaultFlag?: boolean
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaOptionMaterialVO extends ProductRecord {
  optionMaterialId?: number
  formulaId?: number
  optionId?: number
  optionValueId?: number
  optionCode?: string
  valueCode?: string
  formulaMaterialId?: number
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  requiredFlag?: boolean
  defaultFlag?: boolean
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaRestrictionVO extends ProductRecord {
  restrictionId?: number
  formulaId?: number
  restrictionName?: string
  targetOptionCode?: string
  conditionType?: string
  conditionOptionCode?: string
  conditionOperator?: string
  conditionValueCode?: string
  conditionValueNumber?: number
  conditionExpression?: string
  conditionText?: string
  actionType?: string
  targetValueCode?: string
  messageText?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaUsageRuleVO extends ProductRecord {
  usageRuleId?: number
  formulaId?: number
  formulaMaterialId?: number
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  ruleName?: string
  conditionType?: string
  conditionOptionCode?: string
  conditionOptionNameCn?: string
  conditionValueCode?: string
  conditionValueNameCn?: string
  conditionExpression?: string
  conditionText?: string
  conditionKey?: string
  usageMode?: string
  fixedUsageQty?: number
  lengthFormula?: string
  lengthFormulaText?: string
  widthFormula?: string
  widthFormulaText?: string
  heightFormula?: string
  heightFormulaText?: string
  weightFormula?: string
  weightFormulaText?: string
  usageFormula?: string
  usageFormulaText?: string
  calculationUnitCode?: string
  roundingMode?: string
  minUsageQty?: number
  maxUsageQty?: number
  lossRate?: number
  defaultRuleFlag?: boolean
  productionRemark?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaVariableVO extends ProductRecord {
  variableId?: number
  formulaId?: number
  variableKey?: string
  variableCode?: string
  variableName?: string
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaVariableRuleVO extends ProductRecord {
  ruleId?: number
  formulaId?: number
  variableId?: number
  variableKey?: string
  variableCode?: string
  conditionExpression?: string
  conditionText?: string
  valueType?: string
  fixedValue?: number
  formulaExpression?: string
  formulaText?: string
  defaultRuleFlag?: boolean
  sortOrder?: number
  remark?: string
}

export interface ProductFormulaSetupVO extends ProductRecord {
  formula?: ProductFormulaVO
  materials?: ProductFormulaMaterialVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
  optionMaterials?: ProductFormulaOptionMaterialVO[]
  restrictions?: ProductFormulaRestrictionVO[]
  usageRules?: ProductFormulaUsageRuleVO[]
  variables?: ProductFormulaVariableVO[]
  variableRules?: ProductFormulaVariableRuleVO[]
}

export interface ProductFormulaVersionVO extends ProductRecord {
  versionId?: number
  formulaId?: number
  versionNo?: number
  versionLabel?: string
  versionStatus?: string
  formulaSnapshotJson?: string
  setupSnapshotJson?: string
  validationStatus?: string
  validationReportJson?: string
  submitBy?: string
  submitTime?: string
  auditBy?: string
  auditTime?: string
  rejectReason?: string
}

export interface ProductFormulaReviewQuery extends ProductPageQuery {
  formulaCode?: string
  formulaName?: string
  versionLabel?: string
  submitBy?: string
  validationStatus?: string
}

export interface ProductFormulaSimulationBO extends ProductRecord {
  orderWidth?: number
  orderHeight?: number
  orderQuantity?: number
  room?: string
  selectedOptionValues?: Record<string, string>
  saveResult?: boolean
  remark?: string
}

export interface ProductFormulaSimulationItemVO extends ProductRecord {
  formulaMaterialId?: number
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  materialTypeNameCn?: string
  attributeGroupNameCn?: string
  specModelText?: string
  unitCode?: string
  usageQty?: number
  lossRate?: number
  unitPrice?: number
  salesPrice?: number
  amount?: number
  usageSummary?: string
  productionRemark?: string
}

export interface ProductFormulaSimulationVO extends ProductRecord {
  formulaId?: number
  status?: string
  message?: string
  simulationTime?: string
  orderWidth?: number
  orderHeight?: number
  orderQuantity?: number
  room?: string
  selectedOptionValues?: Record<string, string>
  items?: ProductFormulaSimulationItemVO[]
  singleAmount?: number
  totalAmount?: number
}

export interface ProductMaterialAttributeVO extends ProductRecord {
  attributeValueId?: number
  materialId?: number
  materialCode?: string
  attributeId?: number
  attributeCode?: string
  attributeNameCn?: string
  attributeNameEn?: string
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
  salesPrice?: number
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
