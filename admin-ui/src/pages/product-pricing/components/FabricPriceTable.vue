<template>
  <section class="price-section">
    <div class="price-section__header">
      <div>
        <h3>{{ t('productCenter.pricing.fabricRules') }}</h3>
        <p>{{ t('productCenter.pricing.fabricRulesHint') }}</p>
      </div>
      <div class="price-section__actions">
        <el-button v-if="editable" v-hasPermi="['product:pricing:edit']" icon="Refresh" @click="$emit('generate', false)">
          {{ t('productCenter.pricing.generateCurrentVersionPrices') }}
        </el-button>
        <el-button v-if="editable" v-hasPermi="['product:pricing:edit']" icon="CopyDocument" @click="$emit('generate', true)">
          {{ t('productCenter.pricing.overwriteGeneratePrices') }}
        </el-button>
        <el-button v-if="editable" v-hasPermi="['product:pricing:edit']" type="primary" icon="EditPen" :disabled="!selectedRows.length" @click="$emit('batch', selectedRows)">
          {{ t('productCenter.pricing.batchSetPrice') }}
        </el-button>
      </div>
    </div>
    <el-table :data="rows" border class="price-rule-table" height="520" row-key="priceFabricId" @selection-change="selectedRows = $event">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="132" />
      <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="360" show-overflow-tooltip />
      <el-table-column prop="unitCode" :label="t('productCenter.common.unitCode')" width="86" align="center" />
      <el-table-column :label="t('productCenter.pricing.priceSummary')" min-width="420">
        <template #default="{ row }">
          <div class="price-summary">
            <div v-for="item in summaryRows(row)" :key="item.key" class="price-summary__line">
              <div class="price-summary__meta">
                <el-tag size="small" effect="plain" :type="item.defaultRule ? 'primary' : 'success'">{{ item.label }}</el-tag>
                <span>{{ item.unitPriceText }}</span>
              </div>
              <div class="price-summary__formula">{{ item.formulaText }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.pricing.ruleCount')" width="112" align="center">
        <template #default="{ row }">{{ row.ruleCount || 0 }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.pricing.defaultRule')" width="112" align="center">
        <template #default="{ row }">
          <el-tag :type="row.defaultRuleFlag ? 'success' : 'danger'" effect="plain">
            {{ row.defaultRuleFlag ? t('common.yes') : t('common.no') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="128" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['product:pricing:edit']" link type="primary" @click="$emit('open-rules', row)">
            {{ t('productCenter.pricing.priceRuleSetting') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { FabricPriceRule, PriceFabricVO } from '@/api/product-pricing/types'
import { displayFabricPriceFormula, displayPriceExpression } from '../utils/pricingDisplay'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

defineEmits<{
  generate: [overwrite: boolean]
  batch: [rows: PriceFabricVO[]]
  'open-rules': [row: PriceFabricVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const props = defineProps<{
  rows: PriceFabricVO[]
  rules: FabricPriceRule[]
  editable?: boolean
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()
const selectedRows = ref<PriceFabricVO[]>([])

watch(() => props.rows, () => {
  selectedRows.value = []
})

function summaryRows(row: PriceFabricVO) {
  const rowRules = props.rules.filter(rule => rule.priceFabricId === row.priceFabricId)
  if (!rowRules.length) {
    return [{
      key: 'missing',
      label: t('productCenter.pricing.unset'),
      unitPriceText: '-',
      formulaText: '-',
      defaultRule: false
    }]
  }
  return rowRules.map((rule, index) => ({
    key: String(rule.fabricRuleId || `${rule.conditionKey || 'rule'}-${index}`),
    label: rule.defaultRuleFlag ? t('productCenter.pricing.defaultRule') : displayPriceExpression(rule.conditionText || rule.conditionExpression, t, props.options, props.optionValues),
    unitPriceText: `${t('productCenter.pricing.unitPrice')} ${formatMoney(rule.unitPrice)}`,
    formulaText: displayPriceExpression(displayFabricPriceFormula(rule.priceFormula, rule.unitPrice), t, props.options, props.optionValues),
    defaultRule: Boolean(rule.defaultRuleFlag)
  }))
}

function formatMoney(value?: number) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '-'
}
</script>

<style scoped>
.price-section {
  min-width: 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.price-section__header,
.price-section__actions {
  display: flex;
  align-items: flex-start;
}

.price-section__header {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.price-section h3 {
  margin: 0;
  color: #1d2129;
  font-size: 16px;
}

.price-section p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
}

.price-section__actions {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.price-rule-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #344054;
}

.price-summary {
  display: grid;
  gap: 8px;
  padding: 4px 0;
}

.price-summary__line {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.price-summary__meta {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.price-summary__meta span {
  color: #344054;
}

.price-summary__formula {
  color: #344054;
  line-height: 1.45;
  white-space: normal;
  word-break: break-word;
}
</style>
