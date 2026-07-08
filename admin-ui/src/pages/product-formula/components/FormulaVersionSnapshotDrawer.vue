<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="76%"
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-empty v-if="!version" :description="t('productCenter.formulaWorkbench.snapshotEmpty')" />
    <div v-else class="snapshot-drawer">
      <section class="snapshot-summary">
        <div>
          <span>{{ t('productCenter.formula.code') }}</span>
          <strong>{{ formula.formulaCode || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formula.name') }}</span>
          <strong>{{ formula.formulaName || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formula.category') }}</span>
          <strong>{{ formula.categoryNameCn || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formula.productType') }}</span>
          <strong>{{ formula.productTypeNameCn || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formula.materialLineCount') }}</span>
          <strong>{{ materials.length }}</strong>
        </div>
      </section>

      <section class="snapshot-section">
        <div class="snapshot-section__title">{{ t('productCenter.formulaReview.materialDetail') }}</div>
        <el-table :data="materials" border height="420">
          <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
          <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="100" />
          <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="120" />
          <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="130" />
          <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="220" show-overflow-tooltip />
          <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="unitCode" :label="t('productCenter.formulaSetup.unit')" width="80" align="center" />
          <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="90" align="right">
            <template #default="{ row }">{{ formatNumber(row.lossRate) }}</template>
          </el-table-column>
          <el-table-column prop="productionRemark" :label="t('productCenter.formulaSetup.productionRemark')" min-width="160" show-overflow-tooltip />
        </el-table>
      </section>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaSetupVO,
  ProductFormulaVersionVO,
  ProductFormulaVO
} from '@/api/product-capability/types'
import { parseFormulaReviewJson } from '../utils/formulaReviewDisplay'

const props = defineProps<{
  modelValue: boolean
  version?: ProductFormulaVersionVO
  t: (key: string) => string
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const title = computed(() => `${props.t('productCenter.formulaWorkbench.versionSnapshot')} ${props.version?.versionLabel || ''}`)
const formula = computed(() => parseFormulaReviewJson<ProductFormulaVO>(props.version?.formulaSnapshotJson))
const setup = computed(() => parseFormulaReviewJson<ProductFormulaSetupVO>(props.version?.setupSnapshotJson))
const materials = computed(() => (setup.value.materials || []) as ProductFormulaMaterialVO[])

function formatNumber(value?: number) {
  if (value == null || Number.isNaN(Number(value))) return '-'
  return Number(value).toFixed(2)
}
</script>

<style scoped>
.snapshot-drawer {
  display: grid;
  gap: 14px;
}

.snapshot-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.snapshot-summary div {
  display: grid;
  gap: 5px;
  padding: 12px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fbfcff;
}

.snapshot-summary span {
  color: #667085;
  font-size: 12px;
}

.snapshot-summary strong {
  overflow: hidden;
  color: #1d2129;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.snapshot-section {
  display: grid;
  gap: 8px;
}

.snapshot-section__title {
  color: #1d2129;
  font-size: 15px;
  font-weight: 600;
}
</style>
