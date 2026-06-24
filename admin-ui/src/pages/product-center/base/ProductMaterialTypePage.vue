<template>
  <div class="app-container material-type-page">
    <div class="material-type-page__search-bar">
      <el-form :model="groupQuery" :inline="true" class="material-type-page__search">
        <el-form-item :label="t('productCenter.materialType.groupName')">
          <el-input v-model="groupQuery.groupNameCn" clearable :placeholder="t('productCenter.common.inputPlaceholder')" @keyup.enter="loadGroups" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadGroups">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetGroupQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-form :model="typeQuery" :inline="true" class="material-type-page__search">
        <el-form-item :label="t('productCenter.materialType.name')">
          <el-input v-model="typeQuery.materialTypeNameCn" clearable :placeholder="t('productCenter.common.inputPlaceholder')" :disabled="!selectedGroup" @keyup.enter="loadTypes" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :disabled="!selectedGroup" @click="loadTypes">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :disabled="!selectedGroup" @click="resetTypeQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="material-type-page__grids">
      <section class="material-type-page__pane material-type-page__groups">
        <div class="material-type-page__toolbar">
          <div class="material-type-page__title">
            <strong>{{ t('productCenter.materialType.group') }}</strong>
          </div>
          <div class="material-type-page__actions">
            <el-button type="primary" plain icon="Plus" @click="openGroupForm()" v-hasPermi="['product:material-type:add']">{{ t('common.add') }}</el-button>
            <el-button type="success" plain icon="Edit" :disabled="!selectedGroup" @click="openGroupForm(selectedGroup)" v-hasPermi="['product:material-type:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedGroup" @click="removeGroup" v-hasPermi="['product:material-type:remove']">{{ t('common.delete') }}</el-button>
          </div>
        </div>
        <el-table
          v-loading="groupLoading"
          :data="groups"
          border
          row-key="groupId"
          highlight-current-row
          class="material-type-page__table"
          @row-click="selectGroup"
        >
          <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
          <el-table-column prop="groupCode" :label="t('productCenter.materialType.groupCode')" min-width="120" show-overflow-tooltip />
          <el-table-column prop="groupNameCn" :label="t('productCenter.materialType.groupName')" min-width="120" show-overflow-tooltip />
          <el-table-column prop="status" :label="t('productCenter.common.status')" width="88" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status"
                active-value="ENABLED"
                inactive-value="DISABLED"
                @change="changeGroupStatus(row, $event)"
              />
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="material-type-page__pane material-type-page__types">
        <div class="material-type-page__toolbar">
          <div class="material-type-page__title">
            <strong>{{ t('productCenter.materialType.type') }}</strong>
            <span v-if="selectedGroup">{{ selectedGroup.groupNameCn }} / {{ selectedGroup.groupCode }}</span>
          </div>
          <div class="material-type-page__actions">
            <el-button type="primary" plain icon="Plus" :disabled="!selectedGroup" @click="openTypeForm()" v-hasPermi="['product:material-type:add']">{{ t('common.add') }}</el-button>
            <el-button type="success" plain icon="Edit" :disabled="!selectedType" @click="openTypeForm(selectedType)" v-hasPermi="['product:material-type:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedType" @click="removeType" v-hasPermi="['product:material-type:remove']">{{ t('common.delete') }}</el-button>
            <el-button type="info" plain icon="View" :disabled="!selectedType" @click="openReference('type')" v-hasPermi="['product:material-type:reference']">{{ t('productCenter.common.references') }}</el-button>
          </div>
        </div>
        <el-alert
          v-if="!selectedGroup"
          type="info"
          show-icon
          :closable="false"
          :title="t('productCenter.materialType.selectGroupFirst')"
          class="material-type-page__hint"
        />
        <el-table
          v-loading="typeLoading"
          :data="types"
          border
          row-key="materialTypeId"
          highlight-current-row
          class="material-type-page__table"
          @row-click="selectType"
        >
          <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
          <el-table-column prop="materialTypeCode" :label="t('productCenter.materialType.code')" min-width="150" show-overflow-tooltip />
          <el-table-column prop="materialTypeNameCn" :label="t('productCenter.materialType.name')" min-width="150" show-overflow-tooltip />
          <el-table-column prop="materialTypeNameEn" :label="t('productCenter.materialType.nameEn')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" :label="t('productCenter.common.status')" width="88" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status"
                active-value="ENABLED"
                inactive-value="DISABLED"
                @change="changeTypeStatus(row, $event)"
              />
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <el-drawer v-model="groupOpen" :title="groupForm.groupId ? t('productCenter.common.editTitle', { name: t('productCenter.materialType.group') }) : t('productCenter.common.addTitle', { name: t('productCenter.materialType.group') })" size="640px" append-to-body>
      <el-form ref="groupFormRef" :model="groupForm" :rules="groupRules" label-width="132px">
        <el-form-item :label="t('productCenter.materialType.groupCode')" prop="groupCode">
          <el-input v-model="groupForm.groupCode" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.groupName')" prop="groupNameCn">
          <el-input v-model="groupForm.groupNameCn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.groupNameEn')">
          <el-input v-model="groupForm.groupNameEn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')">
          <el-input-number v-model="groupForm.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.remark')">
          <el-input v-model="groupForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitGroup">{{ t('common.confirm') }}</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="typeOpen" :title="typeForm.materialTypeId ? t('productCenter.common.editTitle', { name: t('productCenter.materialType.type') }) : t('productCenter.common.addTitle', { name: t('productCenter.materialType.type') })" size="640px" append-to-body>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="132px">
        <el-form-item :label="t('productCenter.materialType.group')">
          <el-input :model-value="selectedGroupLabel" disabled />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.code')" prop="materialTypeCode">
          <el-input v-model="typeForm.materialTypeCode" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.name')" prop="materialTypeNameCn">
          <el-input v-model="typeForm.materialTypeNameCn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.nameEn')">
          <el-input v-model="typeForm.materialTypeNameEn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')">
          <el-input-number v-model="typeForm.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.remark')">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitType">{{ t('common.confirm') }}</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="referenceOpen" :title="t('productCenter.common.references')" size="420px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('productCenter.common.canRemove')">
          {{ referenceAllowed ? t('common.yes') : t('common.no') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.referenceCount')">
          {{ referenceResult.referenceCount ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="referenceResult.blockerReasonKey" class="material-type-page__hint" :title="t(referenceResult.blockerReasonKey)" type="warning" show-icon :closable="false" />
      <el-table v-if="referenceSummaries.length" :data="referenceSummaries" border class="material-type-page__reference-table">
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column :label="t('productCenter.common.referenceSummary')" prop="summary" show-overflow-tooltip />
      </el-table>
      <el-empty v-else :description="t('productCenter.common.noReferences')" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="ProductMaterialTypePage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productMaterialTypeApi, productMaterialTypeGroupApi } from '@/api/product-capability/base'
import type { ProductMaterialTypeGroupQuery, ProductMaterialTypeGroupVO, ProductMaterialTypeQuery, ProductMaterialTypeVO, ReferenceCheckResult } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, unknown>) => {
  let message = getMessage(key, localeStore.language)
  Object.entries(params || {}).forEach(([name, value]) => {
    message = message.replace(new RegExp(`\\{${name}\\}`, 'g'), String(value ?? ''))
  })
  return message
}

const groupLoading = ref(false)
const typeLoading = ref(false)
const submitLoading = ref(false)
const groups = ref<ProductMaterialTypeGroupVO[]>([])
const types = ref<ProductMaterialTypeVO[]>([])
const selectedGroup = ref<ProductMaterialTypeGroupVO>()
const selectedType = ref<ProductMaterialTypeVO>()
const groupOpen = ref(false)
const typeOpen = ref(false)
const referenceOpen = ref(false)
const referenceResult = ref<ReferenceCheckResult>({})
const groupFormRef = ref<FormInstance>()
const typeFormRef = ref<FormInstance>()
const groupQuery = reactive<ProductMaterialTypeGroupQuery>({ pageNum: 1, pageSize: 500 })
const typeQuery = reactive<ProductMaterialTypeQuery>({ pageNum: 1, pageSize: 500 })
const groupForm = reactive<ProductMaterialTypeGroupVO>({})
const typeForm = reactive<ProductMaterialTypeVO>({})

const groupRules = computed<FormRules>(() => ({
  groupCode: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.materialType.groupCode') }), trigger: 'blur' }],
  groupNameCn: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.materialType.groupName') }), trigger: 'blur' }]
}))
const typeRules = computed<FormRules>(() => ({
  materialTypeCode: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.materialType.code') }), trigger: 'blur' }],
  materialTypeNameCn: [{ required: true, message: t('productCenter.common.required', { name: t('productCenter.materialType.name') }), trigger: 'blur' }]
}))
const selectedGroupLabel = computed(() => selectedGroup.value ? `${selectedGroup.value.groupCode || ''} ${selectedGroup.value.groupNameCn || ''}`.trim() : '')
const referenceAllowed = computed(() => Number(referenceResult.value.referenceCount || 0) <= 0 && referenceResult.value.allowed !== false)
const referenceSummaries = computed(() => (referenceResult.value.referenceSummaries || []).map((summary) => ({ summary })))

async function loadGroups() {
  groupLoading.value = true
  try {
    const response = await productMaterialTypeGroupApi.list(groupQuery)
    groups.value = response.rows || []
    if (!selectedGroup.value && groups.value.length) {
      selectGroup(groups.value[0])
    } else if (selectedGroup.value) {
      const matched = groups.value.find((item) => item.groupId === selectedGroup.value?.groupId)
      if (matched) selectGroup(matched)
    }
  } finally {
    groupLoading.value = false
  }
}

async function loadTypes() {
  if (!selectedGroup.value?.groupCode) {
    types.value = []
    selectedType.value = undefined
    return
  }
  typeLoading.value = true
  try {
    const response = await productMaterialTypeApi.list({ ...typeQuery, attributeGroupCode: selectedGroup.value.groupCode })
    types.value = response.rows || []
    selectedType.value = undefined
  } finally {
    typeLoading.value = false
  }
}

function selectGroup(row: ProductMaterialTypeGroupVO) {
  selectedGroup.value = row
  selectedType.value = undefined
  loadTypes()
}

function selectType(row: ProductMaterialTypeVO) {
  selectedType.value = row
}

function resetGroupQuery() {
  groupQuery.groupNameCn = undefined
  loadGroups()
}

function resetTypeQuery() {
  typeQuery.materialTypeNameCn = undefined
  loadTypes()
}

function assign<T extends Record<string, unknown>>(target: T, source: Record<string, unknown>) {
  Object.keys(target).forEach((key) => delete target[key])
  Object.assign(target, source)
}

function openGroupForm(row?: ProductMaterialTypeGroupVO) {
  assign(groupForm, { status: 'ENABLED', systemFlag: false, editableFlag: true, sortOrder: 0, ...(row || {}) })
  groupOpen.value = true
}

function openTypeForm(row?: ProductMaterialTypeVO) {
  if (!selectedGroup.value) return
  assign(typeForm, {
    status: 'ENABLED',
    systemFlag: false,
    editableFlag: true,
    sortOrder: 0,
    attributeGroupId: selectedGroup.value.groupId,
    attributeGroupCode: selectedGroup.value.groupCode,
    attributeGroupNameCn: selectedGroup.value.groupNameCn,
    ...(row || {})
  })
  typeOpen.value = true
}

async function submitGroup() {
  await groupFormRef.value?.validate()
  submitLoading.value = true
  try {
    if (groupForm.groupId) {
      await productMaterialTypeGroupApi.update(groupForm)
    } else {
      await productMaterialTypeGroupApi.add(groupForm)
    }
    groupOpen.value = false
    ElMessage.success(t('common.success'))
    await loadGroups()
  } finally {
    submitLoading.value = false
  }
}

async function submitType() {
  await typeFormRef.value?.validate()
  submitLoading.value = true
  try {
    if (typeForm.materialTypeId) {
      await productMaterialTypeApi.update(typeForm)
    } else {
      await productMaterialTypeApi.add(typeForm)
    }
    typeOpen.value = false
    ElMessage.success(t('common.success'))
    await loadTypes()
  } finally {
    submitLoading.value = false
  }
}

async function removeGroup() {
  if (!selectedGroup.value?.groupId) return
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productMaterialTypeGroupApi.remove(selectedGroup.value.groupId)
  ElMessage.success(t('common.success'))
  selectedGroup.value = undefined
  await loadGroups()
}

async function removeType() {
  if (!selectedType.value?.materialTypeId) return
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await productMaterialTypeApi.remove(selectedType.value.materialTypeId)
  ElMessage.success(t('common.success'))
  await loadTypes()
}

async function changeGroupStatus(row: ProductMaterialTypeGroupVO, status: string | number | boolean) {
  if (!row.groupId) return
  await productMaterialTypeGroupApi.changeStatus?.(row.groupId, String(status))
  row.status = String(status)
}

async function changeTypeStatus(row: ProductMaterialTypeVO, status: string | number | boolean) {
  if (!row.materialTypeId) return
  await productMaterialTypeApi.changeStatus?.(row.materialTypeId, String(status))
  row.status = String(status)
}

async function openReference(target: 'group' | 'type') {
  const response = target === 'group' && selectedGroup.value?.groupId
    ? await productMaterialTypeGroupApi.references?.(selectedGroup.value.groupId)
    : selectedType.value?.materialTypeId
      ? await productMaterialTypeApi.references?.(selectedType.value.materialTypeId)
      : undefined
  referenceResult.value = unwrapReferenceResponse(response)
  referenceOpen.value = true
}

function unwrapReferenceResponse(response: unknown): ReferenceCheckResult {
  if (!response) return {}
  if (typeof response === 'object' && 'data' in response) {
    return (response as { data?: ReferenceCheckResult }).data || {}
  }
  return response as ReferenceCheckResult
}

onMounted(loadGroups)
</script>

<style scoped lang="scss">
.material-type-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-type-page__search-bar {
  display: grid;
  grid-template-columns: minmax(480px, 560px) minmax(0, 1fr);
  gap: 12px;
  padding: 12px 12px 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
  align-items: start;
}

.material-type-page__grids {
  display: grid;
  grid-template-columns: minmax(480px, 560px) minmax(0, 1fr);
  gap: 12px;
}

.material-type-page__pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px;
}

.material-type-page__search {
  display: flex;
  flex-wrap: nowrap;
  align-items: flex-start;
  margin-bottom: 0;
  min-width: 0;

  :deep(.el-form-item) {
    margin-right: 8px;
    margin-bottom: 8px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 168px;
  }
}

.material-type-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.material-type-page__title {
  display: grid;
  gap: 4px;
  flex: 1;
  min-width: 120px;

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

.material-type-page__actions {
  display: flex;
  flex-wrap: nowrap;
  justify-content: flex-end;
  gap: 8px;
}

.material-type-page__table {
  width: 100%;

  :deep(.el-table__body tr) {
    cursor: pointer;
  }

  :deep(.el-table__cell) {
    padding: 10px 0;
  }
}

.material-type-page__hint {
  margin-bottom: 8px;
}

.material-type-page__reference-table {
  margin-top: 12px;
}

@media (max-width: 1200px) {
  .material-type-page__search-bar,
  .material-type-page__grids {
    grid-template-columns: 1fr;
  }

  .material-type-page__search {
    flex-wrap: wrap;
  }
}
</style>
