<template>
  <div class="app-container product-center-page product-dict-page">
    <section class="product-dict-page__panel product-dict-page__panel--types">
      <el-form ref="typeQueryRef" :model="typeQuery" :inline="true" class="product-dict-page__search">
        <el-form-item :label="t('productCenter.productDict.typeCode')" prop="dictTypeCode">
          <el-input
            v-model="typeQuery.dictTypeCode"
            :placeholder="t('productCenter.common.inputPlaceholder')"
            clearable
            @keyup.enter="queryTypes"
          />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.typeName')" prop="dictTypeNameCn">
          <el-input
            v-model="typeQuery.dictTypeNameCn"
            :placeholder="t('productCenter.common.inputPlaceholder')"
            clearable
            @keyup.enter="queryTypes"
          />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.status')" prop="status">
          <el-select v-model="typeQuery.status" :placeholder="t('productCenter.common.selectPlaceholder')" clearable>
            <el-option :label="t('productCenter.status.enabled')" value="ENABLED" />
            <el-option :label="t('productCenter.status.disabled')" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="queryTypes">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetTypeQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <div class="product-dict-page__toolbar">
        <div class="product-dict-page__title">
          <strong>{{ t('productCenter.productDict.typeTitle') }}</strong>
          <span>{{ t('productCenter.productDict.typeDescription') }}</span>
        </div>
        <div class="product-dict-page__actions">
          <el-button type="primary" plain icon="Plus" @click="addType" v-hasPermi="['product:dict:add']">
            {{ t('common.add') }}
          </el-button>
          <el-button type="success" plain icon="Edit" :disabled="selectedTypeIds.length !== 1" @click="editSelectedType" v-hasPermi="['product:dict:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!selectedTypeIds.length" @click="deleteSelectedTypes" v-hasPermi="['product:dict:remove']">
            {{ t('common.delete') }}
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="typeLoading"
        :data="typeRows"
        border
        highlight-current-row
        row-key="dictTypeId"
        class="product-dict-page__table"
        @current-change="selectType"
        @selection-change="handleTypeSelection"
        @row-dblclick="editType"
      >
        <el-table-column type="selection" width="44" align="center" />
        <el-table-column type="index" :index="typeRowIndex" :label="t('common.index')" width="64" align="center" fixed />
        <el-table-column :label="t('productCenter.productDict.typeCode')" prop="dictTypeCode" min-width="190" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.productDict.typeNameCn')" prop="dictTypeNameCn" min-width="170" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.productDict.typeNameEn')" prop="dictTypeNameEn" min-width="210" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.productDict.businessDomain')" prop="businessDomain" width="116" align="center">
          <template #default="{ row }">
            {{ businessDomainLabel(row.businessDomain) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.common.sortOrder')" prop="sortOrder" width="88" align="center" />
        <el-table-column :label="t('productCenter.common.status')" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              active-value="ENABLED"
              inactive-value="DISABLED"
              @change="changeTypeStatus(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="112" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('common.edit')" placement="top">
              <el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" @click.stop="editType(row)" v-hasPermi="['product:dict:edit']" />
            </el-tooltip>
            <el-tooltip :content="t('common.delete')" placement="top">
              <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click.stop="deleteType(row)" v-hasPermi="['product:dict:remove']" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="typeTotal > 0"
        v-model:page="typeQuery.pageNum"
        v-model:limit="typeQuery.pageSize"
        :total="typeTotal"
        @pagination="loadTypes"
      />
    </section>

    <section class="product-dict-page__panel product-dict-page__panel--items">
      <div class="product-dict-page__item-head">
        <div class="product-dict-page__title">
          <strong>{{ t('productCenter.productDict.itemTitle') }}</strong>
          <span v-if="activeType">{{ activeType.dictTypeNameCn }} / {{ activeType.dictTypeCode }}</span>
          <span v-else>{{ t('productCenter.productDict.selectType') }}</span>
        </div>
        <div class="product-dict-page__actions">
          <el-button type="primary" plain icon="Plus" :disabled="!activeType" @click="addItem" v-hasPermi="['product:dict:add']">
            {{ t('common.add') }}
          </el-button>
          <el-button type="success" plain icon="Edit" :disabled="selectedItemIds.length !== 1" @click="editSelectedItem" v-hasPermi="['product:dict:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!selectedItemIds.length" @click="deleteSelectedItems" v-hasPermi="['product:dict:remove']">
            {{ t('common.delete') }}
          </el-button>
        </div>
      </div>

      <el-form ref="itemQueryRef" :model="itemQuery" :inline="true" class="product-dict-page__search product-dict-page__search--items">
        <el-form-item :label="t('productCenter.productDict.itemValue')" prop="dictItemValue">
          <el-input
            v-model="itemQuery.dictItemValue"
            :placeholder="t('productCenter.common.inputPlaceholder')"
            clearable
            :disabled="!activeType"
            @keyup.enter="queryItems"
          />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.itemLabel')" prop="dictItemLabelCn">
          <el-input
            v-model="itemQuery.dictItemLabelCn"
            :placeholder="t('productCenter.common.inputPlaceholder')"
            clearable
            :disabled="!activeType"
            @keyup.enter="queryItems"
          />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.status')" prop="status">
          <el-select v-model="itemQuery.status" :placeholder="t('productCenter.common.selectPlaceholder')" clearable :disabled="!activeType">
            <el-option :label="t('productCenter.status.enabled')" value="ENABLED" />
            <el-option :label="t('productCenter.status.disabled')" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :disabled="!activeType" @click="queryItems">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :disabled="!activeType" @click="resetItemQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="itemLoading"
        :data="itemRows"
        border
        row-key="dictItemId"
        class="product-dict-page__table"
        @selection-change="handleItemSelection"
        @row-dblclick="editItem"
      >
        <el-table-column type="selection" width="44" align="center" />
        <el-table-column type="index" :index="itemRowIndex" :label="t('common.index')" width="64" align="center" fixed />
        <el-table-column :label="t('productCenter.productDict.itemValue')" prop="dictItemValue" min-width="170" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.productDict.itemLabelCn')" prop="dictItemLabelCn" min-width="180" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.productDict.itemLabelEn')" prop="dictItemLabelEn" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="supportsParentValue" :label="t('productCenter.productDict.parentItem')" prop="parentValue" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ parentItemLabel(row.parentValue) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.productDict.systemFlag')" prop="systemFlag" width="92" align="center">
          <template #default="{ row }">
            {{ booleanLabel(row.systemFlag) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.common.sortOrder')" prop="sortOrder" width="88" align="center" />
        <el-table-column :label="t('productCenter.common.status')" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              active-value="ENABLED"
              inactive-value="DISABLED"
              @change="changeItemStatus(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="112" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('common.edit')" placement="top">
              <el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" @click.stop="editItem(row)" v-hasPermi="['product:dict:edit']" />
            </el-tooltip>
            <el-tooltip :content="t('common.delete')" placement="top">
              <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click.stop="deleteItem(row)" v-hasPermi="['product:dict:remove']" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="itemTotal > 0"
        v-model:page="itemQuery.pageNum"
        v-model:limit="itemQuery.pageSize"
        :total="itemTotal"
        @pagination="loadItems"
      />
    </section>

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
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.control') }}</div>
        <el-form-item :label="t('productCenter.productDict.systemFlag')" prop="systemFlag">
          <el-switch v-model="typeForm.systemFlag" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.editableFlag')" prop="editableFlag">
          <el-switch v-model="typeForm.editableFlag" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" prop="sortOrder">
          <el-input-number v-model="typeForm.sortOrder" :min="0" controls-position="right" class="product-dict-page__number" />
        </el-form-item>
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.note') }}</div>
        <el-form-item :label="t('productCenter.common.remark')" prop="remark" class="product-dict-page__form-item--full">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="product-dict-page__drawer-actions">
          <el-button @click="typeDrawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="typeSubmitLoading" @click="submitType">{{ t('common.confirm') }}</el-button>
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
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.control') }}</div>
        <el-form-item :label="t('productCenter.productDict.systemFlag')" prop="systemFlag">
          <el-switch v-model="itemForm.systemFlag" />
        </el-form-item>
        <el-form-item :label="t('productCenter.productDict.editableFlag')" prop="editableFlag">
          <el-switch v-model="itemForm.editableFlag" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" prop="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" controls-position="right" class="product-dict-page__number" />
        </el-form-item>
        <div class="product-dict-page__section-title">{{ t('productCenter.formSection.note') }}</div>
        <el-form-item :label="t('productCenter.common.remark')" prop="remark" class="product-dict-page__form-item--full">
          <el-input v-model="itemForm.remark" type="textarea" :rows="3" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="product-dict-page__drawer-actions">
          <el-button @click="itemDrawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="itemSubmitLoading" @click="submitItem">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="ProductDictPage">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productDictItemApi, productDictTypeApi } from '@/api/product-capability/product-dict'
import type { ProductDictItemQuery, ProductDictItemVO, ProductDictTypeQuery, ProductDictTypeVO } from '@/api/product-capability/types'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const businessDomainOptions = computed(() => [
  { label: t('productCenter.productDict.domainBase'), value: 'BASE' },
  { label: t('productCenter.productDict.domainEngineering'), value: 'ENGINEERING' },
  { label: t('productCenter.productDict.domainConfig'), value: 'CONFIG' }
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

const typeQuery = reactive<ProductDictTypeQuery>({
  pageNum: 1,
  pageSize: 10
})
const itemQuery = reactive<ProductDictItemQuery>({
  pageNum: 1,
  pageSize: 10
})
const typeForm = ref<ProductDictTypeVO>({})
const itemForm = ref<ProductDictItemVO>({})

const typeDrawerTitle = computed(() => typeForm.value.dictTypeId ? t('productCenter.productDict.editType') : t('productCenter.productDict.addType'))
const itemDrawerTitle = computed(() => itemForm.value.dictItemId ? t('productCenter.productDict.editItem') : t('productCenter.productDict.addItem'))
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

function businessDomainLabel(value?: string) {
  return businessDomainOptions.value.find((item) => item.value === value)?.label || value || '-'
}

function booleanLabel(value: unknown) {
  return value === true || value === 'true' || value === '1' || value === 1 ? t('common.yes') : t('common.no')
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
    status: 'ENABLED',
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
    status: 'ENABLED',
    delFlag: '0',
    systemFlag: false,
    editableFlag: true,
    sortOrder: 0
  }
  itemFormRef.value?.resetFields()
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
    await loadItems()
  } else {
    activeType.value = undefined
    itemRows.value = []
    itemTotal.value = 0
  }
}

async function loadItems() {
  if (!activeType.value?.dictTypeCode) {
    itemRows.value = []
    itemTotal.value = 0
    return
  }
  itemLoading.value = true
  try {
    itemQuery.dictTypeCode = activeType.value.dictTypeCode
    const response = await productDictItemApi.list(itemQuery)
    itemRows.value = response.rows || []
    itemTotal.value = response.total || 0
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
  queryTypes()
}

function resetItemQuery() {
  itemQueryRef.value?.resetFields()
  queryItems()
}

async function selectType(row?: ProductDictTypeVO) {
  if (!row?.dictTypeCode || row.dictTypeCode === activeType.value?.dictTypeCode) return
  activeType.value = row
  itemQuery.pageNum = 1
  selectedItemIds.value = []
  await router.replace({ path: '/product-master/product-dicts', query: { dictTypeCode: row.dictTypeCode } })
  await loadItems()
}

function handleTypeSelection(selection: ProductDictTypeVO[]) {
  selectedTypeIds.value = selection.reduce<Array<string | number>>((result, item) => {
    if (isRecordId(item.dictTypeId)) result.push(item.dictTypeId)
    return result
  }, [])
}

function handleItemSelection(selection: ProductDictItemVO[]) {
  selectedItemIds.value = selection.reduce<Array<string | number>>((result, item) => {
    if (isRecordId(item.dictItemId)) result.push(item.dictItemId)
    return result
  }, [])
}

function addType() {
  resetTypeForm()
  typeDrawerOpen.value = true
}

async function editType(row: ProductDictTypeVO) {
  if (!row.dictTypeId) return
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

async function deleteType(row: ProductDictTypeVO) {
  if (!row.dictTypeId) return
  if (!(await ensureCanDeleteType(row.dictTypeId))) {
    ElMessage.warning(t('productCenter.common.hasReferences'))
    return
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productDictTypeApi.remove(row.dictTypeId)
  ElMessage.success(t('common.deleteSuccess'))
  if (activeType.value?.dictTypeId === row.dictTypeId) activeType.value = undefined
  await loadTypes()
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
  const response = await productDictItemApi.get(row.dictItemId)
  itemForm.value = { ...response.data }
  itemDrawerOpen.value = true
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

async function deleteItem(row: ProductDictItemVO) {
  if (!row.dictItemId) return
  if (!(await ensureCanDeleteItem(row.dictItemId))) {
    ElMessage.warning(t('productCenter.common.hasReferences'))
    return
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productDictItemApi.remove(row.dictItemId)
  ElMessage.success(t('common.deleteSuccess'))
  await loadItems()
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

loadTypes()
</script>

<style scoped lang="scss">
.product-dict-page {
  display: grid;
  grid-template-columns: minmax(520px, 0.95fr) minmax(620px, 1.25fr);
  gap: 12px;
}

.product-dict-page__panel {
  min-width: 0;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.product-dict-page__search {
  margin-bottom: 8px;

  :deep(.el-form-item) {
    margin-right: 12px;
    margin-bottom: 10px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 190px;
  }
}

.product-dict-page__search--items {
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.product-dict-page__toolbar,
.product-dict-page__item-head {
  display: flex;
  align-items: flex-start;
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
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.product-dict-page__table {
  :deep(.el-table__body tr) {
    cursor: pointer;
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
  .product-dict-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .product-dict-page__form {
    grid-template-columns: 1fr;
  }

  .product-dict-page__toolbar,
  .product-dict-page__item-head {
    display: grid;
  }

  .product-dict-page__actions {
    justify-content: flex-start;
  }
}
</style>
