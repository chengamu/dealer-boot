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
        <el-button v-if="editable" v-hasPermi="['product:pricing:edit']" type="primary" icon="EditPen" @click="$emit('batch')">
          {{ t('productCenter.pricing.batchSetPrice') }}
        </el-button>
      </div>
    </div>
    <PriceFormulaEditor
      :model-value="formula"
      :label="t('productCenter.pricing.fabricFormula')"
      :rows="1"
      :disabled="!editable"
      @update:model-value="$emit('formula-change', $event)"
    />
    <el-table :data="rows" border class="price-rule-table" height="460">
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="128" />
      <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="260" show-overflow-tooltip />
      <el-table-column prop="unitCode" :label="t('productCenter.common.unitCode')" width="76" align="center" />
      <el-table-column prop="optionCombinationName" :label="t('productCenter.pricing.optionCombination')" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.pricing.unitPrice')" width="130" align="right">
        <template #default="{ row }">
          <span>{{ money(row.basePrice) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="areaFormula" :label="t('productCenter.pricing.fabricFormula')" min-width="320" show-overflow-tooltip />
      <el-table-column :label="t('common.operate')" width="112" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="editable" v-hasPermi="['product:pricing:edit']" link type="primary" @click="openRule(row)">
            {{ t('productCenter.pricing.priceSetting') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <FabricPriceRuleDialog
      v-model="dialogOpen"
      :row="activeRow"
      :formula="formula"
      @save="$emit('row-change', $event)"
    />
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { FabricPriceRule } from '@/api/product-pricing/types'
import { money } from '../utils/pricingDisplay'
import PriceFormulaEditor from './PriceFormulaEditor.vue'
import FabricPriceRuleDialog from './FabricPriceRuleDialog.vue'

defineProps<{
  rows: FabricPriceRule[]
  formula: string
  editable?: boolean
}>()

defineEmits<{
  generate: [overwrite: boolean]
  batch: []
  'row-change': [row: FabricPriceRule]
  'formula-change': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const dialogOpen = ref(false)
const activeRow = ref<FabricPriceRule>()

function openRule(row: FabricPriceRule) {
  activeRow.value = row
  dialogOpen.value = true
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

.price-rule-table {
  margin-top: 12px;
}

.price-rule-table :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #344054;
}
</style>
