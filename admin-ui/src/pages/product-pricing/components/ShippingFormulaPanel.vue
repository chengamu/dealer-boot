<template>
  <section class="shipping-panel">
    <div class="shipping-panel__header">
      <div>
        <h3>{{ t('productCenter.pricing.extraFeeRules') }}</h3>
        <p>{{ t('productCenter.pricing.extraFeeRulesHint') }}</p>
      </div>
    </div>
    <div class="shipping-panel__grid">
      <article v-for="item in normalizedRows" :key="item.feeCode" class="shipping-card">
        <div class="shipping-card__title">
          <span>{{ item.feeName }}</span>
          <el-tag size="small" type="info">{{ triggerText(item.feeCode) }}</el-tag>
        </div>
        <PriceFormulaEditor
          :model-value="item.formulaText || ''"
          :label="t('productCenter.pricing.shippingFormula')"
          :rows="2"
          :disabled="!editable"
          @update:model-value="updateFormula(item.feeCode || '', $event)"
        />
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ExtraFeeRule } from '@/api/product-pricing/types'
import PriceFormulaEditor from './PriceFormulaEditor.vue'

const props = defineProps<{
  rows: ExtraFeeRule[]
  editable?: boolean
}>()

const emit = defineEmits<{
  change: [payload: ExtraFeeRule[]]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const defaults: ExtraFeeRule[] = [
  { feeCode: 'MANUAL', feeName: t('productCenter.pricing.manualShipping'), feeCategory: 'SHIPPING', triggerCondition: 'motorized = false', feeMode: 'FORMULA', status: 'ENABLED', sortOrder: 0 },
  { feeCode: 'MOTORIZED', feeName: t('productCenter.pricing.motorizedShipping'), feeCategory: 'SHIPPING', triggerCondition: 'motorized = true', feeMode: 'FORMULA', status: 'ENABLED', sortOrder: 1 }
]

const normalizedRows = computed(() => defaults.map(item => ({
  ...item,
  ...(props.rows.find(row => row.feeCode === item.feeCode) || {})
})))

function updateFormula(code: string, formulaText: string) {
  if (!props.editable) return
  emit('change', normalizedRows.value.map(row => row.feeCode === code ? { ...row, formulaText } : row))
}

function triggerText(code?: string) {
  return code === 'MOTORIZED' ? t('productCenter.pricing.motorizedTrigger') : t('productCenter.pricing.manualTrigger')
}
</script>

<style scoped>
.shipping-panel {
  min-width: 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.shipping-panel__header {
  margin-bottom: 12px;
}

.shipping-panel h3 {
  margin: 0;
  color: #1d2129;
  font-size: 16px;
}

.shipping-panel p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
}

.shipping-panel__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.shipping-card {
  display: grid;
  gap: 10px;
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e3ebf7;
  border-radius: 8px;
}

.shipping-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #1f2937;
  font-weight: 700;
}
</style>
