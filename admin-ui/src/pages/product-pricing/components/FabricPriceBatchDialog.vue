<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="t('productCenter.pricing.batchSetPrice')"
    size="96vw"
    append-to-body
    variant="wide"
    class="fabric-price-batch-drawer"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="batch-price-editor">
      <section class="batch-price-editor__summary">
        <div class="batch-price-editor__summary-head">
          <span>{{ t('productCenter.pricing.selectedFabricCount') }}</span>
          <strong>{{ fabrics.length }}</strong>
        </div>
        <div class="batch-price-editor__fabric-list">
          <div v-for="item in fabrics" :key="item.priceFabricId" class="batch-price-editor__fabric-item">
            <el-tag size="small" effect="plain">{{ item.materialCode || '-' }}</el-tag>
            <strong>{{ item.materialNameCn || '-' }}</strong>
            <span>{{ item.unitCode || '-' }}</span>
          </div>
        </div>
      </section>

      <div class="fabric-rule-editor__toolbar">
        <div>
          <h3>{{ t('productCenter.pricing.priceRuleSetting') }}</h3>
          <p>{{ t('productCenter.pricing.batchRuleSettingHint') }}</p>
        </div>
        <div class="fabric-rule-editor__actions">
          <el-button type="primary" plain icon="Plus" @click="addRule">
            {{ t('productCenter.pricing.addPriceRule') }}
          </el-button>
          <el-button plain icon="CopyDocument" :disabled="!selectedRule" @click="copyRule">
            {{ t('common.copy') }}
          </el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!selectedRule" @click="removeRule">
            {{ t('common.delete') }}
          </el-button>
        </div>
      </div>

      <el-table
        :data="localRows"
        border
        height="520"
        class="fabric-rule-table"
        highlight-current-row
        @current-change="selectedRule = $event"
      >
        <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
        <el-table-column :label="t('productCenter.pricing.condition')" min-width="380">
          <template #default="{ row }">
            <div class="rule-cell rule-cell--condition">
              <el-select v-model="row.conditionType" @change="onConditionTypeChange(row)">
                <el-option value="DEFAULT" :label="t('productCenter.pricing.defaultRule')" />
                <el-option value="EXPRESSION" :label="t('productCenter.pricing.conditionRule')" />
              </el-select>
              <template v-if="row.conditionType === 'DEFAULT'">
                <el-tag effect="plain">{{ t('productCenter.pricing.defaultRule') }}</el-tag>
              </template>
              <template v-else>
                <el-input
                  v-model="row.conditionExpression"
                  :placeholder="t('productCenter.pricing.conditionPlaceholder')"
                  @change="syncConditionText(row)"
                />
                <el-button plain @click="openExpression(row, 'condition')">
                  {{ t('productCenter.formulaSetup.conditionExpressionEditor') }}
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.pricing.unitPrice')" width="124" align="right" class-name="fabric-rule-table__price-column">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :controls="false" @change="syncDefaultPriceFormula(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.pricing.fabricFormula')" min-width="560">
          <template #default="{ row }">
            <div class="rule-cell">
              <el-input v-model="row.priceFormula" :placeholder="t('productCenter.pricing.formulaPlaceholder')" />
              <el-button plain @click="openExpression(row, 'formula')">
                {{ t('productCenter.formulaSetup.formulaSelectorShort') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.remark')" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.remark" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <PriceExpressionEditorDialog
      v-model="expressionOpen"
      v-model:text="expressionText"
      :target="expressionTarget"
      :options="options"
      :option-values="optionValues"
      @confirm="confirmExpression"
    />

    <template #footer>
      <el-button @click="close">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :disabled="!fabrics.length" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { FabricPriceRule, PriceFabricVO } from '@/api/product-pricing/types'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { fabricPriceFormulaForUnitPrice } from '../utils/pricingDisplay'
import PriceExpressionEditorDialog from './PriceExpressionEditorDialog.vue'

type ExpressionTarget = 'formula' | 'condition'

const props = defineProps<{
  modelValue: boolean
  fabrics: PriceFabricVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: { priceFabricIds: number[], rows: FabricPriceRule[] }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const localRows = ref<FabricPriceRule[]>([])
const selectedRule = ref<FabricPriceRule | null>(null)
const expressionOpen = ref(false)
const expressionTarget = ref<ExpressionTarget>('formula')
const expressionText = ref('')
const editingRule = ref<FabricPriceRule | null>(null)

watch(() => props.modelValue, (open) => {
  if (!open) return
  selectedRule.value = null
  localRows.value = []
  addRule(true)
})

function addRule(defaultRule = false) {
  const isDefault = defaultRule || !localRows.value.some((row) => row.defaultRuleFlag || row.conditionType === 'DEFAULT')
  localRows.value.push({
    conditionType: isDefault ? 'DEFAULT' : 'EXPRESSION',
    conditionJson: '',
    conditionExpression: isDefault ? 'DEFAULT' : '',
    conditionText: isDefault ? t('productCenter.pricing.defaultRule') : '',
    conditionKey: isDefault ? 'DEFAULT' : '',
    priceMode: 'FORMULA',
    unitPrice: 0,
    priceFormula: fabricPriceFormulaForUnitPrice(0),
    defaultRuleFlag: isDefault,
    status: 'ENABLED',
    sortOrder: localRows.value.length
  })
}

function copyRule() {
  if (!selectedRule.value) return
  localRows.value.push({
    ...selectedRule.value,
    fabricRuleId: undefined,
    defaultRuleFlag: false,
    conditionType: 'EXPRESSION',
    conditionJson: '',
    conditionExpression: '',
    conditionText: '',
    conditionKey: '',
    sortOrder: localRows.value.length
  })
}

function removeRule() {
  if (!selectedRule.value) return
  localRows.value = localRows.value.filter((row) => row !== selectedRule.value)
  selectedRule.value = null
}

function onConditionTypeChange(row: FabricPriceRule) {
  const isDefault = row.conditionType === 'DEFAULT'
  row.defaultRuleFlag = isDefault
  row.conditionJson = ''
  row.conditionExpression = isDefault ? 'DEFAULT' : ''
  row.conditionText = isDefault ? t('productCenter.pricing.defaultRule') : ''
  row.conditionKey = isDefault ? 'DEFAULT' : ''
}

function syncConditionText(row: FabricPriceRule) {
  row.conditionJson = ''
  row.conditionText = row.conditionExpression
  row.conditionKey = row.conditionExpression
}

function syncDefaultPriceFormula(row: FabricPriceRule) {
  if (!row.priceFormula || /^\s*\d+(\.\d+)?\s*\*\s*MAX\(/.test(row.priceFormula)) {
    row.priceFormula = fabricPriceFormulaForUnitPrice(row.unitPrice)
  }
}

function openExpression(row: FabricPriceRule, target: ExpressionTarget) {
  editingRule.value = row
  expressionTarget.value = target
  expressionText.value = target === 'formula' ? row.priceFormula || '' : row.conditionExpression || ''
  expressionOpen.value = true
}

function confirmExpression() {
  if (!editingRule.value) return
  if (expressionTarget.value === 'formula') {
    editingRule.value.priceFormula = expressionText.value
  } else {
    editingRule.value.conditionType = 'EXPRESSION'
    editingRule.value.defaultRuleFlag = false
    editingRule.value.conditionJson = ''
    editingRule.value.conditionExpression = expressionText.value
    syncConditionText(editingRule.value)
  }
  expressionOpen.value = false
}

function save() {
  emit('save', {
    priceFabricIds: props.fabrics.map((item) => Number(item.priceFabricId)).filter(Boolean),
    rows: localRows.value.map((row, index) => ({ ...row, sortOrder: index }))
  })
  close()
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped src="./FabricPriceRuleDrawer.css"></style>
<style scoped src="./FabricPriceBatchDialog.css"></style>
