<template>
  <div v-if="groups.length" class="quick-order-option-groups">
    <QuoteOptionGroup
      v-for="group in groups"
      :key="group.root.optionCode"
      :root="group.root"
      :options="group.options"
      :setup="setup || {}"
      :selected-values="selectedValues"
      :language="language"
      :readonly="readonly"
    />
  </div>
  <el-empty v-else :description="t('dealer.quickOrder.option.empty')" :image-size="48" />
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CustomerQuoteCatalogSetup, QuoteLanguage } from '@/api/customer/quote'
import QuoteOptionGroup from '@/pages/customer/quote/QuoteOptionGroup.vue'
import { activeQuoteOptions, applyQuoteDefaults, groupQuoteOptions } from '@/pages/customer/quote/quoteOptionGroups'

const props = defineProps<{
  setup?: CustomerQuoteCatalogSetup
  selectedValues: Record<string, string>
  language: QuoteLanguage
  readonly?: boolean
}>()

const emit = defineEmits<{ change: [] }>()
const { t } = useI18n()
const activeOptions = computed(() => props.setup ? activeQuoteOptions(props.setup, props.selectedValues) : [])
const groups = computed(() => groupQuoteOptions(activeOptions.value))

watch(() => props.setup, (setup) => {
  if (setup) applyQuoteDefaults(setup, props.selectedValues)
}, { immediate: true })

watch(() => props.selectedValues, () => {
  if (!props.setup) return
  const visibleCodes = new Set(activeOptions.value.map((option) => option.optionCode).filter(Boolean))
  Object.keys(props.selectedValues).forEach((code) => {
    if (!visibleCodes.has(code)) delete props.selectedValues[code]
  })
  applyQuoteDefaults(props.setup, props.selectedValues)
  emit('change')
}, { deep: true })
</script>

<style scoped>
.quick-order-option-groups { overflow: hidden; border: 1px solid #e6ecf3; border-radius: 8px; background: #fff; }
.quick-order-option-groups :deep(.quote-option-group__fields) { grid-template-columns: repeat(2, minmax(200px, 1fr)); }
</style>
