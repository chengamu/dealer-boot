<template>
  <section class="setup-section">
    <div class="setup-section__toolbar">
      <div class="setup-section__title">
        <h3>{{ t('productCenter.formulaSetup.materialPool') }}</h3>
        <p>{{ t('productCenter.formulaSetup.materialPoolHint') }}</p>
      </div>
      <div class="material-pool-toolbar">
        <div class="material-pool-toolbar__filters">
          <el-select
            v-model="materialTypeFilter"
            filterable
            class="material-type-filter"
            :aria-label="t('productCenter.formulaSetup.materialTypeFilterAria')"
            :placeholder="t('productCenter.formulaSetup.filterByMaterialType')"
            :prefix-icon="Filter"
          >
            <el-option :label="t('productCenter.formulaSetup.allMaterialTypes')" :value="MATERIAL_TYPE_ALL" />
            <el-option v-for="type in materialTypeOptions" :key="type" :label="type" :value="type" />
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
      <el-table-column :label="t('productCenter.formulaSetup.attributeGroup')" width="68">
        <template #default="{ row }">
          <el-tag size="small" :class="`group-tag group-tag--${String(row.attributeGroupCode || '').toLowerCase()}`">{{ row.attributeGroupNameCn || row.attributeGroupCode || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialType')" width="88" show-overflow-tooltip>
        <template #default="{ row }">{{ row.materialTypeNameCn || row.materialTypeCode || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.materialCode')" prop="materialCode" width="96" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.formulaSetup.materialName')" prop="materialNameCn" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.formulaSetup.specModel')" min-width="160">
        <template #default="{ row }">
          <span class="spec-model-text">{{ row.specModelText || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.unit')" prop="unitCode" width="66">
        <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.defaultFlag')" width="76" align="center">
        <template #default="{ row }"><el-switch v-model="row.defaultFlag" /></template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.requiredFlag')" width="76" align="center">
        <template #default="{ row }"><el-switch v-model="row.requiredFlag" /></template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.usageSummary')" min-width="122">
        <template #default="{ row }">{{ usageSummary(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.productionRemark')" min-width="128">
        <template #default="{ row }">
          <el-input v-model="row.productionRemark" clearable />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="118" fixed="right" align="center">
        <template #default="{ row, $index }">
          <AdminTableActions :actions="[
            { label: t('productCenter.formulaSetup.usageSetting'), icon: 'Setting', onClick: () => $emit('open-usage', row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', onClick: () => $emit('remove-material', $index) }
          ]" />
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { computed, ref } from 'vue'
import { Connection, Delete, Filter, Search, Setting } from '@element-plus/icons-vue'
import type { ProductFormulaMaterialVO } from '@/api/product-capability/types'

const props = defineProps<{
  loading: boolean
  materials: ProductFormulaMaterialVO[]
  usageSummary: (row: ProductFormulaMaterialVO) => string
  usageUnset: (row: ProductFormulaMaterialVO) => boolean
  unitLabel: (unitCode?: string) => string
  materialCount: number
  unsetUsageCount: number
  exceptionCount: number
}>()

const emit = defineEmits<{
  'open-picker': []
  'open-usage': [row: ProductFormulaMaterialVO]
  'remove-material': [index: number]
  'remove-materials': [rows: ProductFormulaMaterialVO[]]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const unitLabel = (unitCode?: string) => props.unitLabel(unitCode)
const MATERIAL_TYPE_ALL = '__ALL__'
const keyword = ref('')
const materialTypeFilter = ref(MATERIAL_TYPE_ALL)
const selectedRows = ref<ProductFormulaMaterialVO[]>([])
const materialTypeOptions = computed(() => Array.from(new Set(props.materials
  .map((row) => row.materialTypeNameCn || row.materialTypeCode || '')
  .filter(Boolean))))
const visibleMaterials = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  return props.materials.filter((row) => {
    if (materialTypeFilter.value !== MATERIAL_TYPE_ALL && (row.materialTypeNameCn || row.materialTypeCode) !== materialTypeFilter.value) return false
    if (!value) return true
    return [row.materialCode, row.materialNameCn, row.specModelText, row.materialTypeNameCn]
      .some((field) => String(field || '').toLowerCase().includes(value))
  })
})

function openBatchUsage() {
  const row = selectedRows.value[0]
  if (row) {
    emit('open-usage', row)
  }
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
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.setup-section__title {
  flex: 0 0 auto;
  min-width: 112px;
  max-width: 180px;
}

.setup-section__toolbar h3 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #111827;
}

.setup-section__toolbar p {
  overflow: hidden;
  margin: 0;
  color: #6b7280;
  font-size: 13px;
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
  flex: 1;
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
  width: min(320px, 100%);
}

.material-type-filter {
  width: 160px;
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
  height: 56px;
}

.setup-table :deep(.el-input-number) {
  width: 100%;
}

.setup-table :deep(.el-input-number .el-input__wrapper),
.setup-table :deep(.el-input .el-input__wrapper),
.setup-table :deep(.el-select .el-select__wrapper) {
  min-height: 32px;
  border-radius: 6px;
}

.spec-model-text {
  display: -webkit-box;
  max-height: 48px;
  overflow: hidden;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.35;
  white-space: normal;
  word-break: break-all;
  -webkit-box-orient: vertical;
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
