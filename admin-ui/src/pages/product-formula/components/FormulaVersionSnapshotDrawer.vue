<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="82%"
    destroy-on-close
    class="formula-snapshot-drawer"
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
        <FormulaReviewMaterialSummary
          :materials="materials"
          :usage-rules="usageRules"
          :price-snapshot-rows="priceSnapshotRows"
          :unit-rows="unitRows || []"
          :t="t"
        />
      </section>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaSetupVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVersionVO,
  ProductFormulaVO,
  ProductUnitVO
} from '@/api/product-capability/types'
import { parseFormulaReviewJson } from '../utils/formulaReviewDisplay'
import FormulaReviewMaterialSummary from './FormulaReviewMaterialSummary.vue'
import type { FormulaReviewPriceSnapshotRow } from '../utils/formulaReviewMaterialSummary'

const props = defineProps<{
  modelValue: boolean
  version?: ProductFormulaVersionVO
  unitRows?: ProductUnitVO[]
  t: (key: string) => string
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const title = computed(() => `${props.t('productCenter.formulaWorkbench.versionSnapshot')} ${props.version?.versionLabel || ''}`)
const formula = computed(() => parseFormulaReviewJson<ProductFormulaVO>(props.version?.formulaSnapshotJson))
const setup = computed(() => parseFormulaReviewJson<ProductFormulaSetupVO>(props.version?.setupSnapshotJson))
const materials = computed(() => (setup.value.materials || []) as ProductFormulaMaterialVO[])
const usageRules = computed(() => (setup.value.usageRules || []) as ProductFormulaUsageRuleVO[])
const priceSnapshotRows = computed(() => (setup.value.priceSnapshot || []) as FormulaReviewPriceSnapshotRow[])
</script>

<style scoped>
:deep(.formula-snapshot-drawer .el-drawer__body) {
  display: flex;
  min-height: 0;
  padding: 16px 20px 18px;
}

.snapshot-drawer {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
  gap: 12px;
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
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
  gap: 8px;
}

.snapshot-section__title {
  color: #1d2129;
  font-size: 15px;
  font-weight: 600;
}

.snapshot-section :deep(.review-material-summary) {
  min-height: 0;
}
</style>
