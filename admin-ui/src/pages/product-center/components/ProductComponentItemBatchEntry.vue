<template>
  <el-drawer v-model="visible" :title="t('productCenter.componentItem.batchEntry')" size="100%" append-to-body destroy-on-close class="product-component-item-batch">
    <div class="product-component-item-batch__toolbar">
      <el-space>
        <el-button type="primary" plain icon="Plus" @click="addRow">{{ t('common.add') }}</el-button>
        <el-button type="danger" plain icon="Delete" @click="removeSelectedRows">{{ t('common.delete') }}</el-button>
      </el-space>
      <span>{{ t('productCenter.componentItem.batchEntryHint') }}</span>
    </div>
    <div class="product-component-item-batch__table">
      <VxeTable
        ref="tableRef"
        border
        stripe
        show-overflow
        height="100%"
        :data="rows"
        :column-config="{ resizable: true, useKey: true, drag: true }"
        :column-drag-config="{ trigger: 'cell', showIcon: false }"
        :row-config="{ keyField: '__rowKey' }"
        :edit-config="{ trigger: 'click', mode: 'cell', autoClear: false }"
        :keyboard-config="{ isArrow: true }"
      >
        <VxeColumn type="checkbox" width="48" fixed="left" />
        <VxeColumn type="seq" :title="t('common.index')" width="64" fixed="left" />
        <VxeColumn field="componentCode" :title="t('productCenter.component.code')" min-width="220" :edit-render="componentSelectRender" />
        <VxeColumn field="materialCode" :title="t('productCenter.componentItem.materialCode')" min-width="220" :edit-render="materialSelectRender" />
        <VxeColumn field="materialNameCn" :title="t('productCenter.componentItem.materialNameCn')" min-width="180">
          <template #default="{ row }">
            <span class="product-component-item-batch__readonly">{{ displayValue(row.materialNameCn) }}</span>
          </template>
        </VxeColumn>
        <VxeColumn field="qtyFormula" :title="t('productCenter.componentItem.qtyFormula')" min-width="180" :edit-render="{}">
          <template #edit="{ row }">
            <el-input v-model="row.qtyFormula" />
          </template>
        </VxeColumn>
        <VxeColumn field="defaultQty" :title="t('productCenter.componentItem.defaultQty')" width="140" :edit-render="{}">
          <template #edit="{ row }">
            <el-input-number v-model="row.defaultQty" :min="0" controls-position="right" />
          </template>
        </VxeColumn>
        <VxeColumn field="unitCode" :title="t('productCenter.componentItem.unitCode')" width="150">
          <template #default="{ row }">
            <span class="product-component-item-batch__readonly">{{ optionLabel(unitCodeField, row.unitCode) }}</span>
          </template>
        </VxeColumn>
        <VxeColumn field="requiredFlag" :title="t('productCenter.componentItem.requiredFlag')" width="120" :edit-render="{}">
          <template #default="{ row }">{{ booleanLabel(row.requiredFlag) }}</template>
          <template #edit="{ row }">
            <el-switch v-model="row.requiredFlag" />
          </template>
        </VxeColumn>
        <VxeColumn field="sortOrder" :title="t('productCenter.common.sortOrder')" width="120" :edit-render="{}">
          <template #edit="{ row }">
            <el-input-number v-model="row.sortOrder" :min="0" controls-position="right" />
          </template>
        </VxeColumn>
        <VxeColumn field="status" :title="t('productCenter.common.status')" width="130" :edit-render="statusSelectRender" />
      </VxeTable>
    </div>
    <template #footer>
      <div class="product-component-item-batch__actions">
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveRows">{{ t('productCenter.componentItem.saveRows') }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VxeColumn, VxeTable } from 'vxe-table'
import 'vxe-pc-ui/es/vxe-select'
import 'vxe-table/es/table/style.css'
import 'vxe-table/es/column/style.css'
import 'vxe-pc-ui/es/vxe-input/style.css'
import 'vxe-pc-ui/es/vxe-select/style.css'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductPageQuery, ProductRecord } from '@/api/product-capability/types'
import type { ProductFieldConfig, ProductGridConfig } from './ProductEntityGridPage.vue'

const props = defineProps<{
  modelValue: boolean
  config: ProductGridConfig
  queryParams: ProductPageQuery
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll('{' + name + '}', String(value)), message)
}

const rows = ref<ProductRecord[]>([])
const saving = ref(false)
const remoteOptions = ref<Record<string, Array<{ label?: string; value?: string | number; record?: ProductRecord }>>>({})
const tableRef = ref<{
  clearEdit?: () => Promise<void>
  getCheckboxRecords?: () => ProductRecord[]
  scrollToRow?: (row: ProductRecord, field?: string) => Promise<void>
  setEditCell?: (row: ProductRecord, field: string) => Promise<void>
}>()
const minEntryRows = 10
const selectEditProps = {
  filterable: true,
  transfer: false,
  optionProps: { label: 'label', value: 'value' },
  virtualYConfig: { enabled: true, gt: 20 }
}

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
const allFormFields = computed(() => props.config.fields.filter((field) => field.form !== false && field.type !== 'datetime' && field.type !== 'status'))
const componentCodeField = computed(() => props.config.fields.find((field) => field.prop === 'componentCode'))
const materialCodeField = computed(() => props.config.fields.find((field) => field.prop === 'materialCode'))
const unitCodeField = computed(() => props.config.fields.find((field) => field.prop === 'unitCode'))
const defaultComponentCode = computed(() => props.queryParams.componentCode || props.config.initialQuery?.componentCode)
const componentOptions = computed(() => componentCodeField.value ? displaySelectOptions(fieldOptions(componentCodeField.value)) : [])
const materialOptions = computed(() => materialCodeField.value ? displaySelectOptions(fieldOptions(materialCodeField.value)) : [])
const statusOptions = computed(() => [
  { label: t('productCenter.status.enabled'), value: 'ENABLED' },
  { label: t('productCenter.status.disabled'), value: 'DISABLED' }
])
const componentSelectRender = computed(() => ({
  name: 'VxeSelect',
  options: componentOptions.value,
  props: selectEditProps,
  optionProps: selectEditProps.optionProps,
  events: {
    change: ({ row }: { row: ProductRecord }, params: { value?: unknown }) => handleComponentChange(row, params?.value)
  }
}))
const materialSelectRender = computed(() => ({
  name: 'VxeSelect',
  options: materialOptions.value,
  props: selectEditProps,
  optionProps: selectEditProps.optionProps,
  events: {
    change: ({ row }: { row: ProductRecord }, params: { value?: unknown }) => handleMaterialChange(row, params?.value)
  }
}))
const statusSelectRender = computed(() => ({
  name: 'VxeSelect',
  options: statusOptions.value,
  props: selectEditProps,
  optionProps: selectEditProps.optionProps,
  events: {
    change: finishEdit
  }
}))

function fieldOptions(field: ProductFieldConfig) {
  return remoteOptions.value[field.prop] || field.options || []
}

function displaySelectOptions(options: Array<{ label?: string; value?: string | number; record?: ProductRecord }>) {
  return options.map((option) => {
    const value = String(option.value ?? '')
    const label = String(option.label ?? option.value ?? '')
    if (!value || !label.startsWith(value)) return option
    const name = label.slice(value.length).trim()
    return name ? { ...option, label: `${value} · ${name}` } : option
  })
}

function displayValue(value: unknown) {
  if (Array.isArray(value)) return value.length ? value.join(', ') : '-'
  if (value && typeof value === 'object') return JSON.stringify(value)
  return String(value ?? '-')
}

function optionLabel(field: ProductFieldConfig | undefined, value: unknown) {
  if (!field) return displayValue(value)
  const option = fieldOptions(field).find((item) => item.value === value)
  return option?.label || displayValue(value)
}

function booleanLabel(value: unknown) {
  return value === '1' || value === 'true' || value === true || value === 1 ? t('common.yes') : t('common.no')
}

function normalizeStatus(value: unknown) {
  return value === 'ENABLED' || value === '1' || value === 'true' || value === true || value === 1 ? 'ENABLED' : 'DISABLED'
}

function normalizeNumberValue(value: unknown) {
  if (value === null || value === undefined || value === '') return value
  const next = Number(value)
  return Number.isFinite(next) ? next : value
}

function normalizeFormRecord(record?: ProductRecord) {
  const next: ProductRecord = { ...(record || {}) }
  allFormFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = normalizeNumberValue(next[field.prop])
    if (field.type === 'boolean') next[field.prop] = next[field.prop] === '1' || next[field.prop] === 'true' || next[field.prop] === true || next[field.prop] === 1
  })
  return next
}

async function loadRemoteOptions() {
  const entries = await Promise.all(allFormFields.value.filter((field) => field.optionLoader).map(async (field) => {
    const options = await field.optionLoader?.({})
    return [field.prop, options || []] as const
  }))
  remoteOptions.value = Object.fromEntries(entries)
}

function nextRowKey() {
  return `batch-${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function normalizeRow(row?: ProductRecord, index = rows.value.length) {
  return normalizeFormRecord({
    __rowKey: row?.__rowKey || row?.[props.config.idKey] || nextRowKey(),
    status: 'ENABLED',
    requiredFlag: false,
    defaultQty: 1,
    sortOrder: (index + 1) * 10,
    componentCode: defaultComponentCode.value,
    ...(row || {})
  })
}

async function addRow() {
  const row = normalizeRow()
  rows.value.push(row)
  await nextTick()
  await tableRef.value?.scrollToRow?.(row, 'componentCode')
  await tableRef.value?.setEditCell?.(row, 'componentCode')
}

function removeSelectedRows() {
  const selectedRows = tableRef.value?.getCheckboxRecords?.() || []
  if (!selectedRows.length) return
  const selectedKeys = new Set(selectedRows.map((row) => row.__rowKey))
  rows.value = rows.value.filter((row) => !selectedKeys.has(row.__rowKey))
  if (!rows.value.length) addRow()
}

function handleComponentChange(row: ProductRecord, value: unknown) {
  if (!componentCodeField.value) return
  fillFields(row, componentCodeField.value, value)
  finishEdit()
}

function handleMaterialChange(row: ProductRecord, value: unknown) {
  if (!materialCodeField.value) return
  fillFields(row, materialCodeField.value, value)
  finishEdit()
}

function finishEdit() {
  nextTick(() => tableRef.value?.clearEdit?.())
}

function fillFields(row: ProductRecord, field: ProductFieldConfig, value: unknown) {
  const option = fieldOptions(field).find((item) => item.value === value)
  if (!option?.record || !field.fillFields) return
  Object.entries(field.fillFields).forEach(([target, source]) => {
    if (source) row[target] = option.record?.[source]
  })
}

function buildPayload(row: ProductRecord) {
  const payload = { ...row }
  delete payload.__rowKey
  payload.defaultQty = normalizeNumberValue(payload.defaultQty)
  payload.sortOrder = normalizeNumberValue(payload.sortOrder)
  payload.requiredFlag = payload.requiredFlag === true || payload.requiredFlag === '1' || payload.requiredFlag === 'true' ? '1' : '0'
  payload.status = normalizeStatus(payload.status || 'ENABLED')
  return payload
}

function isEmptyDraftRow(row: ProductRecord) {
  return !row[props.config.idKey] && !row.materialCode && !row.qtyFormula && (!row.componentCode || row.componentCode === defaultComponentCode.value)
}

async function loadRows() {
  await loadRemoteOptions()
  const response = await props.config.api.list({ ...props.queryParams, pageNum: 1, pageSize: 999 })
  const sourceRows = response.rows || []
  const nextRows = sourceRows.map((row, index) => normalizeRow(row, index))
  while (nextRows.length < minEntryRows) {
    nextRows.push(normalizeRow(undefined, nextRows.length))
  }
  rows.value = nextRows
}

async function saveRows() {
  const savableRows = rows.value.filter((row) => !isEmptyDraftRow(row))
  const invalidRow = savableRows.find((row) => !row.componentCode || !row.materialCode)
  if (invalidRow) {
    ElMessage.warning(t('productCenter.componentItem.batchEntryRequired'))
    return
  }
  if (!savableRows.length) {
    ElMessage.warning(t('productCenter.componentItem.batchEntryRequired'))
    return
  }
  saving.value = true
  try {
    for (const row of savableRows) {
      const payload = buildPayload(row)
      if (payload[props.config.idKey]) {
        await props.config.api.update(payload)
      } else {
        await props.config.api.add(payload)
      }
    }
    ElMessage.success(t('common.save.success'))
    visible.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}

watch(visible, (value) => {
  if (value) loadRows()
})
</script>

<style scoped lang="scss">
.product-component-item-batch__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.product-component-item-batch {
  --vxe-ui-layout-background-color: var(--el-bg-color);
  --vxe-ui-base-popup-border-color: var(--el-border-color-light);
  --vxe-ui-base-popup-box-shadow: var(--el-box-shadow-light);
  --vxe-ui-base-border-radius: 6px;
  --vxe-ui-font-primary-color: var(--el-color-primary);
  --vxe-ui-select-option-hover-background-color: var(--el-fill-color-light);
  --vxe-ui-select-option-height-default: 36px;

  :deep(.vxe-table) {
    border-radius: 6px;
    --vxe-ui-table-header-background-color: var(--el-fill-color-light);
    --vxe-ui-table-header-font-color: var(--el-text-color-primary);
    --vxe-ui-table-border-color: var(--el-border-color-lighter);
    --vxe-ui-font-color: var(--el-text-color-regular);
  }

  :deep(.el-input),
  :deep(.el-input-number),
  :deep(.vxe-select),
  :deep(.vxe-input) {
    width: 100%;
  }

  :deep(.vxe-input--wrapper) {
    height: 32px;
    min-height: 32px;
    border-color: var(--el-border-color);
    border-radius: var(--el-border-radius-base);
    background-color: var(--el-fill-color-blank);
    color: var(--el-text-color-regular);
    font-size: var(--el-font-size-base);
  }

  :deep(.vxe-input--wrapper:hover) {
    border-color: var(--el-border-color-hover);
  }

  :deep(.is--active > .vxe-input--wrapper),
  :deep(.vxe-input--wrapper.is--focus) {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }

  :deep(.vxe-input--inner) {
    color: var(--el-text-color-regular);
    font-size: var(--el-font-size-base);
  }
}

:global(.product-component-item-batch .el-drawer__body) {
  display: flex;
  flex-direction: column;
  padding: 16px 20px 0;
  background: var(--el-bg-color-page);
}

:global(.product-component-item-batch .el-drawer__footer) {
  padding: 12px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.product-component-item-batch__table {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.product-component-item-batch__readonly {
  color: var(--el-text-color-secondary);
}

.product-component-item-batch__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:global(.product-component-item-batch .vxe-select--panel-wrapper) {
  min-width: 520px;
  max-width: min(520px, calc(100vw - 48px));
  max-height: 280px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
  color: var(--el-text-color-regular);
  font-size: var(--el-font-size-base);
  opacity: 1;
  overflow: hidden;
}

:global(.product-component-item-batch .vxe-select--panel-search) {
  padding: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

:global(.product-component-item-batch .vxe-select-option) {
  height: 36px;
  line-height: 36px;
  background: var(--el-bg-color);
  color: var(--el-text-color-regular);
}

:global(.product-component-item-batch .vxe-select-option--label) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.product-component-item-batch .vxe-select-option.is--selected) {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

:global(.product-component-item-batch .vxe-select-option:not(.is--disabled).is--hover) {
  background-color: var(--el-fill-color-light);
}
</style>
