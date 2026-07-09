<template>
  <section class="price-condition-builder__card">
    <h4>{{ t('productCenter.pricing.sizeCondition') }}</h4>
    <el-select v-model="dimensionBuilder.variable" filterable>
      <el-option v-for="item in dimensionVariables" :key="item.name" :label="variableLabel(item)" :value="item.name" />
    </el-select>
    <div class="price-condition-builder__row">
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
    <div class="price-condition-builder__row">
      <el-select v-model="dimensionBuilder.joiner">
        <el-option :label="t('productCenter.pricing.and')" value="&&" />
        <el-option :label="t('productCenter.pricing.or')" value="||" />
      </el-select>
      <el-button type="primary" plain @click="appendDimensionCondition">
        {{ t('productCenter.formulaSetup.insertCondition') }}
      </el-button>
    </div>
  </section>

  <section class="price-condition-builder__card">
    <h4>{{ t('productCenter.formulaSetup.optionCondition') }}</h4>
    <el-select v-model="optionBuilder.optionRef" filterable :placeholder="t('productCenter.formulaSetup.optionName')">
      <el-option v-for="item in options" :key="optionRef(item)" :label="optionLabel(item)" :value="optionRef(item)" />
    </el-select>
    <div class="price-condition-builder__row">
      <el-select v-model="optionBuilder.operator">
        <el-option label="=" value="==" />
        <el-option label="!=" value="!=" />
      </el-select>
      <el-select v-model="optionBuilder.valueRef" filterable :placeholder="t('productCenter.formulaSetup.conditionValue')">
        <el-option v-for="item in optionValueRows" :key="valueRef(item)" :label="valueLabel(item)" :value="valueRef(item)" />
      </el-select>
    </div>
    <div class="price-condition-builder__row">
      <el-select v-model="optionBuilder.joiner">
        <el-option :label="t('productCenter.pricing.and')" value="&&" />
        <el-option :label="t('productCenter.pricing.or')" value="||" />
      </el-select>
      <el-button type="primary" plain @click="appendOptionCondition">
        {{ t('productCenter.formulaSetup.insertCondition') }}
      </el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { optionValueLiteral, optionVariableName } from '../../product-formula/components/formulaExpressionDisplay'
import { priceConditionVariables } from '../utils/pricingExpression'
import type { PriceExpressionVariable } from '../utils/pricingExpression'

const props = defineProps<{
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  append: [payload: { clause: string, joiner: string }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const optionBuilder = reactive({ optionRef: '', valueRef: '', operator: '==', joiner: '&&' })
const dimensionBuilder = reactive({ variable: 'width', operator: '>', value: 0, joiner: '&&' })

const dimensionVariables = computed(() =>
  priceConditionVariables.filter((item) => ['width', 'drop', 'widthCm', 'dropCm', 'areaM2', 'areaSqft'].includes(item.name))
)
const selectedOption = computed(() =>
  (props.options || []).find((item) => optionRef(item) === optionBuilder.optionRef || item.optionCode === optionBuilder.optionRef)
)
const optionValueRows = computed(() => {
  const option = selectedOption.value
  return (props.optionValues || []).filter((item) => {
    if (!option) return false
    if (item.optionRefKey && option.optionRefKey) return item.optionRefKey === option.optionRefKey
    return item.optionCode === option.optionCode
  })
})

function appendDimensionCondition() {
  if (!dimensionBuilder.variable || dimensionBuilder.value === null || dimensionBuilder.value === undefined) return
  emit('append', {
    clause: `${dimensionBuilder.variable} ${dimensionBuilder.operator} ${dimensionBuilder.value}`,
    joiner: dimensionBuilder.joiner
  })
}

function appendOptionCondition() {
  const option = selectedOption.value
  const value = optionValueRows.value.find((item) => valueRef(item) === optionBuilder.valueRef || item.valueCode === optionBuilder.valueRef)
  if (!option || !value) return
  emit('append', {
    clause: `${optionVariableName(option)} ${optionBuilder.operator} ${optionValueLiteral(value)}`,
    joiner: optionBuilder.joiner
  })
}

function optionLabel(item: ProductFormulaOptionVO) {
  return item.optionNameCn || item.optionNameEn || item.optionCode || '-'
}

function valueLabel(item: ProductFormulaOptionValueVO) {
  return item.valueNameCn || item.valueNameEn || item.valueCode || '-'
}

function optionRef(item: ProductFormulaOptionVO) {
  return item.optionRefKey || item.optionCode || ''
}

function valueRef(item: ProductFormulaOptionValueVO) {
  return item.valueRefKey || item.valueCode || ''
}

function variableLabel(item: PriceExpressionVariable) {
  return item.labelKey ? t(item.labelKey) : item.label
}
</script>

<style scoped>
.price-condition-builder__card {
  display: grid;
  gap: 10px;
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.price-condition-builder__card h4 {
  margin: 0;
  color: #1f2937;
  font-size: 14px;
}

.price-condition-builder__row {
  display: grid;
  grid-template-columns: minmax(90px, 0.35fr) minmax(0, 0.65fr);
  gap: 8px;
}
</style>
