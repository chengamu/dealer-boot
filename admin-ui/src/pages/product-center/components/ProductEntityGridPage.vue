<template>
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px" class="product-grid-page__search">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop">
        <el-select
          v-if="field.type === 'status' || field.type === 'select'"
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.selectPlaceholder')"
          clearable
          filterable
          style="width: 180px"
        >
          <template v-if="field.type === 'status'">
            <el-option :label="t('productCenter.status.enabled')" value="1" />
            <el-option :label="t('productCenter.status.disabled')" value="0" />
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

    <el-table v-loading="loading" :data="rows" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column
        v-for="field in tableFields"
        :key="field.prop"
        :label="t(field.labelKey)"
        :prop="field.prop"
        :width="field.width"
        :show-overflow-tooltip="field.type !== 'status' && field.type !== 'url'"
      >
        <template #default="{ row }">
          <product-status-tag v-if="field.type === 'status'" :value="row[field.prop]" />
          <span v-else-if="field.type === 'select'">{{ optionLabel(field, row[field.prop]) }}</span>
          <span v-else-if="field.type === 'datetime'">{{ formatUtc(row[field.prop] as string | undefined) }}</span>
          <el-link v-else-if="field.type === 'url' && row[field.prop]" type="primary" :href="String(row[field.prop])" target="_blank">
            {{ t('productCenter.common.open') }}
          </el-link>
          <span v-else>{{ row[field.prop] ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="230" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!config.hideReference" link type="primary" icon="View" @click="handleReference(row)" v-hasPermi="[config.permissions.reference]">
            {{ t('productCenter.common.references') }}
          </el-button>
          <el-button v-if="config.showDetail" link type="primary" icon="View" @click="handleDetail(row)" v-hasPermi="[config.permissions.reference]">
            {{ t('common.detail') }}
          </el-button>
          <el-button v-if="!config.readonly" link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="[config.permissions.edit]">
            {{ t('common.edit') }}
          </el-button>
          <el-button v-if="!config.readonly" link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="[config.permissions.remove]">
            {{ t('common.delete') }}
          </el-button>
          <el-button
            v-for="action in config.rowActions || []"
            :key="action.labelKey"
            link
            :type="action.type || 'primary'"
            :icon="action.icon"
            :loading="rowActionLoading === rowActionKey(action, row)"
            :disabled="Boolean(rowActionLoading) || action.disabled?.(row)"
            @click="handleRowAction(action, row)"
            v-hasPermi="[action.permission]"
          >
            {{ t(action.labelKey) }}
          </el-button>
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

    <el-drawer v-model="open" :title="drawerTitle" size="520px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="132px">
        <el-form-item v-for="field in formFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop">
          <el-input-number v-if="field.type === 'number'" v-model="form[field.prop] as number" :min="0" :disabled="drawerReadonly" controls-position="right" style="width: 100%" />
          <el-select v-else-if="field.type === 'status' || field.type === 'select'" v-model="form[field.prop]" :disabled="drawerReadonly" filterable clearable style="width: 100%">
            <template v-if="field.type === 'status'">
              <el-option :label="t('productCenter.status.enabled')" value="1" />
              <el-option :label="t('productCenter.status.disabled')" value="0" />
            </template>
            <template v-else>
              <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
            </template>
          </el-select>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop] as string" type="textarea" :rows="3" :disabled="drawerReadonly" :placeholder="t('productCenter.common.inputPlaceholder')" />
          <el-input v-else v-model="form[field.prop] as string" :disabled="drawerReadonly" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
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
          {{ referenceResult.canRemove === false ? t('common.no') : t('common.yes') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.canDisable')">
          {{ referenceResult.canDisable === false ? t('common.no') : t('common.yes') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.referenceCount')">
          {{ referenceResult.referenceCount ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-if="!referenceResult.references?.length" :description="t('productCenter.common.noReferences')" />
    </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductPageQuery, ProductRecord, ReferenceCheckResult } from '@/api/product-capability/types'
import ProductStatusTag from './ProductStatusTag.vue'

export interface ProductFieldConfig {
  prop: string
  labelKey: string
  type?: 'text' | 'textarea' | 'number' | 'status' | 'datetime' | 'url' | 'select'
  options?: Array<{ label?: string; value?: string | number }>
  width?: number | string
  search?: boolean
  table?: boolean
  form?: boolean
  required?: boolean
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
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<ProductPageQuery>({
  pageNum: 1,
  pageSize: 10
})

const searchFields = computed(() => props.config.fields.filter((field) => field.search))
const tableFields = computed(() => props.config.fields.filter((field) => field.table !== false))
const formFields = computed(() => props.config.fields.filter((field) => field.form !== false && field.type !== 'datetime'))
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const drawerTitle = computed(() => {
  if (drawerReadonly.value) return t('common.detail')
  return form.value[props.config.idKey] ? t('productCenter.common.editTitle', { name: t(props.config.titleKey) }) : t('productCenter.common.addTitle', { name: t(props.config.titleKey) })
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

function isCancelError(error: unknown) {
  return error === 'cancel' || error === 'close'
}

function rowActionKey(action: NonNullable<ProductGridConfig['rowActions']>[number], row: ProductRecord) {
  return `${action.labelKey}:${String(row[props.config.idKey] ?? '')}`
}

function reset() {
  const next: ProductRecord = { status: '1', delFlag: '0' }
  formFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = 0
  })
  form.value = next
  formRef.value?.resetFields()
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
  form.value = response.data || {}
  open.value = true
}

async function handleDetail(row: ProductRecord) {
  const id = row[props.config.idKey]
  if (!isRecordId(id)) return
  reset()
  drawerReadonly.value = true
  const response = await props.config.api.get(id)
  form.value = response.data || {}
  open.value = true
}

async function handleDelete(row?: ProductRecord) {
  const targetIds = row?.[props.config.idKey] ? [row[props.config.idKey] as string | number] : ids.value
  if (!targetIds.length) return
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

.product-grid-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
