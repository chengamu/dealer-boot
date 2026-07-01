<template>
  <AdminDialog v-model="importOpen" :title="t('productCenter.formulaSetup.importOptionValues')" width="980px" variant="picker" class="admin-selector-dialog formula-option-dialog">
    <div class="admin-dialog__toolbar formula-option-dialog__toolbar">
      <div>
        <strong>{{ selectedOption?.optionNameCn || selectedOption?.optionCode || '-' }}</strong>
        <span>{{ sourceText(selectedOption || {}) }}</span>
      </div>
      <el-input :model-value="valueImportKeyword" :placeholder="t('productCenter.formulaSetup.materialSearchPlaceholder')" clearable @update:model-value="$emit('update:valueImportKeyword', String($event))" />
    </div>
    <div class="admin-dialog__table admin-selector-dialog__table">
      <el-table :data="importableSourceMaterials" border height="100%" @selection-change="$emit('update:importMaterialRows', $event)">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="120" show-overflow-tooltip />
        <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="140" show-overflow-tooltip />
        <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="140" show-overflow-tooltip />
        <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="240" show-overflow-tooltip />
        <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="unitCode" :label="t('productCenter.formulaSetup.unit')" width="90" />
      </el-table>
    </div>
    <template #footer>
      <AdminDialogFooter :status="`${t('productCenter.formulaSetup.selectedMaterialCount')}：${importMaterialRows.length}`">
        <el-button @click="importOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!importMaterialRows.length" @click="$emit('append-imported-material-values')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>

  <AdminDialog v-model="materialOpen" :title="valueMaterialDialogTitle" width="760px" class="formula-option-dialog">
    <div class="admin-dialog__toolbar formula-option-dialog__toolbar">
      <div>
        <strong>{{ selectedValueName }}</strong>
        <span>{{ t('productCenter.formulaSetup.valueMaterialHint') }}</span>
      </div>
    </div>
    <el-select
      v-model="materialCodes"
      multiple
      filterable
      clearable
      collapse-tags
      collapse-tags-tooltip
      class="value-material-select"
      :placeholder="t('productCenter.formulaSetup.pickFromMaterialPool')"
    >
      <el-option v-for="material in materials" :key="String(material.materialCode)" :label="materialLabel(material)" :value="material.materialCode" />
    </el-select>
    <template #footer>
      <AdminDialogFooter :status="t('common.selectedCount', { count: valueMaterialCodes.length })">
        <el-button @click="materialOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="$emit('save-value-materials')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ProductFormulaMaterialVO, ProductFormulaOptionVO } from '@/api/product-capability/types'

const props = defineProps<{
  valueImportOpen: boolean
  valueImportKeyword: string
  importMaterialRows: ProductFormulaMaterialVO[]
  importableSourceMaterials: ProductFormulaMaterialVO[]
  valueMaterialOpen: boolean
  valueMaterialCodes: string[]
  selectedOption?: ProductFormulaOptionVO
  selectedValueName: string
  valueMaterialDialogTitle: string
  materials: ProductFormulaMaterialVO[]
  materialLabel: (row: ProductFormulaMaterialVO) => string
  sourceText: (option: ProductFormulaOptionVO) => string
  t: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:valueImportOpen': [value: boolean]
  'update:valueImportKeyword': [value: string]
  'update:importMaterialRows': [rows: ProductFormulaMaterialVO[]]
  'update:valueMaterialOpen': [value: boolean]
  'update:valueMaterialCodes': [value: string[]]
  'append-imported-material-values': []
  'save-value-materials': []
}>()

const importOpen = computed({
  get: () => props.valueImportOpen,
  set: (value) => emit('update:valueImportOpen', value)
})
const materialOpen = computed({
  get: () => props.valueMaterialOpen,
  set: (value) => emit('update:valueMaterialOpen', value)
})
const materialCodes = computed({
  get: () => props.valueMaterialCodes,
  set: (value) => emit('update:valueMaterialCodes', value)
})
</script>

<style scoped>
.formula-option-dialog__toolbar {
  justify-content: space-between;
  color: #6b7280;
}

.formula-option-dialog__toolbar > div {
  display: grid;
  gap: 2px;
}

.formula-option-dialog__toolbar strong {
  color: #111827;
}

.formula-option-dialog__toolbar .el-input {
  width: 320px;
}

.value-material-select {
  width: 100%;
}
</style>
