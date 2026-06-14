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

export interface SalesProductVO extends ProductRecord {
  salesProductId?: number
  salesProductCode?: string
  salesProductNameCn?: string
  salesProductNameEn?: string
  categoryId?: number
  categoryCode?: string
  categoryNameCn?: string
  categoryNameEn?: string
  productType?: string
  salesMode?: string
  templateId?: number
  templateCode?: string
  templateVersionId?: number
  templateVersionNo?: string
  defaultWidth?: number
  defaultHeight?: number
  dimensionUnit?: string
  bizStatus?: string
  legacySource?: string
  legacyId?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface SalesProductQuery extends ProductPageQuery {
  salesProductCode?: string
  salesProductNameCn?: string
  categoryId?: number
  categoryCode?: string
  productType?: string
  salesMode?: string
  templateCode?: string
  bizStatus?: string
  status?: string
}

export interface ConfigTemplateVO extends ProductRecord {
  templateId?: number
  templateCode?: string
  templateNameCn?: string
  templateNameEn?: string
  productModelId?: number
  productModelCode?: string
  salesProductId?: number
  salesProductCode?: string
  currentVersionId?: number
  currentVersionNo?: string
  publishedVersionId?: number
  publishedVersionNo?: string
  bizStatus?: string
  status?: string
  remark?: string
}

export interface ConfigTemplateVersionVO extends ProductRecord {
  templateVersionId?: number
  templateId?: number
  templateCode?: string
  versionNo?: string
  versionName?: string
  versionStatus?: string
  productModelId?: number
  productModelCode?: string
  salesProductId?: number
  salesProductCode?: string
  salesVariantId?: number
  salesVariantCode?: string
  pricePlanVersionId?: number
  pricePlanCode?: string
  schemaJson?: string
  draftHash?: string
  remark?: string
}

export interface QuestionGroupVO extends ProductRecord {
  questionGroupId?: number
  groupCode?: string
  groupNameCn?: string
  groupNameEn?: string
  descriptionCn?: string
  descriptionEn?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ConfigQuestionVO extends ProductRecord {
  questionId?: number
  templateVersionId?: number
  questionGroupId?: number
  questionCode?: string
  questionNameCn?: string
  questionNameEn?: string
  helpTextCn?: string
  helpTextEn?: string
  inputType?: string
  requiredFlag?: string
  customerVisible?: string
  defaultValue?: string
  validationJson?: string
  displayRuleJson?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ConfigOptionVO extends ProductRecord {
  optionId?: number
  questionId?: number
  templateVersionId?: number
  optionCode?: string
  optionNameCn?: string
  optionNameEn?: string
  optionValue?: string
  sourceType?: string
  sourceRefId?: number
  sourceCode?: string
  sourceName?: string
  displayNameCn?: string
  displayNameEn?: string
  valueCode?: string
  helpTextCn?: string
  helpTextEn?: string
  componentJson?: string
  mediaJson?: string
  ruleJson?: string
  status?: string
  sortOrder?: number
  remark?: string
}

export interface ConfigEvaluationRequest extends ProductRecord {
  salesProductId?: number
  templateVersionId?: number
  width?: number
  height?: number
  selectedOptions?: Record<string, string | number>
  inputValues?: Record<string, unknown>
}

export interface ConfigEvaluationMessage {
  code?: string
  message?: string
  targetType?: string
  targetId?: number
}

export interface DisabledOptionVO {
  questionId?: number
  questionCode?: string
  optionId?: number
  optionCode?: string
  disabledReason?: string
}

export interface ConfigEvaluationResultVO extends ProductRecord {
  salesProductId?: number
  templateVersionId?: number
  resultStatus?: string
  warnings?: ConfigEvaluationMessage[]
  blockers?: ConfigEvaluationMessage[]
  visibleQuestions?: ConfigQuestionVO[]
  availableOptions?: ConfigOptionVO[]
  disabledOptions?: DisabledOptionVO[]
  validations?: ConfigEvaluationMessage[]
  autoComponents?: ProductRecord[]
  mediaAssets?: ProductRecord[]
}

export interface ProductCrudApi<TRecord extends ProductRecord = ProductRecord, TQuery extends ProductPageQuery = ProductPageQuery> {
  list: (query?: TQuery) => Promise<{ rows?: TRecord[]; total?: number }>
  options?: (query?: TQuery) => Promise<{ data?: TRecord[] } | TRecord[]>
  tree?: (query?: TQuery) => Promise<{ data?: TRecord[] } | TRecord[]>
  get: (id: string | number) => Promise<{ data?: TRecord }>
  add: (data: TRecord) => Promise<unknown>
  update: (data: TRecord) => Promise<unknown>
  remove: (ids: Array<string | number> | string | number) => Promise<unknown>
  changeStatus?: (id: string | number, status: string) => Promise<unknown>
  references?: (id: string | number) => Promise<{ data?: ReferenceCheckResult }>
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

export interface ProductBaseAttributeVO extends ProductRecord {
  attributeId?: number
  attributeGroup?: string
  attributeCode?: string
  attributeNameCn?: string
  attributeNameEn?: string
  valueType?: string
  unitCode?: string
  materialTypes?: string
  extraJson?: string
  sortOrder?: number
  status?: string
  remark?: string
}

export interface ProductBaseAttributeQuery extends ProductPageQuery {
  attributeGroup?: string
  attributeCode?: string
  attributeNameCn?: string
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
  businessType?: string
  unitCode?: string
  supplierCode?: string
  supplierName?: string
  factoryModel?: string
  sampleBookNo?: string
  vendorItemNo?: string
  primarySpec?: string
  primaryColor?: string
  primaryWeight?: string
  attributeSummary?: string
  legacySource?: string
  legacyId?: string
  status?: string
  remark?: string
  attributeList?: ProductMaterialAttributeVO[]
}

export interface ProductMaterialQuery extends ProductPageQuery {
  materialCode?: string
  materialNameCn?: string
  materialType?: string
  businessType?: string
  supplierCode?: string
  sampleBookNo?: string
  vendorItemNo?: string
  status?: string
}

export interface FabricSeriesVO extends ProductRecord {
  seriesId?: number
  seriesCode?: string
  seriesNameCn?: string
  seriesNameEn?: string
  materialType?: string
  defaultThicknessUnit?: string
  thicknessRuleEnabled?: string | boolean
  maxThicknessDiff?: number
  maxCombinedThickness?: number
  widthRuleEnabled?: string | boolean
  availableWidths?: string
  minWidthValue?: number
  maxWidthValue?: number
  widthUnit?: string
  status?: string
  remark?: string
}

export interface FabricSeriesQuery extends ProductPageQuery {
  seriesCode?: string
  seriesNameCn?: string
  materialType?: string
  status?: string
}

export interface FabricProfileVO extends ProductRecord {
  fabricId?: number
  fabricCode?: string
  materialId?: number
  materialCode?: string
  seriesId?: number
  seriesCode?: string
  seriesNameCn?: string
  colorCode?: string
  colorName?: string
  materialComposition?: string
  textureType?: string
  finishType?: string
  factoryModel?: string
  sampleBookNo?: string
  vendorItemNo?: string
  supplierCode?: string
  supplierName?: string
  widthValue?: number
  widthUnit?: string
  thicknessValue?: number
  thicknessUnit?: string
  gsmValue?: number
  purchaseUnitCode?: string
  inventoryUnitCode?: string
  salesUnitCode?: string
  legacyAttributeText?: string
  status?: string
  remark?: string
}

export interface FabricProfileQuery extends ProductPageQuery {
  materialCode?: string
  seriesId?: number
  seriesCode?: string
  colorCode?: string
  colorName?: string
  sampleBookNo?: string
  vendorItemNo?: string
  supplierCode?: string
  status?: string
}

export interface ProductComponentItemVO extends ProductRecord {
  componentItemId?: number
  componentId?: number
  componentCode?: string
  materialId?: number
  materialCode?: string
  materialNameCn?: string
  qtyFormula?: string
  defaultQty?: number
  unitCode?: string
  requiredFlag?: string | boolean
  sortOrder?: number
  status?: string
}

export interface ProductComponentItemQuery extends ProductPageQuery {
  componentId?: number
  componentCode?: string
  materialCode?: string
  materialNameCn?: string
  status?: string
}

export interface ProductComponentVO extends ProductRecord {
  componentId?: number
  componentCode?: string
  componentNameCn?: string
  componentNameEn?: string
  componentType?: string
  businessType?: string
  defaultQty?: number
  qtyMode?: string
  unitCode?: string
  materialCode?: string
  materialNameCn?: string
  status?: string
  remark?: string
  itemList?: ProductComponentItemVO[]
}

export interface ProductComponentQuery extends ProductPageQuery {
  componentCode?: string
  componentNameCn?: string
  componentType?: string
  businessType?: string
  status?: string
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

export interface WorkbenchSummary {
  modelCount?: number
  draftCount?: number
  publishedCount?: number
  blockerCount?: number
  warningCount?: number
  pendingSyncCount?: number
  lastSyncTime?: string
}

export interface WorkbenchProgress {
  modelId?: number
  modelCode?: string
  modelName?: string
  categoryName?: string
  templateStatus?: string
  priceStatus?: string
  assetStatus?: string
  publishStatus?: string
  blockerCount?: number
  warningCount?: number
  updatedTime?: string
}

export interface WorkbenchPriority {
  taskId?: number
  taskType?: string
  severity?: string
  targetType?: string
  targetCode?: string
  targetName?: string
  ownerName?: string
  dueTime?: string
  status?: string
}

export interface WorkbenchSyncEvent {
  eventId?: number
  eventType?: string
  targetType?: string
  targetCode?: string
  status?: string
  retryCount?: number
  lastErrorKey?: string
  createdTime?: string
  updatedTime?: string
}
