import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from '@/api/product-capability/types'
import type { BusinessNumberMode } from '@/utils/businessNumber'

export type ProductGridRowTone = 'danger' | 'warning' | 'success' | 'muted'

export interface ProductFieldConfig {
  prop: string
  labelKey: string
  type?: 'text' | 'textarea' | 'number' | 'inch' | 'status' | 'date' | 'datetime' | 'url' | 'select' | 'boolean' | 'remote-select' | 'tree-select' | 'material-attributes'
  options?: Array<{ label?: string; value?: string | number; record?: ProductRecord }>
  optionLoader?: (form: ProductRecord) => Promise<Array<{ label?: string; value?: string | number; record?: ProductRecord }>>
  fillFields?: Record<string, string | undefined>
  clearFields?: string[]
  multiple?: boolean
  valueMode?: 'raw' | 'csv'
  visible?: (form: ProductRecord) => boolean
  readonly?: (form: ProductRecord) => boolean
  sectionKey?: string
  sectionLabelKey?: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  multiline?: boolean
  sortable?: boolean
  sortProp?: string
  precision?: number
  numberMode?: BusinessNumberMode
  unitPrecision?: number
  currencyDigits?: number
  min?: number
  max?: number
  denominator?: number
  step?: number
  search?: boolean
  table?: boolean
  form?: boolean
  required?: boolean
  formSpan?: 1 | 2
  onChange?: (value: unknown, form: ProductRecord) => void
  formatter?: (value: unknown, row: ProductRecord) => string
}

export interface ProductGridConfig {
  key: string
  titleKey: string
  descriptionKey: string
  idKey: string
  permissions: {
    add: string
    edit: string
    remove: string
    reference: string
  }
  superEditPermission?: string
  fields: ProductFieldConfig[]
  readonly?: boolean
  singleRowActions?: boolean
  submitFields?: string[]
  initialQuery?: ProductPageQuery
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  defaultRecord?: ProductRecord
  attachments?: {
    targetType: string
    targetCodeField: string
    defaultUsageType?: string
  }
  changeLog?: {
    bizModule?: string
    bizType: string
    permission: string
    titleKey?: string
  }
  hideReference?: boolean
  showDetail?: boolean
  closePath?: string
  rowTone?: (row: ProductRecord) => ProductGridRowTone | undefined
  rowClassName?: (row: ProductRecord) => string
  rowActions?: Array<{
    labelKey: string
    icon?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    permission: string
    primary?: boolean
    visible?: (row: ProductRecord) => boolean
    disabled?: (row: ProductRecord) => boolean
    handler: (row: ProductRecord) => void | Promise<void>
  }>
  toolbarActions?: Array<{
    labelKey: string
    icon?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    permission: string
    handler: () => void | Promise<void>
  }>
  optionLoaders?: Record<string, (form: ProductRecord) => Promise<Array<{ label?: string; value?: string | number; record?: ProductRecord }>>>
  tree?: {
    parentKey: string
    rowKey?: string
    rootValue?: string | number
    treeProps?: { children?: string }
  }
  api: {
    list: (query?: ProductPageQuery) => Promise<{ rows?: ProductRecord[]; total?: number }>
    get: (id: string | number) => Promise<{ data?: ProductRecord }>
    add: (data: ProductRecord) => Promise<unknown>
    update: (data: ProductRecord) => Promise<unknown>
    superUpdate?: (data: ProductRecord) => Promise<unknown>
    remove: (ids: Array<string | number> | string | number) => Promise<unknown>
    changeStatus?: (id: string | number, status: string) => Promise<unknown>
    editCheck?: (id: string | number) => Promise<{ data?: { editable?: boolean; reason?: string; reasonKey?: string; impactSummary?: string[] } }>
    references?: (id: string | number) => Promise<{ data?: ReferenceCheckResult }>
  }
}
