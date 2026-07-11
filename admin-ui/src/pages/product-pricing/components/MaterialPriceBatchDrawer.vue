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
          <span>{{ t('productCenter.pricing.selectedMaterialCount') }}</span>
          <strong>{{ materials.length }}</strong>
        </div>
        <div class="batch-price-editor__fabric-list">
          <div v-for="item in materials" :key="item.priceMaterialId" class="batch-price-editor__fabric-item">
            <el-tag size="small" effect="plain">{{ item.attributeGroupNameCn || '-' }}</el-tag>
            <span>{{ item.materialCode || '-' }}</span>
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
        <el-table-column :label="t('productCenter.pricing.materialFormula')" min-width="560">
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
      <el-button type="primary" :disabled="!materials.length" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { MaterialPriceRule, PriceMaterialVO } from '@/api/product-pricing/types'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { materialPriceFormulaForUnitPrice } from '../utils/pricingDisplay'
import PriceExpressionEditorDialog from './PriceExpressionEditorDialog.vue'

type ExpressionTarget = 'formula' | 'condition'

const props = defineProps<{
  modelValue: boolean
  materials: PriceMaterialVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: { priceMaterialIds: string[], rules: MaterialPriceRule[] }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const localRows = ref<MaterialPriceRule[]>([])
const selectedRule = ref<MaterialPriceRule | null>(null)
const expressionOpen = ref(false)
const expressionTarget = ref<ExpressionTarget>('formula')
const expressionText = ref('')
const editingRule = ref<MaterialPriceRule | null>(null)
const batchGroupCode = computed(() => {
  const groups = new Set(props.materials.map(item => item.attributeGroupCode).filter(Boolean))
  return groups.size === 1 ? props.materials[0]?.attributeGroupCode : undefined
})

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
    priceFormula: materialPriceFormulaForUnitPrice(0, batchGroupCode.value),
    defaultRuleFlag: isDefault,
    status: 'ENABLED',
    sortOrder: localRows.value.length
  })
}

function copyRule() {
  if (!selectedRule.value) return
  localRows.value.push({
    ...selectedRule.value,
    materialRuleId: undefined,
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

function onConditionTypeChange(row: MaterialPriceRule) {
  const isDefault = row.conditionType === 'DEFAULT'
  row.defaultRuleFlag = isDefault
  row.conditionJson = ''
  row.conditionExpression = isDefault ? 'DEFAULT' : ''
  row.conditionText = isDefault ? t('productCenter.pricing.defaultRule') : ''
  row.conditionKey = isDefault ? 'DEFAULT' : ''
}

function syncConditionText(row: MaterialPriceRule) {
  row.conditionJson = ''
  row.conditionText = row.conditionExpression
  row.conditionKey = row.conditionExpression
}

function syncDefaultPriceFormula(row: MaterialPriceRule) {
  if (!row.priceFormula || /^\s*\d+(\.\d+)?\s*\*/.test(row.priceFormula)) {
    row.priceFormula = materialPriceFormulaForUnitPrice(row.unitPrice, batchGroupCode.value)
  }
}

function openExpression(row: MaterialPriceRule, target: ExpressionTarget) {
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
    priceMaterialIds: props.materials.map((item) => String(item.priceMaterialId || '')).filter(Boolean),
    rules: localRows.value.map((row, index) => ({ ...row, sortOrder: index }))
  })
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped src="./MaterialPriceRuleDrawer.css"></style>
<style scoped src="./MaterialPriceBatchDrawer.css"></style>
