<template>
  <section class="node-card option-values-card">
    <div class="node-card__header">
      <div>
        <h4>{{ t('productCenter.formulaSetup.optionValuesTitle') }}</h4>
        <p>{{ selectedOption?.optionNameCn || selectedOption?.optionCode }}</p>
      </div>
      <div class="node-card__actions">
        <template v-if="selectedOption?.sourceType === 'MATERIAL_POOL'">
          <el-button type="primary" plain :icon="Plus" @click="$emit('importValues')">
            {{ t('productCenter.formulaSetup.importOptionValues') }}
          </el-button>
        </template>
        <el-button v-else type="primary" plain :icon="Plus" :disabled="!selectedOptionCode" @click="$emit('addValue')">
          {{ t('productCenter.formulaSetup.addOptionValue') }}
        </el-button>
      </div>
    </div>

    <el-table
      :data="optionValues"
      border
      class="setup-table option-value-table"
      :row-class-name="optionValueRowClass"
      @row-click="$emit('selectValue', $event)"
    >
      <el-table-column :label="t('productCenter.formulaSetup.valueNameCn')" width="220">
        <template #default="{ row }">
          <el-input v-model="row.valueNameCn" :disabled="selectedOption?.sourceType === 'MATERIAL_POOL'" />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.valueNameEn')" width="220">
        <template #default="{ row }">
          <el-input v-model="row.valueNameEn" />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.linkedMaterial')" min-width="320">
        <template #default="{ row }">
          <div class="linked-materials">
            <el-tag v-for="material in materialsForValue(row)" :key="String(material.materialCode)" size="small">
              {{ material.materialNameCn || material.materialCode }}
            </el-tag>
            <span v-if="materialsForValue(row).length === 0" class="muted">{{ t('productCenter.formulaSetup.noLinkedMaterial') }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.defaultFlag')" width="84" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.defaultFlag" @change="$emit('setDefault', row)" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('productCenter.formulaSetup.manageLinkedMaterial'), icon: 'Setting', stopPropagation: true, onClick: () => $emit('manageMaterial', row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', stopPropagation: true, onClick: () => $emit('removeValue', row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type {
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO
} from '@/api/product-capability/types'

const props = defineProps<{
  selectedOption?: ProductFormulaOptionVO
  selectedOptionCode: string
  selectedValueCode: string
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
}>()

defineEmits<{
  importValues: []
  addValue: []
  selectValue: [row: ProductFormulaOptionValueVO]
  setDefault: [row: ProductFormulaOptionValueVO]
  manageMaterial: [row: ProductFormulaOptionValueVO]
  removeValue: [row: ProductFormulaOptionValueVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function materialsForValue(row?: ProductFormulaOptionValueVO) {
  return props.optionMaterials.filter((item) => item.optionCode === row?.optionCode && item.valueCode === row?.valueCode)
}

function optionValueRowClass({ row }: { row: ProductFormulaOptionValueVO }) {
  return row.valueCode && row.valueCode === props.selectedValueCode ? 'option-value-row--active' : ''
}
</script>

<style scoped>
.node-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.node-card__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}

.node-card__header h4 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.node-card__header p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.node-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.linked-materials {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #9aa4b2;
}

.option-value-table :deep(.option-value-row--active > td) {
  background: #eef6ff !important;
}

.setup-table {
  width: 100%;
}
</style>
