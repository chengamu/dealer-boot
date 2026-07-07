<template>
  <section class="simulation-option-group">
    <div class="simulation-option-group__side">
      <strong>{{ title }}</strong>
      <span>{{ summary }}</span>
      <small v-if="root.remark">{{ root.remark }}</small>
    </div>
    <div class="simulation-option-group__fields">
      <FormulaSimulationOptionField
        v-for="option in options"
        :key="option.optionCode"
        v-model="selectedOptionValues[option.optionCode || '']"
        :option="option"
        :values="optionValuesOf(option.optionCode)"
        :option-materials="optionMaterials"
        :show-validation="showValidation"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import FormulaSimulationOptionField from './FormulaSimulationOptionField.vue'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  root: ProductFormulaOptionVO
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  selectedOptionValues: Record<string, string>
  showValidation: boolean
}>()

const { t } = useI18n()
const title = computed(() => props.root.optionNameCn || props.root.optionNameEn || props.root.optionCode || '-')
const summary = computed(() => {
  const childCount = Math.max(0, props.options.length - 1)
  return childCount > 0
    ? t('productCenter.formulaSimulation.childOptionsSummary', { count: childCount })
    : t('productCenter.formulaSimulation.baseOption')
})

function optionValuesOf(optionCode?: string) {
  return props.optionValues
    .filter((value) => value.status === PRODUCT_STATUS_ENABLED && value.optionCode === optionCode)
    .sort((left, right) => (left.sortOrder ?? 999999) - (right.sortOrder ?? 999999))
}
</script>
