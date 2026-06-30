<template>
  <AdminDialog
    v-model="dialogVisible"
    :title="t('productCenter.formulaSetup.batchAddMaterial')"
    width="1320px"
    variant="picker"
    class="material-picker-dialog"
  >
    <div class="admin-dialog__toolbar material-picker-search-card">
      <el-form :model="query" :inline="true" class="material-picker-filter">
        <el-form-item :label="t('productCenter.formulaSetup.attributeGroup')">
          <el-select v-model="query.attributeGroupCode" clearable filterable @change="handleGroupChange">
            <el-option v-for="group in groupOptions" :key="String(group.value)" :label="group.label" :value="group.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.formulaSetup.materialType')">
          <el-select v-model="query.materialTypeCode" clearable filterable>
            <el-option v-for="type in pickerMaterialTypeOptions" :key="String(type.value)" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.formulaSetup.materialCode')">
          <el-input v-model="query.materialCode" clearable />
        </el-form-item>
        <el-form-item :label="t('productCenter.formulaSetup.materialName')">
          <el-input v-model="query.materialNameCn" clearable />
        </el-form-item>
        <el-form-item class="material-picker-filter__actions">
          <el-button type="primary" icon="Search">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="admin-dialog__table material-picker-grid-card">
      <el-table :data="filteredMaterialRows" border height="100%" class="material-picker-table" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="112" />
        <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="132" />
        <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="132" />
        <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="270" />
        <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="240" />
        <el-table-column prop="unitCode" :label="t('productCenter.formulaSetup.unit')" width="110">
          <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
        </el-table-column>
        <el-table-column prop="manufacturerName" :label="t('productCenter.formulaSetup.manufacturerName')" min-width="160" />
      </el-table>
    </div>

    <template #footer>
      <AdminDialogFooter :status="`${t('productCenter.formulaSetup.selectedMaterialCount')}：${selectedMaterials.length}`">
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="selectedMaterials.length === 0" @click="confirmSelection">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts" name="FormulaMaterialPickerDialog">
import { computed, reactive, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import { localizedRecordLabel } from '@/utils/productLabels'
import type { ProductMaterialTypeVO, ProductMaterialVO, ProductOption, ProductRecord } from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  materialRows: ProductMaterialVO[]
  groupOptions: ProductOption[]
  materialTypeRows: ProductMaterialTypeVO[]
  unitLabel: (unitCode?: string) => string
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'confirm', value: ProductMaterialVO[]): void
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const query = reactive({
  attributeGroupCode: '',
  materialTypeCode: '',
  materialCode: '',
  materialNameCn: ''
})
const selectedMaterials = ref<ProductMaterialVO[]>([])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const pickerMaterialTypeOptions = computed<ProductOption[]>(() => props.materialTypeRows
  .filter((row) => !query.attributeGroupCode || row.attributeGroupCode === query.attributeGroupCode)
  .map((row) => ({ value: row.materialTypeCode || '', label: labelOf(row, 'materialTypeCode', 'materialTypeNameCn', 'materialTypeNameEn') }))
  .filter((item) => item.value))

const filteredMaterialRows = computed(() => props.materialRows.filter((row) => {
  if (row.status && row.status !== PRODUCT_STATUS_ENABLED) return false
  if (query.attributeGroupCode && row.attributeGroupCode !== query.attributeGroupCode) return false
  if (query.materialTypeCode && row.materialTypeCode !== query.materialTypeCode) return false
  if (!containsText(row.materialCode, query.materialCode)) return false
  return containsText(row.materialNameCn, query.materialNameCn)
}))

watch(() => props.modelValue, (visible) => {
  if (!visible) selectedMaterials.value = []
})

function handleSelectionChange(rows: ProductMaterialVO[]) {
  selectedMaterials.value = rows
}

function handleGroupChange() {
  query.materialTypeCode = ''
}

function resetQuery() {
  query.attributeGroupCode = ''
  query.materialTypeCode = ''
  query.materialCode = ''
  query.materialNameCn = ''
}

function confirmSelection() {
  emit('confirm', selectedMaterials.value)
  selectedMaterials.value = []
  dialogVisible.value = false
}

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  return localizedRecordLabel(row, localeStore.language, codeKey, cnKey, enKey)
}

function containsText(value: unknown, keyword: string) {
  if (!keyword) return true
  return String(value || '').toLowerCase().includes(keyword.trim().toLowerCase())
}
</script>

<style scoped>
.material-picker-search-card {
  flex: 0 0 auto;
  margin-bottom: 12px;
}

.material-picker-filter {
  display: grid;
  grid-template-columns: minmax(190px, 1fr) minmax(200px, 1fr) minmax(180px, 0.9fr) minmax(200px, 1fr) auto;
  gap: 8px 12px;
  align-items: center;
}

.material-picker-filter :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 0;
}

.material-picker-filter :deep(.el-form-item__label) {
  padding-right: 10px;
  color: #1f2937;
  font-weight: 600;
}

.material-picker-filter :deep(.el-select),
.material-picker-filter :deep(.el-input) {
  width: 100%;
}

.material-picker-filter :deep(.el-input__wrapper),
.material-picker-filter :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 8px;
}

.material-picker-filter__actions {
  justify-self: end;
}

.material-picker-filter__actions :deep(.el-form-item__content) {
  gap: 8px;
  flex-wrap: nowrap;
}

.material-picker-grid-card {
  flex: 1;
  min-height: 0;
}

.material-picker-table {
  height: 100%;
  border-radius: 0;
}

.material-picker-table :deep(.el-table__header th) {
  height: 44px;
  background: #f7faff !important;
  color: #1f2937;
  font-weight: 700;
}

.material-picker-table :deep(.el-table__row td) {
  min-height: 48px;
  padding: 7px 0;
}

.material-picker-table :deep(.cell) {
  white-space: normal;
  word-break: break-word;
  line-height: 1.35;
}

.material-picker-table :deep(.el-table__body-wrapper),
.material-picker-table :deep(.el-scrollbar),
.material-picker-table :deep(.el-scrollbar__wrap) {
  min-height: 0;
}

@media (max-width: 1080px) {
  .material-picker-filter {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }

  .material-picker-filter__actions {
    justify-self: start;
  }
}
</style>
