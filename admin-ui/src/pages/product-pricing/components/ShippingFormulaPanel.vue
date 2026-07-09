<template>
  <section class="shipping-panel">
    <div class="shipping-panel__header">
      <div>
        <h3>{{ t('productCenter.pricing.extraFeeRules') }}</h3>
        <p>{{ t('productCenter.pricing.extraFeeRulesHint') }}</p>
      </div>
      <div v-if="editable" class="shipping-panel__actions">
        <el-select v-model="selectedTemplateId" filterable :placeholder="t('productCenter.shippingTemplate.selectPlaceholder')">
          <el-option v-for="item in templates" :key="String(item.shippingTemplateId || '')" :label="templateLabel(item)" :value="String(item.shippingTemplateId || '')" />
        </el-select>
        <el-button v-hasPermi="['product:pricing:edit']" type="primary" plain icon="Download" :disabled="!selectedTemplateId" @click="$emit('import', selectedTemplateId)">
          {{ t('productCenter.pricing.importShippingTemplate') }}
        </el-button>
      </div>
    </div>
    <el-table :data="rows" border height="260" :empty-text="t('productCenter.common.empty')">
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column :label="t('productCenter.shippingTemplate.scenario')" width="120">
        <template #default="{ row }">{{ feeName(row.feeCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.shippingTemplate.areaRange')" width="180">
        <template #default="{ row }">{{ areaRange(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.shippingTemplate.template')" min-width="180">
        <template #default="{ row }">{{ row.shippingTemplateName || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.shippingTemplate.formula')" min-width="420">
        <template #default="{ row }">
          <pre class="shipping-panel__formula">{{ row.formulaText || '-' }}</pre>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ExtraFeeRule, ShippingTemplateVO } from '@/api/product-pricing/types'

const props = defineProps<{
  rows: ExtraFeeRule[]
  templates: ShippingTemplateVO[]
  editable?: boolean
}>()

defineEmits<{ import: [shippingTemplateId: string] }>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const selectedTemplateId = ref('')

watch(() => props.templates, (templates) => {
  const selectedExists = templates.some(item => String(item.shippingTemplateId || '') === selectedTemplateId.value)
  if (selectedExists) return
  selectedTemplateId.value = String(templates.find(item => item.defaultFlag)?.shippingTemplateId || '')
}, { immediate: true })

function templateLabel(item: ShippingTemplateVO) {
  return [item.templateCode, item.templateName].filter(Boolean).join(' ')
}

function feeName(code?: string) {
  return code === 'MOTORIZED' ? t('productCenter.shippingTemplate.motorized') : t('productCenter.shippingTemplate.manual')
}

function areaRange(row: ExtraFeeRule) {
  const min = row.minAreaSqft ?? 0
  return row.maxAreaSqft === undefined || row.maxAreaSqft === null
    ? `≥ ${formatNumber(min)} ft²`
    : `${formatNumber(min)} - ${formatNumber(row.maxAreaSqft)} ft²`
}

function formatNumber(value?: number) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(2) : '-'
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.shipping-panel h3,
.shipping-panel p {
  margin: 0;
}

.shipping-panel p {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.shipping-panel__actions {
  display: flex;
  gap: 8px;
}

.shipping-panel__actions .el-select {
  width: 260px;
}

.shipping-panel__formula {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}
</style>
