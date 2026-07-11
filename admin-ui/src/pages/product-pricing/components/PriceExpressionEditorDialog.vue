<template>
  <AdminDialog
    :model-value="modelValue"
    :title="title"
    width="1280px"
    :class="['formula-expression-dialog', { 'formula-expression-dialog--condition': target === 'condition' }]"
    append-to-body
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="expression-editor">
      <div :class="['expression-editor__formula-layout', 'price-expression-layout', { 'price-expression-layout--condition': target === 'condition' }]">
        <div class="expression-editor__formula-main">
          <FormulaExpressionComposer
            v-model="editorText"
            :title="t('productCenter.formulaSetup.expressionContent')"
            :placeholder="placeholder"
            :clear-text="t('common.clear')"
          />
          <FormulaExpressionOperatorPanel
            :title="t('productCenter.formulaSetup.operator')"
            :operators="operators"
            @append="appendText"
          />
          <FormulaExpressionValidationPanel
            :title="t('productCenter.formulaSetup.validationResult')"
            :status-text="resultText"
            :normalized-title="t('productCenter.formulaSetup.normalizedExpression')"
            :expression="result.expression || ''"
            :valid="result.valid"
          />
          <section class="expression-editor__tools">
            <div class="expression-editor__tool-card">
              <h4>{{ t('productCenter.pricing.priceVariables') }}</h4>
              <el-button v-for="item in variableButtons" :key="item.name" plain @click="appendText(item.insert || item.name)">
                {{ variableLabel(item) }}
              </el-button>
            </div>
          </section>
        </div>

        <div class="price-expression-side">
          <PriceExpressionConditionBuilder
            v-if="target === 'condition'"
            :options="options"
            :option-values="optionValues"
            @append="appendCondition"
          />

          <section v-if="target === 'formula'" class="price-expression-side__card">
            <h4>{{ t('productCenter.pricing.priceFormulaTipsTitle') }}</h4>
            <p>{{ t('productCenter.pricing.priceFormulaTips') }}</p>
            <p>{{ t('productCenter.pricing.unitPriceFormulaHint') }}</p>
            <div class="price-expression-function-help">
              <h5>{{ t('productCenter.pricing.functionHelpTitle') }}</h5>
              <dl>
                <template v-for="item in functionHelpItems" :key="item.name">
                  <dt>{{ item.name }}</dt>
                  <dd>{{ t(item.helpKey) }}</dd>
                </template>
              </dl>
            </div>
          </section>
        </div>
      </div>
    </div>
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!result.valid" @click="$emit('confirm')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaExpressionComposer from '../../product-formula/components/FormulaExpressionComposer.vue'
import FormulaExpressionOperatorPanel from '../../product-formula/components/FormulaExpressionOperatorPanel.vue'
import FormulaExpressionValidationPanel from '../../product-formula/components/FormulaExpressionValidationPanel.vue'
import PriceExpressionConditionBuilder from './PriceExpressionConditionBuilder.vue'
import { priceConditionVariables, priceFormulaVariables, priceOperators, validatePriceCondition, validatePriceFormula } from '../utils/pricingExpression'
import type { PriceExpressionVariable } from '../utils/pricingExpression'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { formatMoney } from '@/utils/businessNumber'

type Target = 'formula' | 'condition'

const props = defineProps<{
  modelValue: boolean
  target: Target
  text: string
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  confirm: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const editorText = computed({ get: () => props.text, set: (value: string) => emit('update:text', value) })
const title = computed(() => props.target === 'formula' ? t('productCenter.pricing.priceFormulaSelector') : t('productCenter.formulaSetup.conditionExpressionEditor'))
const placeholder = computed(() => props.target === 'formula' ? t('productCenter.pricing.formulaPlaceholder') : t('productCenter.pricing.conditionPlaceholder'))
const variableButtons = computed(() => props.target === 'formula'
  ? priceFormulaVariables.filter((item) => item.name !== 'unitPrice')
  : priceConditionVariables)
const functionHelpItems = [
  { name: 'MAX', helpKey: 'productCenter.pricing.functionHelpMax' },
  { name: 'MIN', helpKey: 'productCenter.pricing.functionHelpMin' },
  { name: 'ROUND', helpKey: 'productCenter.pricing.functionHelpRound' },
  { name: 'CEIL', helpKey: 'productCenter.pricing.functionHelpCeil' },
  { name: 'FLOOR', helpKey: 'productCenter.pricing.functionHelpFloor' }
]
const operators = computed(() => props.target === 'formula'
  ? ['+', '-', '*', '/', '(', ')', ',', { label: 'MAX', insert: 'MAX(' }, { label: 'MIN', insert: 'MIN(' }, { label: 'ROUND', insert: 'ROUND(' }, { label: 'CEIL', insert: 'CEIL(' }, { label: 'FLOOR', insert: 'FLOOR(' }]
  : priceOperators())
const result = computed(() => props.target === 'formula' ? validatePriceFormula(props.text) : validatePriceCondition(props.text))
const resultText = computed(() => {
  if (!result.value.valid) return t(result.value.message === 'empty' ? 'productCenter.formulaSetup.expressionEmpty' : 'productCenter.formulaSetup.formulaInvalid')
  if (props.target === 'condition') return t('productCenter.formulaSetup.expressionValid')
  return `${t('productCenter.formulaSetup.sampleResult')}：${formatNumber(result.value.sampleValue)}`
})

function appendText(value: string) {
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function appendCondition(payload: { clause: string, joiner: string }) {
  const current = editorText.value.trim()
  editorText.value = `${current}${current ? ` ${payload.joiner} ` : ''}${payload.clause}`.trim()
}

function variableLabel(item: PriceExpressionVariable) {
  return item.labelKey ? t(item.labelKey) : item.label
}

function formatNumber(value: unknown) {
  return formatMoney(value as number | string | null)
}
</script>

<style>
@import '../../product-formula/components/FormulaExpressionEditorDialog.css';

.formula-expression-dialog--condition.admin-dialog.el-dialog {
  height: min(560px, calc(100vh - 48px));
}
</style>

<style scoped>
.price-expression-layout {
  grid-template-columns: minmax(0, 70fr) minmax(320px, 30fr);
}

.price-expression-layout--condition {
  grid-template-columns: minmax(0, 74fr) minmax(300px, 26fr);
}

.price-expression-side {
  display: grid;
  align-content: start;
  gap: 10px;
  min-width: 0;
}

.price-expression-side__card {
  display: grid;
  gap: 10px;
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.price-expression-side__card h4 {
  margin: 0;
  color: #1f2937;
  font-size: 14px;
}

.price-expression-side__card p {
  margin: 0;
  color: #667085;
  line-height: 1.5;
}

.price-expression-function-help {
  display: grid;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid #e5ecf6;
}

.price-expression-function-help h5 {
  margin: 0;
  color: #1f2937;
  font-size: 13px;
}

.price-expression-function-help dl {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  gap: 6px 8px;
  margin: 0;
  color: #667085;
  line-height: 1.45;
}

.price-expression-function-help dt {
  color: #1d4ed8;
  font-weight: 600;
}

.price-expression-function-help dd {
  margin: 0;
}
</style>
