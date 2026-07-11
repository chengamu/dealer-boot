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
            <el-option :label="t('common.all')" :value="MATERIAL_TYPE_ALL" />
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
          <el-button type="primary" :icon="Connection" @click="$emit('open-picker')">{{ t('productCenter.formulaSetup.batchAddMaterial') }}</el-button>
          <el-button plain :icon="Setting" :disabled="!selectedRows.length" @click="openBatchUsage">{{ t('productCenter.formulaSetup.batchUsageSetting') }}</el-button>
          <el-button type="danger" plain :icon="Delete" :disabled="!selectedRows.length" @click="$emit('remove-materials', selectedRows)">{{ t('common.delete') }}</el-button>
        </div>
      </div>
    </div>
    <el-table v-loading="loading" :data="visibleMaterials" border class="setup-table" row-key="materialCode" @selection-change="selectedRows = $event">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
      <el-table-column :label="t('productCenter.formulaSetup.attributeGroup')" width="82" align="center">
        <template #default="{ row }">
          <el-tag size="small" :class="`group-tag group-tag--${String(row.attributeGroupCode || '').toLowerCase()}`">{{ row.attributeGroupNameCn || row.attributeGroupCode || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialType')" width="96" align="center">
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
      <el-table-column :label="t('productCenter.formulaSetup.unit')" prop="unitCode" width="66" align="center">
        <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="112" align="right">
        <template #default="{ row }">
          <el-input-number v-model="row.lossRate" :min="0" :precision="2" controls-position="right" />
        </template>
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
      <el-table-column :label="t('productCenter.common.sortOrder')" width="96" align="right">
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
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('productCenter.formulaSetup.usage'), icon: Setting, onClick: () => $emit('open-usage', row) },
            { label: t('common.delete'), icon: Delete, type: 'danger', onClick: () => $emit('remove-material', row) }
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
  'remove-material': [row: ProductFormulaMaterialVO]
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

<style scoped src="./FormulaMaterialPoolTab.css"></style>
