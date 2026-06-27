<template>
  <div class="app-container product-center-page product-dict-page" data-agent-scope="product-base-page" data-agent-entity="product-dict">
    <div class="product-dict-page__search-bar">
      <el-form ref="typeQueryRef" :model="typeQuery" :inline="true" class="product-dict-page__search" data-agent-scope="product-dict-type-search">
        <el-form-item :label="t('productCenter.productDict.typeName')" prop="dictTypeNameCn" data-agent-field="dictTypeNameCn">
          <el-input v-model="typeQuery.dictTypeNameCn" :placeholder="t('productCenter.common.inputPlaceholder')" clearable :aria-label="t('productCenter.productDict.typeName')" :data-agent-label="t('productCenter.productDict.typeName')" @keyup.enter="queryTypes" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :aria-label="t('common.search')" :data-agent-label="t('common.search')" @click="queryTypes">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :aria-label="t('common.reset')" :data-agent-label="t('common.reset')" @click="resetTypeQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-form ref="itemQueryRef" :model="itemQuery" :inline="true" class="product-dict-page__search" data-agent-scope="product-dict-item-search">
        <el-form-item :label="t('productCenter.productDict.itemLabel')" prop="dictItemLabelCn" data-agent-field="dictItemLabelCn">
          <el-input v-model="itemQuery.dictItemLabelCn" :placeholder="t('productCenter.common.inputPlaceholder')" clearable :disabled="!activeType" :aria-label="t('productCenter.productDict.itemLabel')" :data-agent-label="t('productCenter.productDict.itemLabel')" @keyup.enter="queryItems" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :disabled="!activeType" :aria-label="t('common.search')" :data-agent-label="t('common.search')" @click="queryItems">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :disabled="!activeType" :aria-label="t('common.reset')" :data-agent-label="t('common.reset')" @click="resetItemQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div ref="gridWrapRef" class="product-dict-page__grids" :style="gridStyle">
      <section class="product-dict-page__panel product-dict-page__panel--types">
        <div class="product-dict-page__toolbar">
          <div class="product-dict-page__title">
            <strong>{{ t('productCenter.productDict.typeTitle') }}</strong>
          </div>
          <div class="product-dict-page__actions">
            <el-button type="primary" plain icon="Plus" :aria-label="t('common.add')" :data-agent-label="t('common.add')" @click="addType" v-hasPermi="['product:dict:add']">{{ t('common.add') }}</el-button>
            <el-button type="success" plain icon="Edit" :disabled="selectedTypeIds.length !== 1" :aria-label="t('common.edit')" :data-agent-label="t('common.edit')" @click="editSelectedType" v-hasPermi="['product:dict:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedTypeIds.length" :aria-label="t('common.delete')" :data-agent-label="t('common.delete')" data-agent-danger="delete" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能删除" @click="deleteSelectedTypes" v-hasPermi="['product:dict:remove']">{{ t('common.delete') }}</el-button>
          </div>
        </div>

        <el-table
          v-loading="typeLoading"
          :data="typeRows"
          border
          highlight-current-row
          row-key="dictTypeId"
          class="product-dict-page__table"
          :default-sort="typeDefaultSort"
          @current-change="selectType"
          @sort-change="handleTypeSortChange"
          @row-dblclick="editType"
        >
          <el-table-column type="index" :index="typeRowIndex" :label="t('common.index')" width="58" align="center" />
          <el-table-column :label="t('productCenter.productDict.typeCode')" prop="dictTypeCode" min-width="180" sortable="custom" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.productDict.typeNameCn')" prop="dictTypeNameCn" min-width="150" sortable="custom" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.common.sortOrder')" prop="sortOrder" width="72" align="center" sortable="custom" />
          <el-table-column :label="t('productCenter.common.status')" prop="status" width="88" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status"
                :active-value="PRODUCT_STATUS_ENABLED"
                :inactive-value="PRODUCT_STATUS_DISABLED"
                :aria-label="t('productCenter.common.status')"
                :data-agent-label="t('productCenter.common.status')"
                data-agent-risk="confirm-required"
                data-agent-confirm-required="true"
                data-agent-confirm-message="修改状态需要用户人工确认"
                @change="changeTypeStatus(row, $event)"
              />
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="typeTotal > 0" v-model:page="typeQuery.pageNum" v-model:limit="typeQuery.pageSize" :total="typeTotal" @pagination="loadTypes" />
      </section>

      <div class="product-dict-page__divider" @pointerdown="startResize" />

      <section class="product-dict-page__panel product-dict-page__panel--items">
        <div class="product-dict-page__toolbar">
          <div class="product-dict-page__title">
            <strong>{{ t('productCenter.productDict.itemTitle') }}</strong>
            <span v-if="activeType">{{ activeType.dictTypeNameCn }} / {{ activeType.dictTypeCode }}</span>
          </div>
          <div class="product-dict-page__actions">
            <el-button type="primary" plain icon="Plus" :disabled="!activeType" :aria-label="t('common.add')" :data-agent-label="t('common.add')" @click="addItem" v-hasPermi="['product:dict:add']">{{ t('common.add') }}</el-button>
            <el-button type="success" plain icon="Edit" :disabled="selectedItemIds.length !== 1" :aria-label="t('common.edit')" :data-agent-label="t('common.edit')" @click="editSelectedItem" v-hasPermi="['product:dict:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedItemIds.length" :aria-label="t('common.delete')" :data-agent-label="t('common.delete')" data-agent-danger="delete" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能删除" @click="deleteSelectedItems" v-hasPermi="['product:dict:remove']">{{ t('common.delete') }}</el-button>
          </div>
        </div>

        <el-table
          v-loading="itemLoading"
          :data="itemRows"
          border
          highlight-current-row
          row-key="dictItemId"
          class="product-dict-page__table"
          :default-sort="itemDefaultSort"
          @current-change="selectItem"
          @sort-change="handleItemSortChange"
          @row-dblclick="editItem"
        >
          <el-table-column type="index" :index="itemRowIndex" :label="t('common.index')" width="58" align="center" />
          <el-table-column :label="t('productCenter.productDict.itemValue')" prop="dictItemValue" min-width="145" sortable="custom" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.productDict.itemLabelCn')" prop="dictItemLabelCn" min-width="150" sortable="custom" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.productDict.itemLabelEn')" prop="dictItemLabelEn" min-width="150" show-overflow-tooltip />
          <el-table-column v-if="supportsParentValue" :label="t('productCenter.productDict.parentItem')" prop="parentValue" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ parentItemLabel(row.parentValue) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.common.sortOrder')" prop="sortOrder" width="72" align="center" sortable="custom" />
          <el-table-column :label="t('productCenter.common.status')" prop="status" width="88" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status"
                :active-value="PRODUCT_STATUS_ENABLED"
                :inactive-value="PRODUCT_STATUS_DISABLED"
                :aria-label="t('productCenter.common.status')"
                :data-agent-label="t('productCenter.common.status')"
                data-agent-risk="confirm-required"
                data-agent-confirm-required="true"
                data-agent-confirm-message="修改状态需要用户人工确认"
                @change="changeItemStatus(row, $event)"
              />
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="itemTotal > 0" v-model:page="itemQuery.pageNum" v-model:limit="itemQuery.pageSize" :total="itemTotal" @pagination="loadItems" />
      </section>
    </div>

    <el-drawer v-model="typeDrawerOpen" :title="typeDrawerTitle" size="72%" append-to-body destroy-on-close @closed="resetTypeForm">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="136px" class="product-dict-page__form">
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.basic') }}</div>
        <el-form-item :label="t('productCenter.productDict.typeCode')" prop="dictTypeCode">
          <el-input v-model="typeForm.dictTypeCode" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.typeNameCn')" prop="dictTypeNameCn">
          <el-input v-model="typeForm.dictTypeNameCn" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.typeNameEn')" prop="dictTypeNameEn">
          <el-input v-model="typeForm.dictTypeNameEn" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.businessDomain')" prop="businessDomain">
          <el-select v-model="typeForm.businessDomain" filterable>
            <el-option v-for="option in businessDomainOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" prop="sortOrder" data-agent-field="sortOrder">
          <el-input-number v-model="typeForm.sortOrder" :min="0" controls-position="right" :aria-label="t('productCenter.common.sortOrder')" :data-agent-label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder" data-agent-input-kind="number" class="product-dict-page__number" />
        </el-form-item>
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.note') }}</div>
        <el-form-item :label="t('productCenter.common.remark')" prop="remark" class="product-dict-page__form-item--full">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="product-dict-page__drawer-actions">
          <el-button @click="typeDrawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="typeSubmitLoading" data-agent-danger="save" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能保存" @click="submitType">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="itemDrawerOpen" :title="itemDrawerTitle" size="72%" append-to-body destroy-on-close @closed="resetItemForm">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="136px" class="product-dict-page__form">
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.basic') }}</div>
        <el-form-item :label="t('productCenter.productDict.typeCode')" prop="dictTypeCode">
          <el-input v-model="itemForm.dictTypeCode" disabled />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.itemValue')" prop="dictItemValue">
          <el-input v-model="itemForm.dictItemValue" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.itemLabelCn')" prop="dictItemLabelCn">
          <el-input v-model="itemForm.dictItemLabelCn" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.itemLabelEn')" prop="dictItemLabelEn">
          <el-input v-model="itemForm.dictItemLabelEn" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="supportsParentValue" :label="t('productCenter.productDict.parentItem')" prop="parentValue">
          <el-select v-model="itemForm.parentValue" clearable filterable :placeholder="t('productCenter.common.selectPlaceholder')">
            <el-option v-for="option in parentItemOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" prop="sortOrder" data-agent-field="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" controls-position="right" :aria-label="t('productCenter.common.sortOrder')" :data-agent-label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder" data-agent-input-kind="number" class="product-dict-page__number" />
        </el-form-item>
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.note') }}</div>
        <el-form-item :label="t('productCenter.common.remark')" prop="remark" class="product-dict-page__form-item--full">
          <el-input v-model="itemForm.remark" type="textarea" :rows="3" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="product-dict-page__drawer-actions">
          <el-button @click="itemDrawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="itemSubmitLoading" data-agent-danger="save" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能保存" @click="submitItem">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="ProductDictPage">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productDictItemApi, productDictTypeApi } from '@/api/product-capability/product-dict'
import type { ProductCrudApi, ProductDictItemQuery, ProductDictItemVO, ProductDictTypeQuery, ProductDictTypeVO } from '@/api/product-capability/types'
import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const businessDomainOptions = computed(() => [
  { label: t('productCenter.productDict.domainBase'), value: 'BASE' }
])

const typeRows = ref<ProductDictTypeVO[]>([])
const itemRows = ref<ProductDictItemVO[]>([])
const activeType = ref<ProductDictTypeVO>()
const selectedTypeIds = ref<Array<string | number>>([])
const selectedItemIds = ref<Array<string | number>>([])
const typeLoading = ref(false)
const itemLoading = ref(false)
const typeSubmitLoading = ref(false)
const itemSubmitLoading = ref(false)
const typeTotal = ref(0)
const itemTotal = ref(0)
const typeDrawerOpen = ref(false)
const itemDrawerOpen = ref(false)
const typeQueryRef = ref<FormInstance>()
const itemQueryRef = ref<FormInstance>()
const typeFormRef = ref<FormInstance>()
const itemFormRef = ref<FormInstance>()
const gridWrapRef = ref<HTMLElement>()
const leftPanelWidth = ref(560)
const resizing = ref(false)
const typeDefaultSort = { prop: 'sortOrder', order: 'ascending' as const }
const itemDefaultSort = { prop: 'sortOrder', order: 'ascending' as const }

const typeQuery = reactive<ProductDictTypeQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: typeDefaultSort.prop,
  isAsc: typeDefaultSort.order
})
const itemQuery = reactive<ProductDictItemQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: itemDefaultSort.prop,
  isAsc: itemDefaultSort.order
})
const typeForm = ref<ProductDictTypeVO>({})
const itemForm = ref<ProductDictItemVO>({})

const typeDrawerTitle = computed(() => typeForm.value.dictTypeId ? t('productCenter.productDict.editType') : t('productCenter.productDict.addType'))
const itemDrawerTitle = computed(() => itemForm.value.dictItemId ? t('productCenter.productDict.editItem') : t('productCenter.productDict.addItem'))
const gridStyle = computed(() => ({
  gridTemplateColumns: `${leftPanelWidth.value}px 3px minmax(0, 1fr)`
}))
const hierarchicalDictTypes = new Set(['product_category_level'])
const supportsParentValue = computed(() => Boolean(activeType.value?.dictTypeCode && hierarchicalDictTypes.has(activeType.value.dictTypeCode)))
const parentItemOptions = computed(() => itemRows.value
  .filter((item) => item.dictItemValue && item.dictItemId !== itemForm.value.dictItemId)
  .map((item) => ({
    value: item.dictItemValue,
    label: `${item.dictItemValue} ${item.dictItemLabelCn || item.dictItemLabelEn || ''}`.trim()
  })))
const typeRules = computed<FormRules<ProductDictTypeVO>>(() => ({
  dictTypeCode: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.productDict.typeCode') }), trigger: 'blur' }],
  dictTypeNameCn: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.productDict.typeNameCn') }), trigger: 'blur' }]
}))
const itemRules = computed<FormRules<ProductDictItemVO>>(() => ({
  dictTypeCode: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.productDict.typeCode') }), trigger: 'blur' }],
  dictItemValue: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.productDict.itemValue') }), trigger: 'blur' }],
  dictItemLabelCn: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.productDict.itemLabelCn') }), trigger: 'blur' }]
}))

function typeRowIndex(index: number) {
  return (Number(typeQuery.pageNum || 1) - 1) * Number(typeQuery.pageSize || 10) + index + 1
}

function itemRowIndex(index: number) {
  return (Number(itemQuery.pageNum || 1) - 1) * Number(itemQuery.pageSize || 10) + index + 1
}

function stopResize() {
  if (!resizing.value) return
  resizing.value = false
  document.body.classList.remove('product-dict-page--resizing')
  window.removeEventListener('pointermove', handleResize)
  window.removeEventListener('pointerup', stopResize)
}

function handleResize(event: PointerEvent) {
  const rect = gridWrapRef.value?.getBoundingClientRect()
  if (!rect) return
  const nextWidth = event.clientX - rect.left
  const maxWidth = Math.max(480, rect.width - 620)
  leftPanelWidth.value = Math.min(Math.max(nextWidth, 420), maxWidth)
}

function startResize(event: PointerEvent) {
  resizing.value = true
  document.body.classList.add('product-dict-page--resizing')
  window.addEventListener('pointermove', handleResize)
  window.addEventListener('pointerup', stopResize)
  handleResize(event)
}

function parentItemLabel(value?: string) {
  if (!value) return '-'
  return parentItemOptions.value.find((item) => item.value === value)?.label || value
}

function isRecordId(value: unknown): value is string | number {
  return typeof value === 'string' || typeof value === 'number'
}

function resetTypeForm() {
  typeForm.value = {
    businessDomain: 'BASE',
    status: PRODUCT_STATUS_ENABLED,
    delFlag: '0',
    systemFlag: false,
    editableFlag: true,
    sortOrder: 0
  }
  typeFormRef.value?.resetFields()
}

function resetItemForm() {
  itemForm.value = {
    dictTypeCode: activeType.value?.dictTypeCode,
    status: PRODUCT_STATUS_ENABLED,
    delFlag: '0',
    systemFlag: false,
    editableFlag: true,
    sortOrder: 0
  }
  itemFormRef.value?.resetFields()
}

function applySort(query: ProductDictTypeQuery | ProductDictItemQuery, defaultSort: typeof typeDefaultSort, prop?: string, order?: 'ascending' | 'descending' | null) {
  query.pageNum = 1
  if (prop && order) {
    query.orderByColumn = prop
    query.isAsc = order
  } else {
    query.orderByColumn = defaultSort.prop
    query.isAsc = defaultSort.order
  }
}

function handleTypeSortChange({ prop, order }: { prop?: string; order?: 'ascending' | 'descending' | null }) {
  applySort(typeQuery, typeDefaultSort, prop, order)
  loadTypes()
}

function handleItemSortChange({ prop, order }: { prop?: string; order?: 'ascending' | 'descending' | null }) {
  applySort(itemQuery, itemDefaultSort, prop, order)
  loadItems()
}

async function loadTypes() {
  typeLoading.value = true
  try {
    const response = await productDictTypeApi.list(typeQuery)
    typeRows.value = response.rows || []
    typeTotal.value = response.total || 0
    await syncActiveType()
  } finally {
    typeLoading.value = false
  }
}

async function syncActiveType() {
  const routeTypeCode = typeof route.query.dictTypeCode === 'string' ? route.query.dictTypeCode : ''
  const currentCode = routeTypeCode || activeType.value?.dictTypeCode || typeRows.value[0]?.dictTypeCode || ''
  const matched = typeRows.value.find((item) => item.dictTypeCode === currentCode)
  if (matched) {
    activeType.value = matched
    selectedTypeIds.value = isRecordId(matched.dictTypeId) ? [matched.dictTypeId] : []
    await loadItems()
  } else {
    activeType.value = undefined
    selectedTypeIds.value = []
    selectedItemIds.value = []
    itemRows.value = []
    itemTotal.value = 0
  }
}

async function loadItems() {
  if (!activeType.value?.dictTypeCode) {
    itemRows.value = []
    itemTotal.value = 0
    selectedItemIds.value = []
    return
  }
  itemLoading.value = true
  try {
    itemQuery.dictTypeCode = activeType.value.dictTypeCode
    const response = await productDictItemApi.list(itemQuery)
    itemRows.value = response.rows || []
    itemTotal.value = response.total || 0
    selectedItemIds.value = []
  } finally {
    itemLoading.value = false
  }
}

function queryTypes() {
  typeQuery.pageNum = 1
  loadTypes()
}

function queryItems() {
  itemQuery.pageNum = 1
  loadItems()
}

function resetTypeQuery() {
  typeQueryRef.value?.resetFields()
  typeQuery.dictTypeCode = undefined
  typeQuery.status = undefined
  applySort(typeQuery, typeDefaultSort)
  queryTypes()
}

function resetItemQuery() {
  itemQueryRef.value?.resetFields()
  itemQuery.dictItemValue = undefined
  itemQuery.status = undefined
  applySort(itemQuery, itemDefaultSort)
  queryItems()
}

async function selectType(row?: ProductDictTypeVO) {
  selectedTypeIds.value = isRecordId(row?.dictTypeId) ? [row.dictTypeId] : []
  if (!row?.dictTypeCode || row.dictTypeCode === activeType.value?.dictTypeCode) return
  activeType.value = row
  itemQuery.pageNum = 1
  applySort(itemQuery, itemDefaultSort)
  selectedItemIds.value = []
  await router.replace({ path: '/product-master/product-dicts', query: { dictTypeCode: row.dictTypeCode } })
  await loadItems()
}

function selectItem(row?: ProductDictItemVO) {
  selectedItemIds.value = isRecordId(row?.dictItemId) ? [row.dictItemId] : []
}

function addType() {
  resetTypeForm()
  typeDrawerOpen.value = true
}

async function editType(row: ProductDictTypeVO) {
  if (!row.dictTypeId) return
  if (!(await checkDictEdit(productDictTypeApi.editCheck, row.dictTypeId))) return
  const response = await productDictTypeApi.get(row.dictTypeId)
  typeForm.value = { ...response.data }
  typeDrawerOpen.value = true
}

function editSelectedType() {
  const id = selectedTypeIds.value[0]
  const row = typeRows.value.find((item) => item.dictTypeId === id)
  if (row) editType(row)
}

async function submitType() {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  typeSubmitLoading.value = true
  try {
    if (typeForm.value.dictTypeId) {
      await productDictTypeApi.update(typeForm.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await productDictTypeApi.add(typeForm.value)
      ElMessage.success(t('common.addSuccess'))
    }
    typeDrawerOpen.value = false
    await loadTypes()
  } finally {
    typeSubmitLoading.value = false
  }
}

async function ensureCanDeleteType(id: string | number) {
  const response = await productDictTypeApi.references?.(id)
  const result = response?.data || {}
  return result.allowed ?? result.canRemove ?? Number(result.referenceCount || 0) <= 0
}

async function deleteSelectedTypes() {
  if (!selectedTypeIds.value.length) return
  for (const id of selectedTypeIds.value) {
    if (!(await ensureCanDeleteType(id))) {
      ElMessage.warning(t('productCenter.common.hasReferences'))
      return
    }
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productDictTypeApi.remove(selectedTypeIds.value)
  ElMessage.success(t('common.deleteSuccess'))
  activeType.value = undefined
  selectedTypeIds.value = []
  await loadTypes()
}

async function changeTypeStatus(row: ProductDictTypeVO, value: unknown) {
  if (!row.dictTypeId || !productDictTypeApi.changeStatus) return
  const previous = row.status
  row.status = String(value)
  try {
    await productDictTypeApi.changeStatus(row.dictTypeId, row.status)
    ElMessage.success(t('common.editSuccess'))
  } catch (error) {
    row.status = previous
    throw error
  }
}

function addItem() {
  if (!activeType.value?.dictTypeCode) return
  resetItemForm()
  itemDrawerOpen.value = true
}

async function editItem(row: ProductDictItemVO) {
  if (!row.dictItemId) return
  if (!(await checkDictEdit(productDictItemApi.editCheck, row.dictItemId))) return
  const response = await productDictItemApi.get(row.dictItemId)
  itemForm.value = { ...response.data }
  itemDrawerOpen.value = true
}

async function checkDictEdit(editCheck: ProductCrudApi['editCheck'], id: string | number) {
  if (!editCheck) return true
  const response = await editCheck(id)
  const result = response.data || {}
  if (result.editable !== false) return true
  const reasonKey = result.reasonKey || result.reason || 'productCenter.common.editDenied'
  ElMessage.warning(t(reasonKey))
  return false
}

function editSelectedItem() {
  const id = selectedItemIds.value[0]
  const row = itemRows.value.find((item) => item.dictItemId === id)
  if (row) editItem(row)
}

async function submitItem() {
  const valid = await itemFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!supportsParentValue.value) {
    itemForm.value.parentValue = undefined
  }
  itemSubmitLoading.value = true
  try {
    if (itemForm.value.dictItemId) {
      await productDictItemApi.update(itemForm.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await productDictItemApi.add(itemForm.value)
      ElMessage.success(t('common.addSuccess'))
    }
    itemDrawerOpen.value = false
    await loadItems()
  } finally {
    itemSubmitLoading.value = false
  }
}

async function ensureCanDeleteItem(id: string | number) {
  const response = await productDictItemApi.references?.(id)
  const result = response?.data || {}
  return result.allowed ?? result.canRemove ?? Number(result.referenceCount || 0) <= 0
}

async function deleteSelectedItems() {
  if (!selectedItemIds.value.length) return
  for (const id of selectedItemIds.value) {
    if (!(await ensureCanDeleteItem(id))) {
      ElMessage.warning(t('productCenter.common.hasReferences'))
      return
    }
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productDictItemApi.remove(selectedItemIds.value)
  ElMessage.success(t('common.deleteSuccess'))
  selectedItemIds.value = []
  await loadItems()
}

async function changeItemStatus(row: ProductDictItemVO, value: unknown) {
  if (!row.dictItemId || !productDictItemApi.changeStatus) return
  const previous = row.status
  row.status = String(value)
  try {
    await productDictItemApi.changeStatus(row.dictItemId, row.status)
    ElMessage.success(t('common.editSuccess'))
  } catch (error) {
    row.status = previous
    throw error
  }
}

onBeforeUnmount(stopResize)

loadTypes()
</script>

<style scoped lang="scss">
.product-dict-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-dict-page__search-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
  padding: 12px 12px 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
  align-items: start;
}

.product-dict-page__grids {
  display: grid;
}

.product-dict-page__divider {
  position: relative;
  cursor: col-resize;

  &::before {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 1px;
    width: 1px;
    border-radius: 1px;
    background: var(--el-border-color-lighter);
    content: '';
  }

  &:hover::before {
    background: var(--el-color-primary);
  }
}

.product-dict-page__panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.product-dict-page__search {
  margin-bottom: 0;

  :deep(.el-form-item) {
    margin-right: 8px;
    margin-bottom: 8px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 168px;
  }
}

.product-dict-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.product-dict-page__title {
  display: grid;
  gap: 4px;
  min-width: 0;

  strong {
    color: var(--el-text-color-primary);
    font-size: 16px;
  }

  span {
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 1.4;
  }
}

.product-dict-page__actions {
  display: flex;
  flex-wrap: nowrap;
  justify-content: flex-end;
  gap: 8px;
}

.product-dict-page__table {
  :deep(.el-table__body tr) {
    cursor: pointer;
  }

  :deep(.el-table__cell) {
    padding: 10px 0;
  }
}

.product-dict-page__form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  column-gap: 24px;
  padding-right: 8px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-select) {
    width: 100%;
  }
}

.product-dict-page__section-title {
  grid-column: 1 / -1;
  margin: 6px 0 14px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
  font-weight: 700;
}

.product-dict-page__form-item--full {
  grid-column: 1 / -1;
}

.product-dict-page__number {
  width: 220px;
}

.product-dict-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1280px) {
  .product-dict-page__search-bar,
  .product-dict-page__grids {
    grid-template-columns: 1fr !important;
  }

  .product-dict-page__divider {
    display: none;
  }
}

@media (max-width: 720px) {
  .product-dict-page__form {
    grid-template-columns: 1fr;
  }

  .product-dict-page__toolbar {
    display: grid;
  }

  .product-dict-page__actions {
    justify-content: flex-start;
  }
}
</style>

<style lang="scss">
.product-dict-page--resizing {
  cursor: col-resize;
  user-select: none;
}
</style>
