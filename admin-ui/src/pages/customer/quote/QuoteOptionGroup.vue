<template>
  <section class="quote-option-group">
    <div class="quote-option-group__side">
      <strong>{{ title }}</strong>
      <span>{{ options.length }} {{ t('customer.quote.option.values') }}</span>
    </div>
    <div class="quote-option-group__fields">
      <QuoteOptionField
        v-for="option in options"
        :key="option.optionCode"
        v-model="selectedValues[option.optionCode || '']"
        :option="option"
        :values="valuesForOption(setup, option.optionCode)"
        :language="language"
        :readonly="readonly"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CustomerQuoteCatalogSetup, QuoteLanguage } from '@/api/customer/quote'
import type { ProductFormulaOptionVO } from '@/api/product-capability/types'
import QuoteOptionField from './QuoteOptionField.vue'
import { valuesForOption } from './quoteOptionGroups'

const props = defineProps<{
  root: ProductFormulaOptionVO
  options: ProductFormulaOptionVO[]
  setup: CustomerQuoteCatalogSetup
  selectedValues: Record<string, string>
  language: QuoteLanguage
  readonly?: boolean
}>()
const { t } = useI18n()
const title = computed(() => props.language === 'EN_US'
  ? props.root.optionNameEn || `[${props.root.optionCode || '-'}]`
  : props.root.optionNameCn || props.root.optionCode || '-')
</script>

<style scoped>
.quote-option-group { display: grid; grid-template-columns: 156px minmax(0, 1fr); border-top: 1px solid #e6ebf2; }
.quote-option-group:first-child { border-top: 0; }
.quote-option-group__side { padding: 14px 16px; background: #f7faff; border-right: 1px solid #e6ebf2; }
.quote-option-group__side strong { display: block; color: #1d2939; font-size: 14px; }
.quote-option-group__side span { display: block; margin-top: 6px; color: #667085; font-size: 12px; }
.quote-option-group__fields { display: grid; grid-template-columns: repeat(3, minmax(180px, 1fr)); gap: 12px 18px; padding: 12px 16px 14px; }
@media (max-width: 1280px) { .quote-option-group__fields { grid-template-columns: repeat(2, minmax(180px, 1fr)); } }
</style>
