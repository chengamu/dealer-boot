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
      <div class="expression-editor__formula-layout price-expression-layout">
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
          <section v-if="target === 'condition'" class="price-expression-side__card">
            <h4>{{ t('productCenter.pricing.sizeCondition') }}</h4>
            <el-select v-model="dimensionBuilder.variable" filterable>
              <el-option v-for="item in dimensionVariables" :key="item.name" :label="item.label" :value="item.name" />
            </el-select>
            <div class="price-expression-side__row">
              <el-select v-model="dimensionBuilder.operator">
                <el-option label=">" value=">" />
                <el-option label=">=" value=">=" />
                <el-option label="<" value="<" />
                <el-option label="<=" value="<=" />
                <el-option label="=" value="==" />
                <el-option label="!=" value="!=" />
              </el-select>
              <el-input-number v-model="dimensionBuilder.value" :controls="false" :precision="2" />
            </div>
            <div class="price-expression-side__row">
              <el-select v-model="dimensionBuilder.joiner">
                <el-option :label="t('productCenter.pricing.and')" value="&&" />
                <el-option :label="t('productCenter.pricing.or')" value="||" />
              </el-select>
              <el-button type="primary" plain @click="appendDimensionCondition">
                {{ t('productCenter.formulaSetup.insertCondition') }}
              </el-button>
            </div>
          </section>

          <section v-if="target === 'condition'" class="price-expression-side__card">
            <h4>{{ t('productCenter.formulaSetup.optionCondition') }}</h4>
            <el-select v-model="optionBuilder.optionCode" filterable :placeholder="t('productCenter.formulaSetup.optionName')">
              <el-option v-for="item in options" :key="item.optionCode" :label="optionLabel(item)" :value="item.optionCode || ''" />
            </el-select>
            <div class="price-expression-side__row">
              <el-select v-model="optionBuilder.operator">
                <el-option label="=" value="==" />
                <el-option label="!=" value="!=" />
              </el-select>
              <el-select v-model="optionBuilder.valueCode" filterable :placeholder="t('productCenter.formulaSetup.conditionValue')">
                <el-option v-for="item in optionValueRows" :key="item.valueCode" :label="valueLabel(item)" :value="item.valueCode || ''" />
              </el-select>
            </div>
            <div class="price-expression-side__row">
              <el-select v-model="optionBuilder.joiner">
                <el-option :label="t('productCenter.pricing.and')" value="&&" />
                <el-option :label="t('productCenter.pricing.or')" value="||" />
              </el-select>
              <el-button type="primary" plain @click="appendOptionCondition">
                {{ t('productCenter.formulaSetup.insertCondition') }}
              </el-button>
            </div>
          </section>

          <section class="price-expression-side__card">
            <h4>{{ t('productCenter.pricing.priceFormulaTipsTitle') }}</h4>
            <p>{{ t('productCenter.pricing.priceFormulaTips') }}</p>
            <p v-if="target === 'formula'">{{ t('productCenter.pricing.unitPriceFormulaHint') }}</p>
            <p v-else>{{ t('productCenter.pricing.conditionFormulaHint') }}</p>
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
import { computed, reactive } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaExpressionComposer from '../../product-formula/components/FormulaExpressionComposer.vue'
import FormulaExpressionOperatorPanel from '../../product-formula/components/FormulaExpressionOperatorPanel.vue'
import FormulaExpressionValidationPanel from '../../product-formula/components/FormulaExpressionValidationPanel.vue'
import { priceConditionVariables, priceFormulaVariables, priceOperators, validatePriceCondition, validatePriceFormula } from '../utils/pricingExpression'
import type { PriceExpressionVariable } from '../utils/pricingExpression'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

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
const optionBuilder = reactive({ optionCode: '', valueCode: '', operator: '==', joiner: '&&' })
const dimensionBuilder = reactive({ variable: 'width', operator: '>', value: 0, joiner: '&&' })

const editorText = computed({ get: () => props.text, set: (value: string) => emit('update:text', value) })
const title = computed(() => props.target === 'formula' ? t('productCenter.pricing.priceFormulaSelector') : t('productCenter.formulaSetup.conditionExpressionEditor'))
const placeholder = computed(() => props.target === 'formula' ? t('productCenter.pricing.formulaPlaceholder') : t('productCenter.pricing.conditionPlaceholder'))
const variableButtons = computed(() => props.target === 'formula'
  ? priceFormulaVariables.filter((item) => item.name !== 'unitPrice')
  : priceConditionVariables)
const dimensionVariables = computed(() => priceConditionVariables.filter((item) => ['width', 'drop', 'areaM2', 'areaSqft'].includes(item.name)))
const operators = computed(() => props.target === 'formula'
  ? ['+', '-', '*', '/', '(', ')', ',', { label: 'MAX', insert: 'MAX(' }, { label: 'MIN', insert: 'MIN(' }, { label: 'ROUND', insert: 'ROUND(' }, { label: 'CEIL', insert: 'CEIL(' }, { label: 'FLOOR', insert: 'FLOOR(' }]
  : priceOperators())
const result = computed(() => props.target === 'formula' ? validatePriceFormula(props.text) : validatePriceCondition(props.text))
const resultText = computed(() => {
  if (!result.value.valid) return t(result.value.message === 'empty' ? 'productCenter.formulaSetup.expressionEmpty' : 'productCenter.formulaSetup.formulaInvalid')
  if (props.target === 'condition') return t('productCenter.formulaSetup.expressionValid')
  return `${t('productCenter.formulaSetup.sampleResult')}：${formatNumber(result.value.sampleValue)}`
})
const optionValueRows = computed(() => (props.optionValues || []).filter((item) => item.optionCode === optionBuilder.optionCode))

function appendText(value: string) {
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function appendDimensionCondition() {
  if (!dimensionBuilder.variable || dimensionBuilder.value === null || dimensionBuilder.value === undefined) return
  const clause = `${dimensionBuilder.variable} ${dimensionBuilder.operator} ${dimensionBuilder.value}`
  editorText.value = `${editorText.value.trim()}${editorText.value.trim() ? ` ${dimensionBuilder.joiner} ` : ''}${clause}`.trim()
}

function appendOptionCondition() {
  if (!optionBuilder.optionCode || !optionBuilder.valueCode) return
  const clause = `option_${optionBuilder.optionCode} ${optionBuilder.operator} "${optionBuilder.valueCode}"`
  editorText.value = `${editorText.value.trim()}${editorText.value.trim() ? ` ${optionBuilder.joiner} ` : ''}${clause}`.trim()
}

function optionLabel(item: ProductFormulaOptionVO) {
  return item.optionNameCn || item.optionNameEn || item.optionCode || '-'
}

function valueLabel(item: ProductFormulaOptionValueVO) {
  return item.valueNameCn || item.valueNameEn || item.valueCode || '-'
}

function variableLabel(item: PriceExpressionVariable) {
  return item.labelKey ? t(item.labelKey) : item.label
}

function formatNumber(value: unknown) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(2) : '-'
}
</script>

<style>
@import '../../product-formula/components/FormulaExpressionEditorDialog.css';
</style>

<style scoped>
.price-expression-layout {
  grid-template-columns: minmax(0, 70fr) minmax(320px, 30fr);
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

.price-expression-side__row {
  display: grid;
  grid-template-columns: minmax(90px, 0.35fr) minmax(0, 0.65fr);
  gap: 8px;
}
</style>
