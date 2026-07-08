<template>
  <section class="workbench-steps">
    <button
      v-for="step in steps"
      :key="step.key"
      type="button"
      class="workbench-step"
      :class="{ 'is-disabled': step.disabled }"
      :disabled="step.disabled"
      @click="$emit('open', step.key)"
    >
      <span class="workbench-step__icon">
        <el-icon><component :is="step.icon" /></el-icon>
      </span>
      <span class="workbench-step__body">
        <strong>{{ step.title }}</strong>
        <span>{{ step.desc }}</span>
      </span>
      <el-tag size="small" effect="plain" :type="step.tagType">{{ step.status }}</el-tag>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ProductFormulaVO, ProductFormulaVersionVO } from '@/api/product-capability/types'
import { FORMULA_VALIDATION_STATUS, formulaValidationStatusText } from '@/constants/productStatus'

export type FormulaWorkbenchStepKey = 'materials' | 'options' | 'simulation' | 'review'

const props = defineProps<{
  formula: ProductFormulaVO
  pendingVersion?: ProductFormulaVersionVO
  t: (key: string) => string
}>()

defineEmits<{
  open: [key: FormulaWorkbenchStepKey]
}>()

function validationTag(status?: string) {
  if (status === FORMULA_VALIDATION_STATUS.PASS) return 'success'
  if (status === FORMULA_VALIDATION_STATUS.FAIL) return 'danger'
  return 'warning'
}

const steps = computed(() => [
  {
    key: 'materials' as const,
    icon: 'Box',
    title: props.t('productCenter.formula.actions.materials'),
    desc: props.t('productCenter.formulaWorkbench.materialsDesc'),
    status: formulaValidationStatusText(props.formula.materialValidationStatus, props.t),
    tagType: validationTag(props.formula.materialValidationStatus)
  },
  {
    key: 'options' as const,
    icon: 'Operation',
    title: props.t('productCenter.formula.actions.options'),
    desc: props.t('productCenter.formulaWorkbench.optionsDesc'),
    status: formulaValidationStatusText(props.formula.optionValidationStatus, props.t),
    tagType: validationTag(props.formula.optionValidationStatus)
  },
  {
    key: 'simulation' as const,
    icon: 'DataAnalysis',
    title: props.t('productCenter.formula.actions.simulation'),
    desc: props.t('productCenter.formulaWorkbench.simulationDesc'),
    status: formulaValidationStatusText(props.formula.simulationValidationStatus, props.t),
    tagType: validationTag(props.formula.simulationValidationStatus)
  },
  {
    key: 'review' as const,
    icon: 'Finished',
    title: props.t('productCenter.formulaReview.title'),
    desc: props.t('productCenter.formulaWorkbench.reviewDesc'),
    status: props.pendingVersion?.versionLabel || props.t('productCenter.formulaWorkbench.noPendingReview'),
    tagType: props.pendingVersion ? 'warning' : 'info',
    disabled: !props.pendingVersion
  }
])
</script>

<style scoped>
.workbench-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.workbench-step {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  min-height: 88px;
  padding: 14px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fff;
  color: #1f2937;
  text-align: left;
  cursor: pointer;
}

.workbench-step:hover {
  border-color: #b8d4ff;
  box-shadow: 0 8px 24px rgb(15 23 42 / 6%);
}

.workbench-step.is-disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.workbench-step__icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: #eef5ff;
  color: #1677ff;
  font-size: 20px;
}

.workbench-step__body {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.workbench-step__body strong {
  font-size: 15px;
}

.workbench-step__body span {
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
