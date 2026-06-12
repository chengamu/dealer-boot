<template>
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px" class="product-grid-page__search">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop">
        <el-select
          v-if="field.type === 'status' || field.type === 'select' || field.type === 'boolean'"
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.selectPlaceholder')"
          clearable
          filterable
          style="width: 180px"
        >
          <template v-if="field.type === 'status'">
            <el-option :label="t('productCenter.status.enabled')" value="ENABLED" />
            <el-option :label="t('productCenter.status.disabled')" value="DISABLED" />
          </template>
          <template v-else-if="field.type === 'boolean'">
            <el-option :label="t('common.yes')" :value="true" />
            <el-option :label="t('common.no')" :value="false" />
          </template>
          <template v-else>
            <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
          </template>
        </el-select>
        <el-input
          v-else
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 product-grid-page__toolbar">
      <el-col :span="1.5">
        <el-button v-if="!config.readonly" type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="[config.permissions.add]">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-if="!config.readonly" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="[config.permissions.edit]">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-if="!config.readonly" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="[config.permissions.remove]">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col v-for="action in config.toolbarActions || []" :key="action.labelKey" :span="1.5">
        <el-button :type="action.type || 'primary'" plain :icon="action.icon" @click="action.handler" v-hasPermi="[action.permission]">
          {{ t(action.labelKey) }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      :data-testid="`product-grid-${config.key}`"
      class="product-grid-page__table"
      @selection-change="handleSelectionChange"
      @row-dblclick="handleRowDblclick"
    >
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :index="rowIndex" :label="t('common.index')" width="64" align="center" fixed />
      <el-table-column
        v-for="field in tableFields"
        :key="field.prop"
        :label="t(field.labelKey)"
        :prop="field.prop"
        :width="field.width"
        :min-width="field.minWidth || defaultColumnMinWidth(field)"
        :resizable="true"
        :align="field.align || 'center'"
        :show-overflow-tooltip="field.type !== 'status' && field.type !== 'url'"
      >
        <template #default="{ row }">
          <el-switch
            v-if="field.type === 'status'"
            :model-value="statusSwitchValue(row[field.prop])"
            active-value="ENABLED"
            inactive-value="DISABLED"
            :disabled="config.readonly || !config.api.changeStatus"
            @change="handleStatusChange(row, field, $event)"
          />
          <span v-else-if="field.type === 'select'">{{ optionLabel(field, row[field.prop]) }}</span>
          <span v-else-if="field.type === 'boolean'">{{ booleanLabel(row[field.prop]) }}</span>
          <span v-else-if="field.type === 'datetime'">{{ formatUtc(row[field.prop] as string | undefined) }}</span>
          <el-link v-else-if="field.type === 'url' && row[field.prop]" type="primary" :href="String(row[field.prop])" target="_blank">
            {{ t('productCenter.common.open') }}
          </el-link>
          <span v-else>{{ displayValue(row[field.prop]) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="176" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-tooltip v-if="!config.hideReference" :content="t('productCenter.common.references')" placement="top">
            <el-button link type="primary" icon="View" :aria-label="t('productCenter.common.references')" @click="handleReference(row)" v-hasPermi="[config.permissions.reference]" />
          </el-tooltip>
          <el-tooltip v-if="config.showDetail" :content="t('common.detail')" placement="top">
            <el-button link type="primary" icon="Document" :aria-label="t('common.detail')" @click="handleDetail(row)" v-hasPermi="[config.permissions.reference]" />
          </el-tooltip>
          <el-tooltip v-if="!config.readonly" :content="t('common.edit')" placement="top">
            <el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" @click="handleUpdate(row)" v-hasPermi="[config.permissions.edit]" />
          </el-tooltip>
          <el-tooltip v-if="!config.readonly" :content="t('common.delete')" placement="top">
            <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click="handleDelete(row)" v-hasPermi="[config.permissions.remove]" />
          </el-tooltip>
          <el-tooltip v-for="action in config.rowActions || []" :key="action.labelKey" :content="t(action.labelKey)" placement="top">
            <el-button
              link
              :type="action.type || 'primary'"
              :icon="action.icon"
              :aria-label="t(action.labelKey)"
              :loading="rowActionLoading === rowActionKey(action, row)"
              :disabled="Boolean(rowActionLoading) || action.disabled?.(row)"
              @click="handleRowAction(action, row)"
              v-hasPermi="[action.permission]"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-drawer v-model="open" :title="drawerTitle" size="84%" append-to-body destroy-on-close @closed="reset">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="136px" class="product-grid-page__form">
        <el-form-item v-for="field in formFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop" :class="formItemClass(field)">
          <el-input-number v-if="field.type === 'number'" v-model="form[field.prop] as number" :min="0" :disabled="drawerReadonly" controls-position="right" class="product-grid-page__number" />
          <el-switch
            v-else-if="field.type === 'boolean'"
            :model-value="Boolean(form[field.prop])"
            :active-value="true"
            :inactive-value="false"
            :disabled="drawerReadonly"
            @change="handleBooleanChange(field, $event)"
          />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" :disabled="drawerReadonly" filterable clearable style="width: 100%">
            <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop] as string" type="textarea" :rows="3" :disabled="drawerReadonly" :placeholder="t('productCenter.common.inputPlaceholder')" />
          <el-input v-else v-model="form[field.prop] as string" :disabled="drawerReadonly" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <div v-if="attachmentEnabled" v-loading="attachmentLoading" class="product-grid-page__attachments">
          <div class="product-grid-page__attachments-head">
            <span>{{ t('productCenter.common.attachments') }}</span>
            <el-upload
              v-if="!drawerReadonly && currentRecordId"
              :show-file-list="false"
              :http-request="uploadAttachment"
              :before-upload="beforeAttachmentUpload"
            >
              <el-button type="primary" plain icon="Upload">{{ t('productCenter.common.uploadAttachment') }}</el-button>
            </el-upload>
          </div>
          <el-alert
            v-if="!currentRecordId"
            :title="t('productCenter.common.saveBeforeUpload')"
            type="info"
            show-icon
            :closable="false"
          />
          <el-table v-else-if="attachmentRows.length" :data="attachmentRows" border size="small">
            <el-table-column :label="t('productCenter.asset.code')" prop="assetCode" min-width="160" show-overflow-tooltip />
            <el-table-column :label="t('productCenter.asset.usageType')" prop="usageType" width="120" align="center" />
            <el-table-column :label="t('common.operate')" width="120" align="center">
              <template #default="{ row }">
                <el-tooltip :content="t('productCenter.common.open')" placement="top">
                  <el-button link type="primary" icon="View" :aria-label="t('productCenter.common.open')" @click="openAttachment(row)" />
                </el-tooltip>
                <el-tooltip v-if="!drawerReadonly" :content="t('common.delete')" placement="top">
                  <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click="removeAttachment(row)" />
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t('productCenter.common.noAttachments')" />
        </div>
      </el-form>
      <template #footer>
        <div class="product-grid-page__drawer-actions">
          <el-button @click="cancel">{{ drawerReadonly ? t('common.close') : t('common.cancel') }}</el-button>
          <el-button v-if="!drawerReadonly" type="primary" :loading="submitLoading" @click="submitForm">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="referenceOpen" :title="t('productCenter.common.references')" size="420px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('productCenter.common.canRemove')">
          {{ referenceAllowed ? t('common.yes') : t('common.no') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.canDisable')">
          {{ referenceAllowed ? t('common.yes') : t('common.no') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.referenceCount')">
          {{ referenceResult.referenceCount ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        v-if="referenceResult.blockerReasonKey"
        class="product-grid-page__reference-alert"
        :title="t(referenceResult.blockerReasonKey)"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-table v-if="referenceSummaries.length" :data="referenceSummaries" border class="product-grid-page__reference-table">
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column :label="t('productCenter.common.referenceSummary')" prop="summary" show-overflow-tooltip />
      </el-table>
      <el-empty v-else :description="t('productCenter.common.noReferences')" />
    </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productMediaAssetApi, productMediaBindingApi } from '@/api/product-capability/asset'
import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from '@/api/product-capability/types'
import service from '@/utils/request'
import { getApiBaseUrl } from '@/utils/config'

export interface ProductFieldConfig {
  prop: string
  labelKey: string
  type?: 'text' | 'textarea' | 'number' | 'status' | 'datetime' | 'url' | 'select' | 'boolean'
  options?: Array<{ label?: string; value?: string | number }>
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  search?: boolean
  table?: boolean
  form?: boolean
  required?: boolean
  formSpan?: 1 | 2
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
  fields: ProductFieldConfig[]
  readonly?: boolean
  initialQuery?: ProductPageQuery
  defaultRecord?: ProductRecord
  attachments?: {
    targetType: string
    targetCodeField: string
    defaultUsageType?: string
  }
  hideReference?: boolean
  showDetail?: boolean
  rowActions?: Array<{
    labelKey: string
    icon?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    permission: string
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
  api: {
    list: (query?: ProductPageQuery) => Promise<{ rows?: ProductRecord[]; total?: number }>
    get: (id: string | number) => Promise<{ data?: ProductRecord }>
    add: (data: ProductRecord) => Promise<unknown>
    update: (data: ProductRecord) => Promise<unknown>
    remove: (ids: Array<string | number> | string | number) => Promise<unknown>
    changeStatus?: (id: string | number, status: string) => Promise<unknown>
    references?: (id: string | number) => Promise<{ data?: ReferenceCheckResult }>
  }
}

const props = defineProps<{
  config: ProductGridConfig
}>()

const route = useRoute()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll('{' + name + '}', String(value)), message)
}

const rows = ref<ProductRecord[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const referenceOpen = ref(false)
const drawerReadonly = ref(false)
const rowActionLoading = ref('')
const ids = ref<Array<string | number>>([])
const total = ref(0)
const form = ref<ProductRecord>({})
const referenceResult = ref<ReferenceCheckResult>({})
const attachmentRows = ref<ProductRecord[]>([])
const attachmentLoading = ref(false)
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<ProductPageQuery>({
  pageNum: 1,
  pageSize: 10
})

const searchFields = computed(() => props.config.fields.filter((field) => field.search))
const tableFields = computed(() => props.config.fields.filter((field) => field.table !== false))
const formFields = computed(() => props.config.fields.filter((field) => field.form !== false && field.type !== 'datetime' && field.type !== 'status'))
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const referenceAllowed = computed(() => {
  if (referenceResult.value.allowed != null) return referenceResult.value.allowed
  if (referenceResult.value.canRemove != null) return referenceResult.value.canRemove
  return Number(referenceResult.value.referenceCount || 0) <= 0
})
const referenceSummaries = computed(() => {
  const summaries = referenceResult.value.referenceSummaries || []
  if (summaries.length) return summaries.map((summary) => ({ summary }))
  return (referenceResult.value.references || []).map((item) => ({ summary: displayValue(item) }))
})
const drawerTitle = computed(() => {
  if (drawerReadonly.value) return t('common.detail')
  return form.value[props.config.idKey] ? t('productCenter.common.editTitle', { name: t(props.config.titleKey) }) : t('productCenter.common.addTitle', { name: t(props.config.titleKey) })
})
const attachmentEnabled = computed(() => Boolean(props.config.attachments))
const currentRecordId = computed(() => {
  const value = form.value[props.config.idKey]
  return isRecordId(value) ? value : ''
})
const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  formFields.value.forEach((field) => {
    if (field.required) {
      result[field.prop] = [{ required: true, message: t('productCenter.common.required', { name: t(field.labelKey) }), trigger: 'blur' }]
    }
  })
  return result
})

function isRecordId(value: unknown): value is string | number {
  return typeof value === 'string' || typeof value === 'number'
}

function optionLabel(field: ProductFieldConfig, value: unknown) {
  const option = field.options?.find((item) => item.value === value)
  return option?.label || String(value ?? '-')
}

function normalizeStatus(value: unknown) {
  return value === 'ENABLED' || value === '1' || value === 'true' || value === true || value === 1 ? 'ENABLED' : 'DISABLED'
}

function statusSwitchValue(value: unknown) {
  return normalizeStatus(value)
}

function booleanLabel(value: unknown) {
  return value === '1' || value === 'true' || value === true || value === 1 ? t('common.yes') : t('common.no')
}

function displayValue(value: unknown) {
  if (Array.isArray(value)) return value.length ? value.join(', ') : '-'
  if (value && typeof value === 'object') return JSON.stringify(value)
  return String(value ?? '-')
}

function normalizeNumberValue(value: unknown) {
  if (value === null || value === undefined || value === '') return value
  const next = Number(value)
  return Number.isFinite(next) ? next : value
}

function normalizeFormRecord(record?: ProductRecord) {
  const next: ProductRecord = { ...(record || {}) }
  formFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = normalizeNumberValue(next[field.prop])
    if (field.type === 'boolean') next[field.prop] = next[field.prop] === '1' || next[field.prop] === 'true' || next[field.prop] === true || next[field.prop] === 1
  })
  return next
}

function defaultColumnMinWidth(field: ProductFieldConfig) {
  if (field.type === 'status' || field.type === 'boolean') return 104
  if (field.type === 'number') return 120
  if (field.prop.toLowerCase().includes('code')) return 156
  if (field.prop.toLowerCase().includes('name')) return 180
  return 140
}

function formItemClass(field: ProductFieldConfig) {
  return {
    'product-grid-page__form-item--full': field.formSpan === 2 || field.type === 'textarea' || field.type === 'url',
    'product-grid-page__form-item--compact': field.type === 'number' || field.type === 'boolean'
  }
}

function rowIndex(index: number) {
  return (Number(queryParams.pageNum || 1) - 1) * Number(queryParams.pageSize || 10) + index + 1
}

function isCancelError(error: unknown) {
  return error === 'cancel' || error === 'close'
}

function rowActionKey(action: NonNullable<ProductGridConfig['rowActions']>[number], row: ProductRecord) {
  return `${action.labelKey}:${String(row[props.config.idKey] ?? '')}`
}

function reset() {
  const next: ProductRecord = { status: 'ENABLED', delFlag: '0', ...(props.config.defaultRecord || {}) }
  formFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = 0
    if (field.type === 'boolean') next[field.prop] = false
  })
  form.value = next
  attachmentRows.value = []
  formRef.value?.resetFields()
}

async function loadAttachments(record?: ProductRecord) {
  if (!props.config.attachments) return
  const id = record?.[props.config.idKey]
  if (!isRecordId(id)) {
    attachmentRows.value = []
    return
  }
  attachmentLoading.value = true
  try {
    const response = await productMediaBindingApi.list({
      targetType: props.config.attachments.targetType,
      targetId: id,
      pageNum: 1,
      pageSize: 50
    })
    attachmentRows.value = response.rows || []
  } finally {
    attachmentLoading.value = false
  }
}

async function getList() {
  loading.value = true
  try {
    const response = await props.config.api.list(queryParams)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function resetQueryParams() {
  Object.keys(queryParams).forEach((key) => {
    if (key !== 'pageNum' && key !== 'pageSize') {
      delete queryParams[key]
    }
  })
  queryParams.pageNum = 1
  Object.assign(queryParams, props.config.initialQuery || {})
}

function applyRouteQuery() {
  searchFields.value.forEach((field) => {
    const value = route.query[field.prop]
    if (typeof value === 'string') {
      queryParams[field.prop] = value
    }
  })
}

function handleSelectionChange(selection: ProductRecord[]) {
  ids.value = selection.map((item) => item[props.config.idKey]).filter(isRecordId)
}

function handleAdd() {
  reset()
  drawerReadonly.value = false
  open.value = true
}

async function handleUpdate(row?: ProductRecord) {
  const id = row?.[props.config.idKey] ?? ids.value[0]
  if (!isRecordId(id)) return
  reset()
  drawerReadonly.value = false
  const response = await props.config.api.get(id)
  form.value = normalizeFormRecord(response.data)
  await loadAttachments(form.value)
  open.value = true
}

async function handleDetail(row: ProductRecord) {
  const id = row[props.config.idKey]
  if (!isRecordId(id)) return
  reset()
  drawerReadonly.value = true
  const response = await props.config.api.get(id)
  form.value = normalizeFormRecord(response.data)
  await loadAttachments(form.value)
  open.value = true
}

async function handleRowDblclick(row: ProductRecord) {
  await handleDetail(row)
}

async function handleDelete(row?: ProductRecord) {
  const targetIds = row?.[props.config.idKey] ? [row[props.config.idKey] as string | number] : ids.value
  if (!targetIds.length) return
  if (props.config.api.references) {
    for (const id of targetIds) {
      const response = await props.config.api.references(id)
      const result = response.data || {}
      const allowed = result.allowed ?? result.canRemove ?? Number(result.referenceCount || 0) <= 0
      if (!allowed) {
        const reason = result.blockerReasonKey ? t(result.blockerReasonKey) : t('productCenter.common.hasReferences')
        ElMessage.warning(reason)
        referenceResult.value = result
        referenceOpen.value = true
        return
      }
    }
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), {
    type: 'warning',
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  })
  await props.config.api.remove(targetIds)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

async function handleReference(row: ProductRecord) {
  const id = row[props.config.idKey]
  if (!props.config.api.references || !isRecordId(id)) {
    referenceResult.value = {}
    referenceOpen.value = true
    return
  }
  const response = await props.config.api.references(id)
  referenceResult.value = response.data || {}
  referenceOpen.value = true
}

async function handleStatusChange(row: ProductRecord, field: ProductFieldConfig, value: unknown) {
  const id = row[props.config.idKey]
  if (!props.config.api.changeStatus || !isRecordId(id)) return
  const previous = row[field.prop]
  const nextStatus = normalizeStatus(value)
  row[field.prop] = nextStatus
  try {
    await props.config.api.changeStatus(id, nextStatus)
    ElMessage.success(t('common.editSuccess'))
  } catch (error) {
    row[field.prop] = previous
    throw error
  }
}

function handleBooleanChange(field: ProductFieldConfig, value: unknown) {
  form.value[field.prop] = Boolean(value)
}

function cancel() {
  open.value = false
  drawerReadonly.value = false
  reset()
}

async function handleRowAction(action: NonNullable<ProductGridConfig['rowActions']>[number], row: ProductRecord) {
  rowActionLoading.value = rowActionKey(action, row)
  try {
    await action.handler(row)
    await getList()
  } catch (error) {
    if (!isCancelError(error)) throw error
  } finally {
    rowActionLoading.value = ''
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.value[props.config.idKey]) {
      await props.config.api.update(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await props.config.api.add(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

function beforeAttachmentUpload(file: UploadRawFile) {
  const maxSize = 20
  if (file.size / 1024 / 1024 > maxSize) {
    ElMessage.error(t('upload.fileTooLarge', { size: maxSize }))
    return false
  }
  return true
}

async function uploadAttachment(options: UploadRequestOptions) {
  if (!props.config.attachments || !currentRecordId.value) return
  const formData = new FormData()
  formData.append(options.filename, options.file)
  const uploadResponse = await service.post(`${getApiBaseUrl()}/system/oss/upload`, formData) as unknown as { data?: ProductRecord }
  const uploadData = uploadResponse.data || {}
  const assetCode = `ASSET_${props.config.attachments.targetType}_${currentRecordId.value}_${Date.now()}`
  await productMediaAssetApi.add({
    assetCode,
    assetNameCn: String(uploadData.fileName || options.file.name || assetCode),
    assetNameEn: String(uploadData.fileName || options.file.name || assetCode),
    assetType: String(options.file.type || '').startsWith('image/') ? 'IMAGE' : 'FILE',
    usageType: props.config.attachments.defaultUsageType || 'REFERENCE',
    languageCode: localeStore.language,
    visibility: 'INTERNAL',
    ossId: uploadData.ossId,
    url: uploadData.url,
    versionNo: 1,
    status: 'ENABLED',
    delFlag: '0'
  })
  const assetResponse = await productMediaAssetApi.options({ assetCode, status: 'ENABLED' })
  const assetList = Array.isArray(assetResponse) ? assetResponse : assetResponse.data || []
  const asset = assetList.find((item) => item.assetCode === assetCode) || assetList[0]
  if (!asset?.assetId) {
    ElMessage.error(t('common.upload.failed'))
    return
  }
  await productMediaBindingApi.add({
    assetId: asset?.assetId,
    assetCode,
    targetType: props.config.attachments.targetType,
    targetId: currentRecordId.value,
    targetCode: String(form.value[props.config.attachments.targetCodeField] || ''),
    usageType: props.config.attachments.defaultUsageType || 'REFERENCE',
    visibility: 'INTERNAL',
    languageCode: localeStore.language,
    requiredForPublish: '0',
    sortOrder: attachmentRows.value.length + 1,
    status: 'ENABLED',
    delFlag: '0'
  })
  ElMessage.success(t('common.upload.success'))
  await loadAttachments(form.value)
}

async function openAttachment(row: ProductRecord) {
  if (!row.assetId) return
  const response = await productMediaAssetApi.get(row.assetId as string | number)
  const url = response.data?.url
  if (url) window.open(String(url), '_blank')
}

async function removeAttachment(row: ProductRecord) {
  if (!row.bindingId) return
  await productMediaBindingApi.remove(row.bindingId as string | number)
  ElMessage.success(t('common.deleteSuccess'))
  await loadAttachments(form.value)
}

watch(() => [props.config.key, route.fullPath], () => {
  ids.value = []
  resetQueryParams()
  applyRouteQuery()
  reset()
  getList()
}, { immediate: true })

defineExpose({
  getList
})
</script>

<style scoped lang="scss">
.product-grid-page__search {
  margin-bottom: 8px;
}

.product-grid-page__toolbar {
  align-items: center;
}

.product-grid-page__table {
  :deep(.el-table__body tr) {
    cursor: pointer;
  }
}

.product-grid-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.product-grid-page__form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  column-gap: 24px;
  padding-right: 8px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.product-grid-page__form-item--full,
.product-grid-page__attachments {
  grid-column: 1 / -1;
}

.product-grid-page__form-item--compact {
  :deep(.el-input-number),
  :deep(.el-switch) {
    max-width: 220px;
  }
}

.product-grid-page__number {
  width: 100%;
}

.product-grid-page__attachments {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 16px;
}

.product-grid-page__attachments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.product-grid-page__reference-alert,
.product-grid-page__reference-table {
  margin-top: 12px;
}

@media (max-width: 720px) {
  .product-grid-page__form {
    grid-template-columns: 1fr;
  }
}
</style>
