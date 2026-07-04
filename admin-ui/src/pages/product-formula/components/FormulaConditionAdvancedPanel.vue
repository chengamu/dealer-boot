<template>
  <FormulaExpressionComposer
    v-model="localText"
    :title="t('productCenter.formulaSetup.expressionContent')"
    :placeholder="t('productCenter.formulaSetup.conditionExpressionPlaceholder')"
    :clear-text="t('common.clear')"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import FormulaExpressionComposer from './FormulaExpressionComposer.vue'
import { buildAdvancedCondition, type ConditionBuildResult } from './formulaConditionEditor'
import type { ProductFormulaMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  t: (key: string) => string
  text: string
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  materials: ProductFormulaMaterialVO[]
}>()

const emit = defineEmits<{
  change: [value: ConditionBuildResult]
  'update:text': [value: string]
}>()

const localText = ref(props.text || '')

watch(() => props.text, (value) => {
  localText.value = value || ''
})

watch(localText, (value) => {
  emit('update:text', value)
  emit('change', buildAdvancedCondition(value, props.options, props.optionValues, props.materials))
}, { immediate: true })
</script>
