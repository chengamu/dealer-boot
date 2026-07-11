<template>
  <div class="app-container material-type-page" data-agent-scope="product-base-page" data-agent-entity="material-type">
    <div class="material-type-page__search-bar">
      <el-form :model="groupQuery" :inline="true" class="material-type-page__search" data-agent-scope="material-type-group-search">
        <el-form-item :label="t('productCenter.materialType.groupName')" data-agent-field="groupNameCn">
          <el-input v-model="groupQuery.groupNameCn" clearable :placeholder="t('productCenter.common.inputPlaceholder')" :aria-label="t('productCenter.materialType.groupName')" :data-agent-label="t('productCenter.materialType.groupName')" @keyup.enter="loadGroups" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :aria-label="t('common.search')" :data-agent-label="t('common.search')" @click="loadGroups">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :aria-label="t('common.reset')" :data-agent-label="t('common.reset')" @click="resetGroupQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-form :model="typeQuery" :inline="true" class="material-type-page__search" data-agent-scope="material-type-search">
        <el-form-item :label="t('productCenter.materialType.name')" data-agent-field="materialTypeNameCn">
          <el-input v-model="typeQuery.materialTypeNameCn" clearable :placeholder="t('productCenter.common.inputPlaceholder')" :disabled="!selectedGroup" :aria-label="t('productCenter.materialType.name')" :data-agent-label="t('productCenter.materialType.name')" @keyup.enter="loadTypes" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :disabled="!selectedGroup" :aria-label="t('common.search')" :data-agent-label="t('common.search')" @click="loadTypes">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" :disabled="!selectedGroup" :aria-label="t('common.reset')" :data-agent-label="t('common.reset')" @click="resetTypeQuery">{{ t('common.reset') }}</el-button>
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
            <el-button type="primary" icon="Plus" :aria-label="t('common.add')" :data-agent-label="t('common.add')" @click="openGroupForm()" v-hasPermi="['product:material-type:add']">{{ t('common.add') }}</el-button>
            <el-button plain icon="Edit" :disabled="!selectedGroup" :aria-label="agentGroupActionLabel(t('common.edit'))" :data-agent-label="agentGroupActionLabel(t('common.edit'))" data-agent-action="edit-material-type-group" @click="openGroupForm(selectedGroup)" v-hasPermi="['product:material-type:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedGroup" :aria-label="agentGroupActionLabel(t('common.delete'))" :data-agent-label="agentGroupActionLabel(t('common.delete'))" data-agent-action="delete-material-type-group" data-agent-danger="delete" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能删除" @click="removeGroup" v-hasPermi="['product:material-type:remove']">{{ t('common.delete') }}</el-button>
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
          <el-table-column prop="formulaSummaryVisibleFlag" :label="t('productCenter.materialType.formulaSummaryVisible')" width="112" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.formulaSummaryVisibleFlag !== false"
                :aria-label="agentRowActionLabel(t('productCenter.materialType.formulaSummaryVisible'), groupLabel(row))"
                :data-agent-label="agentRowActionLabel(t('productCenter.materialType.formulaSummaryVisible'), groupLabel(row))"
                :data-agent-row="groupLabel(row)"
                data-agent-action="change-formula-summary-visible"
                @change="changeGroupFormulaSummaryVisible(row, $event)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('productCenter.common.status')" width="88" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status"
                :active-value="PRODUCT_STATUS_ENABLED"
                :inactive-value="PRODUCT_STATUS_DISABLED"
                :aria-label="agentStatusLabel(groupLabel(row), row.status)"
                :data-agent-label="agentStatusLabel(groupLabel(row), row.status)"
                :data-agent-row="groupLabel(row)"
                :data-agent-status="normalizeStatus(row.status)"
                data-agent-risk="confirm-required"
                data-agent-confirm-required="true"
                data-agent-confirm-message="修改状态需要用户人工确认"
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
            <el-button type="primary" icon="Plus" :disabled="!selectedGroup" :aria-label="t('common.add')" :data-agent-label="t('common.add')" @click="openTypeForm()" v-hasPermi="['product:material-type:add']">{{ t('common.add') }}</el-button>
            <el-button plain icon="Edit" :disabled="!selectedType" :aria-label="agentTypeActionLabel(t('common.edit'))" :data-agent-label="agentTypeActionLabel(t('common.edit'))" data-agent-action="edit-material-type" @click="openTypeForm(selectedType)" v-hasPermi="['product:material-type:edit']">{{ t('common.edit') }}</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="!selectedType" :aria-label="agentTypeActionLabel(t('common.delete'))" :data-agent-label="agentTypeActionLabel(t('common.delete'))" data-agent-action="delete-material-type" data-agent-danger="delete" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能删除" @click="removeType" v-hasPermi="['product:material-type:remove']">{{ t('common.delete') }}</el-button>
            <el-button plain icon="View" :disabled="!selectedType" :aria-label="agentTypeActionLabel(t('productCenter.common.references'))" :data-agent-label="agentTypeActionLabel(t('productCenter.common.references'))" data-agent-action="reference-material-type" @click="openReference('type')" v-hasPermi="['product:material-type:reference']">{{ t('productCenter.common.references') }}</el-button>
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
                :active-value="PRODUCT_STATUS_ENABLED"
                :inactive-value="PRODUCT_STATUS_DISABLED"
                :aria-label="agentStatusLabel(typeLabel(row), row.status)"
                :data-agent-label="agentStatusLabel(typeLabel(row), row.status)"
                :data-agent-row="typeLabel(row)"
                :data-agent-status="normalizeStatus(row.status)"
                data-agent-risk="confirm-required"
                data-agent-confirm-required="true"
                data-agent-confirm-message="修改状态需要用户人工确认"
                @change="changeTypeStatus(row, $event)"
              />
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <AdminDrawer
      v-model="groupOpen"
      :title="groupForm.groupId ? t('productCenter.common.editTitle', { name: t('productCenter.materialType.group') }) : t('productCenter.common.addTitle', { name: t('productCenter.materialType.group') })"
      size="640px"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleGroupDrawerBeforeClose"
      @closed="handleGroupDrawerClosed"
    >
      <el-form ref="groupFormRef" :model="groupForm" :rules="groupRules" label-width="132px" data-agent-scope="material-type-group-form">
        <el-form-item :label="t('productCenter.materialType.groupCode')" prop="groupCode" data-agent-field="groupCode">
          <el-input v-model="groupForm.groupCode" :aria-label="t('productCenter.materialType.groupCode')" :data-agent-label="t('productCenter.materialType.groupCode')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.groupName')" prop="groupNameCn" data-agent-field="groupNameCn">
          <el-input v-model="groupForm.groupNameCn" :aria-label="t('productCenter.materialType.groupName')" :data-agent-label="t('productCenter.materialType.groupName')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.groupNameEn')" data-agent-field="groupNameEn">
          <el-input v-model="groupForm.groupNameEn" :aria-label="t('productCenter.materialType.groupNameEn')" :data-agent-label="t('productCenter.materialType.groupNameEn')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder">
          <el-input v-model="groupForm.sortOrder" inputmode="numeric" class="material-type-page__sort-input" :aria-label="t('productCenter.common.sortOrder')" :data-agent-label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder" data-agent-input-kind="number" @keyup.enter="submitGroup" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.formulaSummaryVisible')" data-agent-field="formulaSummaryVisibleFlag">
          <el-switch
            v-model="groupForm.formulaSummaryVisibleFlag"
            :aria-label="t('productCenter.materialType.formulaSummaryVisible')"
            :data-agent-label="t('productCenter.materialType.formulaSummaryVisible')"
          />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.remark')" data-agent-field="remark">
          <el-input v-model="groupForm.remark" type="textarea" :rows="3" :aria-label="t('productCenter.common.remark')" :data-agent-label="t('productCenter.common.remark')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeGroupDrawerWithGuard">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" data-agent-danger="save" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能保存" @click="submitGroup">{{ t('common.confirm') }}</el-button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="typeOpen"
      :title="typeForm.materialTypeId ? t('productCenter.common.editTitle', { name: t('productCenter.materialType.type') }) : t('productCenter.common.addTitle', { name: t('productCenter.materialType.type') })"
      size="640px"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleTypeDrawerBeforeClose"
      @closed="handleTypeDrawerClosed"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="132px" data-agent-scope="material-type-form">
        <el-form-item :label="t('productCenter.materialType.group')" data-agent-field="attributeGroupCode">
          <el-input :model-value="selectedGroupLabel" disabled :aria-label="t('productCenter.materialType.group')" :data-agent-label="t('productCenter.materialType.group')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.code')" prop="materialTypeCode" data-agent-field="materialTypeCode">
          <el-input v-model="typeForm.materialTypeCode" :aria-label="t('productCenter.materialType.code')" :data-agent-label="t('productCenter.materialType.code')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.name')" prop="materialTypeNameCn" data-agent-field="materialTypeNameCn">
          <el-input v-model="typeForm.materialTypeNameCn" :aria-label="t('productCenter.materialType.name')" :data-agent-label="t('productCenter.materialType.name')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.materialType.nameEn')" data-agent-field="materialTypeNameEn">
          <el-input v-model="typeForm.materialTypeNameEn" :aria-label="t('productCenter.materialType.nameEn')" :data-agent-label="t('productCenter.materialType.nameEn')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder">
          <el-input v-model="typeForm.sortOrder" inputmode="numeric" class="material-type-page__sort-input" :aria-label="t('productCenter.common.sortOrder')" :data-agent-label="t('productCenter.common.sortOrder')" data-agent-field="sortOrder" data-agent-input-kind="number" @keyup.enter="submitType" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.remark')" data-agent-field="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" :aria-label="t('productCenter.common.remark')" :data-agent-label="t('productCenter.common.remark')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeTypeDrawerWithGuard">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" data-agent-danger="save" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能保存" @click="submitType">{{ t('common.confirm') }}</el-button>
      </template>
    </AdminDrawer>

    <AdminDrawer v-model="referenceOpen" :title="t('productCenter.common.references')" size="420px" variant="detail" append-to-body>
      <div class="admin-reference">
        <div class="admin-reference__summary">
          <div class="admin-reference__metric">
            <span>{{ t('productCenter.common.canRemove') }}</span>
            <strong :class="referenceAllowed ? 'is-success' : 'is-danger'">{{ referenceAllowed ? t('common.yes') : t('common.no') }}</strong>
          </div>
          <div class="admin-reference__metric">
            <span>{{ t('productCenter.common.referenceCount') }}</span>
            <strong>{{ referenceResult.referenceCount ?? 0 }}</strong>
          </div>
        </div>
        <el-alert v-if="referenceResult.blockerReasonKey" :title="t(referenceResult.blockerReasonKey)" type="warning" show-icon :closable="false" />
        <div v-if="referenceSummaries.length" class="admin-reference__list">
          <div v-for="(item, index) in referenceSummaries" :key="`${index}-${item.summary}`" class="admin-reference__item">
            <span>{{ index + 1 }}</span>
            <p>{{ item.summary }}</p>
          </div>
        </div>
        <el-empty v-else :description="t('productCenter.common.noReferences')" />
      </div>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="ProductMaterialTypePage">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productMaterialTypeApi, productMaterialTypeGroupApi } from '@/api/product-capability/base'
import type { ProductMaterialTypeGroupQuery, ProductMaterialTypeGroupVO, ProductMaterialTypeQuery, ProductMaterialTypeVO, ReferenceCheckResult } from '@/api/product-capability/types'
import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED, normalizeProductStatus } from '@/constants/productStatus'
import { compactParts } from '@/utils/productLabels'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'

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
const groupUnsavedChangesGuard = useUnsavedChangesGuard({
  enabled: () => groupOpen.value,
  getSnapshot: () => JSON.stringify(groupForm || {}),
  confirmDiscard: confirmDiscardChanges
})
const typeUnsavedChangesGuard = useUnsavedChangesGuard({
  enabled: () => typeOpen.value,
  getSnapshot: () => JSON.stringify(typeForm || {}),
  confirmDiscard: confirmDiscardChanges
})

function normalizeStatus(value: unknown) {
  return normalizeProductStatus(value)
}

function statusLabel(value: unknown) {
  return normalizeStatus(value) === PRODUCT_STATUS_ENABLED ? t('productCenter.status.enabled') : t('productCenter.status.disabled')
}

function groupLabel(row?: ProductMaterialTypeGroupVO) {
  if (!row) return t('productCenter.materialType.group')
  const name = localeStore.language === 'zh_CN' ? row.groupNameCn : row.groupNameEn || row.groupNameCn
  return compactParts(t('productCenter.materialType.group'), row.groupCode, name)
}

function typeLabel(row?: ProductMaterialTypeVO) {
  if (!row) return t('productCenter.materialType.type')
  const name = localeStore.language === 'zh_CN' ? row.materialTypeNameCn : row.materialTypeNameEn || row.materialTypeNameCn
  return compactParts(t('productCenter.materialType.type'), row.materialTypeCode, name)
}

function agentRowActionLabel(action: string, rowLabel: string) {
  return compactParts(action, rowLabel)
}

function agentGroupActionLabel(action: string) {
  return agentRowActionLabel(action, groupLabel(selectedGroup.value))
}

function agentTypeActionLabel(action: string) {
  return agentRowActionLabel(action, typeLabel(selectedType.value))
}

function agentStatusLabel(rowLabel: string, status: unknown) {
  return compactParts(
    rowLabel,
    t('productCenter.common.status'),
    t('productCenter.common.currentStatus', { status: statusLabel(status) }),
    t('productCenter.common.statusChangeConfirm')
  )
}

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

async function openGroupForm(row?: ProductMaterialTypeGroupVO) {
  if (row?.groupId && !(await checkBeforeEdit(productMaterialTypeGroupApi.editCheck, row.groupId))) return
  assign(groupForm, { status: PRODUCT_STATUS_ENABLED, systemFlag: false, editableFlag: true, formulaSummaryVisibleFlag: true, sortOrder: 0, ...(row || {}) })
  groupUnsavedChangesGuard.markPristine()
  groupOpen.value = true
}

async function openTypeForm(row?: ProductMaterialTypeVO) {
  if (!selectedGroup.value) return
  if (row?.materialTypeId && !(await checkBeforeEdit(productMaterialTypeApi.editCheck, row.materialTypeId))) return
  assign(typeForm, {
    status: PRODUCT_STATUS_ENABLED,
    systemFlag: false,
    editableFlag: true,
    sortOrder: 0,
    attributeGroupId: selectedGroup.value.groupId,
    attributeGroupCode: selectedGroup.value.groupCode,
    attributeGroupNameCn: selectedGroup.value.groupNameCn,
    ...(row || {})
  })
  typeUnsavedChangesGuard.markPristine()
  typeOpen.value = true
}

async function confirmDiscardChanges() {
  try {
    await ElMessageBox.confirm(t('common.unsavedChangesConfirm'), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

async function closeGroupDrawerWithGuard() {
  await groupUnsavedChangesGuard.closeWithGuard(() => {
    groupOpen.value = false
  })
}

async function closeTypeDrawerWithGuard() {
  await typeUnsavedChangesGuard.closeWithGuard(() => {
    typeOpen.value = false
  })
}

function handleGroupDrawerBeforeClose(done: () => void) {
  groupUnsavedChangesGuard.canClose().then((allowed) => {
    if (allowed) done()
  })
}

function handleTypeDrawerBeforeClose(done: () => void) {
  typeUnsavedChangesGuard.canClose().then((allowed) => {
    if (allowed) done()
  })
}

function handleGroupDrawerClosed() {
  groupUnsavedChangesGuard.resetPristine()
}

function handleTypeDrawerClosed() {
  typeUnsavedChangesGuard.resetPristine()
}

function handleDrawerShortcut(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (groupOpen.value) {
    event.preventDefault()
    closeGroupDrawerWithGuard()
    return
  }
  if (typeOpen.value) {
    event.preventDefault()
    closeTypeDrawerWithGuard()
  }
}

async function checkBeforeEdit(editCheck: typeof productMaterialTypeApi.editCheck, id: string | number) {
  if (!editCheck) return true
  const response = await editCheck(id)
  const result = response.data || {}
  if (result.editable !== false) return true
  const reasonKey = result.reasonKey || result.reason || 'productCenter.common.editDenied'
  ElMessage.warning(t(reasonKey))
  return false
}

async function submitGroup() {
  await groupFormRef.value?.validate()
  groupForm.sortOrder = normalizeSortOrder(groupForm.sortOrder)
  submitLoading.value = true
  try {
    if (groupForm.groupId) {
      await productMaterialTypeGroupApi.update(groupForm)
    } else {
      await productMaterialTypeGroupApi.add(groupForm)
    }
    groupUnsavedChangesGuard.markPristine()
    groupOpen.value = false
    ElMessage.success(t('common.success'))
    await loadGroups()
  } finally {
    submitLoading.value = false
  }
}

async function submitType() {
  await typeFormRef.value?.validate()
  typeForm.sortOrder = normalizeSortOrder(typeForm.sortOrder)
  submitLoading.value = true
  try {
    if (typeForm.materialTypeId) {
      await productMaterialTypeApi.update(typeForm)
    } else {
      await productMaterialTypeApi.add(typeForm)
    }
    typeUnsavedChangesGuard.markPristine()
    typeOpen.value = false
    ElMessage.success(t('common.success'))
    await loadTypes()
  } finally {
    submitLoading.value = false
  }
}

function normalizeSortOrder(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) return 0
  const parsed = Number(text)
  return Number.isFinite(parsed) ? parsed : 0
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

async function changeGroupFormulaSummaryVisible(row: ProductMaterialTypeGroupVO, value: string | number | boolean) {
  if (!row.groupId) return
  const previous = row.formulaSummaryVisibleFlag !== false
  const nextValue = Boolean(value)
  row.formulaSummaryVisibleFlag = nextValue
  try {
    await productMaterialTypeGroupApi.update({ ...row, formulaSummaryVisibleFlag: nextValue })
    ElMessage.success(t('common.success'))
  } catch (error) {
    row.formulaSummaryVisibleFlag = previous
    throw error
  }
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

onMounted(() => {
  window.addEventListener('keydown', handleDrawerShortcut)
  loadGroups()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleDrawerShortcut)
})
</script>

<style scoped lang="scss">
.material-type-page {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.material-type-page__search-bar {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 1fr);
  gap: 10px;
  padding: 10px 12px 2px;
  border: 1px solid #eef0f5;
  border-radius: 8px;
  background: #ffffff;
  align-items: start;
}

.material-type-page__grids {
  display: grid;
  grid-template-columns: minmax(480px, 560px) minmax(0, 1fr);
  gap: 10px;
  align-items: stretch;
}

.material-type-page__pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 420px;
  background: #ffffff;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  padding: 10px 12px;
}

.material-type-page__search {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  margin-bottom: 0;
  min-width: 0;

  :deep(.el-form-item) {
    margin-right: 12px;
    margin-bottom: 8px;
  }

  :deep(.el-form-item:last-child) {
    margin-right: 0;
    margin-left: auto;
  }

  :deep(.el-form-item__label) {
    padding-right: 6px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 168px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
  }

  :deep(.el-button:not(.el-button--primary)) {
    border-color: #d9e0ea;
    color: #475467;
    background: #ffffff;

    &:hover,
    &:focus {
      border-color: #c7d0dd;
      color: #344054;
      background: #f8fafc;
    }
  }
}

.material-type-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 36px;
  margin-bottom: 8px;
}

.material-type-page__title {
  display: grid;
  gap: 4px;
  flex: 1;
  min-width: 120px;

  strong {
    color: #1d2129;
    font-size: 15px;
    font-weight: 600;
  }

  span {
    color: #98a2b3;
    font-size: 13px;
    line-height: 1.4;
  }
}

.material-type-page__actions {
  display: flex;
  flex-wrap: nowrap;
  justify-content: flex-end;
  gap: 8px;

  :deep(.el-button) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
  }

  :deep(.el-button--primary.is-plain) {
    border-color: #1677ff;
    background: #1677ff;
    color: #ffffff;

    &:hover,
    &:focus {
      border-color: #0958d9;
      background: #0958d9;
      color: #ffffff;
    }
  }

  :deep(.el-button--success.is-plain) {
    border-color: #b8d2ff;
    color: #1677ff;
    background: #f8fbff;

    &:hover,
    &:focus {
      border-color: #8bb8ff;
      color: #0958d9;
      background: #eef5ff;
    }
  }

  :deep(.el-button--danger.is-plain) {
    border-color: #ffd6d9;
    color: #dc3545;
    background: #fffafa;

    &:hover,
    &:focus {
      border-color: #ffb8bf;
      color: #c92a3a;
      background: #fff1f2;
    }
  }

  :deep(.el-button--info.is-plain) {
    border-color: #d9e0ea;
    color: #475467;
    background: #ffffff;

    &:hover,
    &:focus {
      border-color: #c7d0dd;
      color: #344054;
      background: #f8fafc;
    }
  }

  :deep(.el-button.is-disabled),
  :deep(.el-button.is-disabled:hover),
  :deep(.el-button.is-disabled:focus) {
    border-color: #eef0f5;
    color: #b8c2d3;
    background: #f8fafc;
  }
}

.material-type-page__table {
  flex: 1;
  width: 100%;

  :deep(.el-table__inner-wrapper::before),
  :deep(.el-table__border-left-patch),
  :deep(.el-table__border-bottom-patch) {
    background-color: #eef0f5;
  }

  :deep(.el-table__header th) {
    background: #f7f9fc;
    color: #344054;
    font-weight: 600;
  }

  :deep(th.el-table__cell),
  :deep(td.el-table__cell) {
    border-color: #eef0f5;
  }

  :deep(.el-table__body tr) {
    cursor: pointer;
  }

  :deep(.el-table__body tr:hover > td.el-table__cell) {
    background: #f8fbff;
  }

  :deep(.el-table__cell) {
    padding: 8px 0;
  }

  :deep(.el-switch) {
    vertical-align: middle;
  }
}

.material-type-page__sort-input {
  width: 220px;
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
