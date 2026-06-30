<template>
  <section class="setup-section">
    <div class="setup-section__toolbar">
      <div class="setup-section__title">
        <h3>{{ t('productCenter.formulaSetup.materialPool') }}</h3>
        <span>{{ t('productCenter.formulaSetup.materialPoolHint') }}</span>
      </div>
      <div class="material-pool-toolbar">
        <div class="material-pool-toolbar__filters">
          <el-select
            v-model="attributeGroupFilter"
            filterable
            class="material-type-filter"
            :aria-label="t('productCenter.formulaSetup.materialTypeFilterAria')"
            :placeholder="t('productCenter.formulaSetup.attributeGroup')"
            :prefix-icon="Filter"
          >
            <el-option :label="localeStore.language === 'zh_CN' ? '全部' : 'All'" :value="MATERIAL_TYPE_ALL" />
            <el-option v-for="group in materialGroupCards" :key="group.code" :label="displayGroupName(group)" :value="group.code" />
          </el-select>
          <el-input
            v-model="keyword"
            clearable
            :aria-label="t('productCenter.formulaSetup.materialSearchAria')"
            :placeholder="t('productCenter.formulaSetup.materialSearchPlaceholder')"
            class="material-pool-toolbar__search"
            :prefix-icon="Search"
          />
        </div>
        <div class="material-pool-toolbar__actions">
          <el-button type="primary" plain :icon="Connection" @click="$emit('open-picker')">{{ t('productCenter.formulaSetup.batchAddMaterial') }}</el-button>
          <el-button plain :icon="Setting" :disabled="!selectedRows.length" @click="openBatchUsage">{{ t('productCenter.formulaSetup.batchUsageSetting') }}</el-button>
          <el-button type="danger" plain :icon="Delete" :disabled="!selectedRows.length" @click="$emit('remove-materials', selectedRows)">{{ t('common.delete') }}</el-button>
        </div>
      </div>
    </div>
    <el-table v-loading="loading" :data="visibleMaterials" border class="setup-table" row-key="materialCode" @selection-change="selectedRows = $event">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
      <el-table-column :label="t('productCenter.formulaSetup.attributeGroup')" width="82">
        <template #default="{ row }">
          <el-tag size="small" :class="`group-tag group-tag--${String(row.attributeGroupCode || '').toLowerCase()}`">{{ row.attributeGroupNameCn || row.attributeGroupCode || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialType')" width="96">
        <template #default="{ row }">
          <span class="material-cell-text material-cell-text--compact">{{ row.materialTypeNameCn || row.materialTypeCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialCode')" prop="materialCode" width="112">
        <template #default="{ row }">
          <span class="material-cell-text material-code-text">{{ row.materialCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialName')" prop="materialNameCn" min-width="240">
        <template #default="{ row }">
          <span class="material-cell-text material-name-text">{{ row.materialNameCn || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.specModel')" min-width="190">
        <template #default="{ row }">
          <span class="material-cell-text spec-model-text">{{ row.specModelText || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.unit')" prop="unitCode" width="66">
        <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.usageSummary')" min-width="280">
        <template #default="{ row }">
          <div class="usage-summary-lines">
            <div
              v-for="(line, lineIndex) in usageSummaryLines(row)"
              :key="`${row.materialCode || row.lineNo || ''}-${lineIndex}`"
              class="usage-summary-line"
              :class="{ 'usage-summary-line--condition': isUsageConditionLine(line) }"
            >
              {{ line }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.productionRemark')" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.productionRemark" clearable />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.common.sortOrder')" width="96" align="center">
        <template #default="{ row }">
          <el-input
            :model-value="sortOrderInputValue(row)"
            class="sort-order-input"
            inputmode="numeric"
            @input="updateSortOrderDraft(row, $event)"
            @blur="commitSortOrder(row)"
            @keyup.enter="commitSortOrder(row)"
          />
        </template>
      </el-table-column>
      <el-table-column width="88" align="center">
        <template #header>
          <el-tooltip :content="t('productCenter.formulaSetup.requiredFlagHint')" placement="top">
            <span class="header-help">{{ t('productCenter.formulaSetup.requiredFlag') }}</span>
          </el-tooltip>
        </template>
        <template #default="{ row }"><el-switch v-model="row.requiredFlag" /></template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="176" fixed="right" align="center" class-name="material-actions-column">
        <template #default="{ row, $index }">
          <AdminTableActions :actions="[
            { label: t('productCenter.formulaSetup.usage'), icon: Setting, onClick: () => $emit('open-usage', row) },
            { label: t('common.delete'), icon: Delete, type: 'danger', onClick: () => $emit('remove-material', $index) }
          ]" />
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { computed, reactive, ref } from 'vue'
import { Connection, Delete, Filter, Search, Setting } from '@element-plus/icons-vue'
import type { ProductFormulaMaterialVO } from '@/api/product-capability/types'

interface MaterialGroupCard {
  code: string
  name: string
  nameEn?: string
}

const props = defineProps<{
  loading: boolean
  materials: ProductFormulaMaterialVO[]
  usageSummary: (row: ProductFormulaMaterialVO) => string
  usageUnset: (row: ProductFormulaMaterialVO) => boolean
  unitLabel: (unitCode?: string) => string
  groupSortMap?: Record<string, number>
  materialGroupCards: MaterialGroupCard[]
  materialCount: number
  unsetUsageCount: number
  exceptionCount: number
}>()

const emit = defineEmits<{
  'open-picker': []
  'open-usage': [row: ProductFormulaMaterialVO]
  'open-batch-usage': [rows: ProductFormulaMaterialVO[]]
  'remove-material': [index: number]
  'remove-materials': [rows: ProductFormulaMaterialVO[]]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const unitLabel = (unitCode?: string) => props.unitLabel(unitCode)
const MATERIAL_TYPE_ALL = '__ALL__'
const keyword = ref('')
const attributeGroupFilter = ref(MATERIAL_TYPE_ALL)
const selectedRows = ref<ProductFormulaMaterialVO[]>([])
const sortOrderDrafts = reactive<Record<string, string>>({})
const materialGroupCards = computed(() => props.materialGroupCards.filter((group) => group.code))
const visibleMaterials = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  return props.materials.filter((row) => {
    if (attributeGroupFilter.value !== MATERIAL_TYPE_ALL && row.attributeGroupCode !== attributeGroupFilter.value) return false
    if (!value) return true
    return [row.materialCode, row.materialNameCn, row.specModelText, row.materialTypeNameCn]
      .some((field) => String(field || '').toLowerCase().includes(value))
  }).slice().sort(compareMaterialRows)
})

function displayGroupName(group: MaterialGroupCard) {
  return localeStore.language === 'zh_CN' ? group.name || group.code : group.nameEn || group.name || group.code
}

function usageSummaryLines(row: ProductFormulaMaterialVO) {
  return props.usageSummary(row).split('\n').map((line) => line.trim()).filter(Boolean)
}

function isUsageConditionLine(line: string) {
  return line.endsWith('：') || line.endsWith(':')
}

function compareMaterialRows(left: ProductFormulaMaterialVO, right: ProductFormulaMaterialVO) {
  const leftGroup = props.groupSortMap?.[left.attributeGroupCode || ''] ?? Number.MAX_SAFE_INTEGER
  const rightGroup = props.groupSortMap?.[right.attributeGroupCode || ''] ?? Number.MAX_SAFE_INTEGER
  if (leftGroup !== rightGroup) return leftGroup - rightGroup
  const leftSort = Number(left.sortOrder || left.lineNo || 0)
  const rightSort = Number(right.sortOrder || right.lineNo || 0)
  if (leftSort !== rightSort) return leftSort - rightSort
  return String(left.materialCode || '').localeCompare(String(right.materialCode || ''))
}

function sortOrderInputValue(row: ProductFormulaMaterialVO) {
  const key = sortOrderDraftKey(row)
  if (sortOrderDrafts[key] !== undefined) return sortOrderDrafts[key]
  return row.sortOrder === undefined || row.sortOrder === null ? '' : String(row.sortOrder)
}

function updateSortOrderDraft(row: ProductFormulaMaterialVO, value: string | number) {
  sortOrderDrafts[sortOrderDraftKey(row)] = String(value ?? '')
}

function commitSortOrder(row: ProductFormulaMaterialVO) {
  const key = sortOrderDraftKey(row)
  if (sortOrderDrafts[key] === undefined) return
  const parsed = Number(sortOrderDrafts[key].trim())
  row.sortOrder = Number.isFinite(parsed) ? parsed : 0
  delete sortOrderDrafts[key]
}

function sortOrderDraftKey(row: ProductFormulaMaterialVO) {
  return String(row.materialCode || row.materialId || row.lineNo || '')
}

function openBatchUsage() {
  if (selectedRows.value.length) emit('open-batch-usage', selectedRows.value)
}

</script>

<style scoped>
.setup-section {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.setup-section__toolbar {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.setup-section__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  width: 100%;
  min-width: 0;
}

.setup-section__toolbar h3 {
  flex: 0 0 auto;
  margin: 0;
  font-size: 15px;
  color: #111827;
}

.setup-section__toolbar span {
  min-width: 0;
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.setup-section__actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.material-pool-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  width: 100%;
  min-width: 0;
}

.material-pool-toolbar__filters,
.material-pool-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.material-pool-toolbar__filters {
  flex: 1;
  min-width: 0;
}

.material-pool-toolbar__actions {
  flex: 0 0 auto;
  justify-content: flex-end;
}

.material-pool-toolbar__search {
  width: min(360px, 100%);
}

.material-type-filter {
  width: 168px;
}

.material-pool-toolbar :deep(.el-button) {
  height: 34px;
  border-radius: 6px;
}

.material-pool-toolbar :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 6px;
}

.setup-table {
  border-radius: 6px;
  overflow: hidden;
}

.setup-table :deep(.el-table__header th) {
  height: 44px;
  background: #f7faff !important;
  color: #1f2937;
  font-weight: 700;
}

.setup-table :deep(.el-table__row td) {
  height: auto;
  min-height: 64px;
  padding: 8px 0;
}

.setup-table :deep(.el-input-number) {
  width: 100%;
}

.sort-order-input {
  width: 72px;
}

.sort-order-input :deep(.el-input__inner) {
  text-align: center;
}

.setup-table :deep(.el-input-number .el-input__wrapper),
.setup-table :deep(.el-input .el-input__wrapper),
.setup-table :deep(.el-select .el-select__wrapper) {
  min-height: 32px;
  border-radius: 6px;
}

.setup-table :deep(.material-actions-column .cell) {
  padding: 0 8px;
}

.setup-table :deep(.material-actions-column .admin-table-actions) {
  justify-content: center;
  gap: 8px;
  white-space: nowrap;
}

.setup-table :deep(.material-actions-column .admin-table-actions__button) {
  margin: 0;
}

.header-help {
  cursor: help;
}

.usage-summary-lines {
  color: #374151;
  font-size: 13px;
  line-height: 1.55;
  word-break: break-word;
}

.usage-summary-line {
  margin-top: 2px;
}

.usage-summary-line:first-child {
  margin-top: 0;
}

.usage-summary-line--condition {
  display: inline-block;
  margin-top: 6px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.usage-summary-line--condition:first-child {
  margin-top: 0;
}

.material-cell-text {
  display: -webkit-box;
  overflow: hidden;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.35;
  white-space: normal;
  word-break: break-all;
  -webkit-box-orient: vertical;
}

.material-cell-text--compact {
  -webkit-line-clamp: 2;
}

.material-code-text {
  color: #374151;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
  -webkit-line-clamp: 2;
}

.material-name-text {
  color: #1f2937;
  -webkit-line-clamp: 2;
}

.spec-model-text {
  max-height: 54px;
  -webkit-line-clamp: 3;
}

.group-tag {
  border: 0;
  font-weight: 600;
}

.group-tag--fabric {
  background: #eff6ff;
  color: #2563eb;
}

.group-tag--aluminum {
  background: #ecfdf5;
  color: #16a34a;
}

.group-tag--system {
  background: #f5f3ff;
  color: #7c3aed;
}

.group-tag--accessory {
  background: #fff7ed;
  color: #ea580c;
}

.group-tag--part_pack {
  background: #ecfeff;
  color: #0891b2;
}

.group-tag--packaging {
  background: #f0fdfa;
  color: #0d9488;
}

@media (max-width: 1280px) {
  .setup-section__toolbar {
    flex-wrap: wrap;
  }

  .material-pool-toolbar {
    align-items: center;
    flex-wrap: wrap;
    width: 100%;
  }

  .material-pool-toolbar__actions {
    justify-content: flex-start;
  }
}
</style>
