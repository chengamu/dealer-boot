<template>
  <div class="quote-line-editor" v-loading="loading">
    <div v-if="groups.length" class="quote-line-editor__groups">
      <QuoteOptionGroup
        v-for="group in groups"
        :key="group.root.optionCode"
        :root="group.root"
        :options="group.options"
        :setup="setup || {}"
        :selected-values="row.selectedOptionValues"
        :language="language"
        :readonly="readonly"
      />
    </div>
    <el-empty v-else :description="t('customer.quote.option.empty')" :image-size="48" />
    <div v-if="readonly && row.remark" class="quote-line-editor__remark">
      <span>{{ t('customer.quote.remark') }}</span>
      <p>{{ row.remark }}</p>
    </div>
    <div v-else-if="!readonly" class="quote-line-editor__footer">
      <el-input v-model="row.remark" :disabled="readonly" :placeholder="t('customer.quote.remark')" maxlength="200" show-word-limit />
      <el-button type="primary" plain :disabled="readonly || !row.saleProductId" :loading="calculating" @click="emit('calculate')">
        {{ t('customer.quote.action.calculateLine') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CustomerQuoteCatalogSetup, CustomerQuoteItem, QuoteLanguage } from '@/api/customer/quote'
import QuoteOptionGroup from './QuoteOptionGroup.vue'
import { activeQuoteOptions, applyQuoteDefaults, groupQuoteOptions } from './quoteOptionGroups'

const props = defineProps<{
  row: CustomerQuoteItem
  setup?: CustomerQuoteCatalogSetup
  language: QuoteLanguage
  loading?: boolean
  calculating?: boolean
  readonly?: boolean
}>()
const emit = defineEmits<{ calculate: []; dirty: [] }>()
const { t } = useI18n()
const activeOptions = computed(() => props.setup ? activeQuoteOptions(props.setup, props.row.selectedOptionValues) : [])
const groups = computed(() => groupQuoteOptions(activeOptions.value))

watch(() => props.setup, (setup) => {
  if (setup) applyQuoteDefaults(setup, props.row.selectedOptionValues)
}, { immediate: true })
watch(() => props.row.selectedOptionValues, () => {
  if (!props.setup) return
  const visibleCodes = new Set(activeOptions.value.map((option) => option.optionCode).filter(Boolean))
  Object.keys(props.row.selectedOptionValues).forEach((code) => {
    if (!visibleCodes.has(code)) delete props.row.selectedOptionValues[code]
  })
  applyQuoteDefaults(props.setup, props.row.selectedOptionValues)
  emit('dirty')
}, { deep: true })
</script>

<style scoped>
.quote-line-editor { padding: 0 14px 14px; background: #fbfcfe; }
.quote-line-editor__groups { overflow: hidden; border: 1px solid #e3e9f1; border-radius: 6px; background: #fff; }
.quote-line-editor__remark {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid #e3e9f1;
  border-radius: 8px;
  background: #fff;
}
.quote-line-editor__remark span { color: #667085; font-size: 12px; font-weight: 600; }
.quote-line-editor__remark p { margin: 0; color: #1d2939; line-height: 20px; }
.quote-line-editor__footer { display: grid; grid-template-columns: minmax(320px, 1fr) auto; gap: 12px; align-items: center; margin-top: 12px; }
.quote-line-editor :deep(.el-empty) { padding: 16px 0; }
</style>
