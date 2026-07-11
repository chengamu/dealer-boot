<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="title"
    size="96vw"
    append-to-body
    variant="wide"
    class="fabric-rule-drawer"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="fabric-rule-editor">
      <section class="fabric-rule-editor__summary">
        <el-tag size="small" effect="plain">{{ material?.attributeGroupNameCn || '-' }}</el-tag>
        <span>{{ material?.materialCode || '-' }}</span>
        <strong>{{ material?.materialNameCn || '-' }}</strong>
        <el-tag size="small" effect="plain">{{ material?.unitCode || '-' }}</el-tag>
      </section>

      <div class="fabric-rule-editor__toolbar">
        <div>
          <h3>{{ t('productCenter.pricing.priceRuleSetting') }}</h3>
          <p>{{ t('productCenter.pricing.priceRuleSettingHint') }}</p>
        </div>
        <div class="fabric-rule-editor__actions">
          <el-button v-if="editable" type="primary" plain icon="Plus" @click="addRule">
            {{ t('productCenter.pricing.addPriceRule') }}
          </el-button>
          <el-button v-if="editable" plain icon="CopyDocument" :disabled="!selectedRule" @click="copyRule">
            {{ t('common.copy') }}
          </el-button>
          <el-button v-if="editable" type="danger" plain icon="Delete" :disabled="!selectedRule" @click="removeRule">
            {{ t('common.delete') }}
          </el-button>
        </div>
      </div>

      <el-table
        :data="localRows"
        border
        height="560"
        class="fabric-rule-table"
        highlight-current-row
        @current-change="selectedRule = $event"
      >
        <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
        <el-table-column :label="t('productCenter.pricing.condition')" min-width="380">
          <template #default="{ row }">
            <div class="rule-cell rule-cell--condition">
              <el-select v-model="row.conditionType" :disabled="!editable" @change="onConditionTypeChange(row)">
                <el-option value="DEFAULT" :label="t('productCenter.pricing.defaultRule')" />
                <el-option value="EXPRESSION" :label="t('productCenter.pricing.conditionRule')" />
              </el-select>
              <template v-if="row.conditionType === 'DEFAULT'">
                <el-tag effect="plain">{{ t('productCenter.pricing.defaultRule') }}</el-tag>
              </template>
              <template v-else>
                <el-input
                  v-model="row.conditionExpression"
                  :disabled="!editable"
                  :placeholder="t('productCenter.pricing.conditionPlaceholder')"
                  @change="syncConditionText(row)"
                />
                <el-button plain :disabled="!editable" @click="openExpression(row, 'condition')">
                  {{ t('productCenter.formulaSetup.conditionExpressionEditor') }}
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.pricing.unitPrice')" width="124" align="right" class-name="fabric-rule-table__price-column">
          <template #default="{ row }">
            <BusinessNumberInput
              v-model="row.unitPrice"
              :disabled="!editable"
              :min="0"
              mode="UNIT_PRICE"
              :max-fraction-digits="4"
              @change="syncDefaultPriceFormula(row)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.pricing.materialFormula')" min-width="560">
          <template #default="{ row }">
            <div class="rule-cell">
              <el-input v-model="row.priceFormula" :disabled="!editable" :placeholder="t('productCenter.pricing.formulaPlaceholder')" />
              <el-button plain :disabled="!editable" @click="openExpression(row, 'formula')">
                {{ t('productCenter.formulaSetup.formulaSelectorShort') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.remark')" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.remark" :disabled="!editable" />
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
      <el-button v-if="editable" type="primary" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { MaterialPriceRule, PriceMaterialVO } from '@/api/product-pricing/types'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { displayMaterialPriceFormula, materialPriceFormulaForUnitPrice } from '../utils/pricingDisplay'
import PriceExpressionEditorDialog from './PriceExpressionEditorDialog.vue'

type ExpressionTarget = 'formula' | 'condition'

const props = defineProps<{
  modelValue: boolean
  material?: PriceMaterialVO
  rules: MaterialPriceRule[]
  editable?: boolean
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [priceMaterialId: string, rows: MaterialPriceRule[]]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const localRows = ref<MaterialPriceRule[]>([])
const selectedRule = ref<MaterialPriceRule | null>(null)
const expressionOpen = ref(false)
const expressionTarget = ref<ExpressionTarget>('formula')
const expressionText = ref('')
const editingRule = ref<MaterialPriceRule | null>(null)
const title = computed(() => `${t('productCenter.pricing.priceRuleSetting')} / ${props.material?.materialNameCn || '-'}`)

watch(() => props.modelValue, (open) => {
  if (!open) return
  selectedRule.value = null
  localRows.value = (props.rules || []).map(row => ({ ...row, priceFormula: displayMaterialPriceFormula(row.priceFormula, row.unitPrice) }))
  if (!localRows.value.length) addRule(true)
})

function addRule(defaultRule = false) {
  const isDefault = defaultRule || !localRows.value.some((row) => row.defaultRuleFlag || row.conditionType === 'DEFAULT')
  localRows.value.push({
    priceMaterialId: props.material?.priceMaterialId,
    conditionType: isDefault ? 'DEFAULT' : 'EXPRESSION',
    conditionJson: '',
    conditionExpression: isDefault ? 'DEFAULT' : '',
    conditionText: isDefault ? t('productCenter.pricing.defaultRule') : '',
    conditionKey: isDefault ? 'DEFAULT' : '',
    priceMode: 'FORMULA',
    unitPrice: 0,
    priceFormula: materialPriceFormulaForUnitPrice(0, props.material?.attributeGroupCode),
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
    row.priceFormula = materialPriceFormulaForUnitPrice(row.unitPrice, props.material?.attributeGroupCode)
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
  const id = props.material?.priceMaterialId
  if (!id) return
  emit('save', id, localRows.value.map((row, index) => ({ ...row, sortOrder: index })))
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped src="./MaterialPriceRuleDrawer.css"></style>
