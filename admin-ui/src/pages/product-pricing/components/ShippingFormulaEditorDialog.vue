<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.shippingTemplate.formulaEditor')"
    width="1080px"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="shipping-formula-editor">
      <section class="shipping-formula-editor__main">
        <div class="shipping-formula-editor__block">
          <header>
            <strong>{{ t('productCenter.shippingTemplate.formula') }}</strong>
            <el-button link type="primary" @click="editorText = ''">{{ t('common.clear') }}</el-button>
          </header>
          <el-input
            v-model="editorText"
            type="textarea"
            :rows="4"
            resize="none"
            :placeholder="t('productCenter.shippingTemplate.formulaPlaceholder')"
          />
        </div>

        <div class="shipping-formula-editor__block">
          <strong>{{ t('productCenter.formulaSetup.operator') }}</strong>
          <div class="shipping-formula-editor__chips">
            <el-button v-for="item in operators" :key="item" @click="append(item)">{{ item }}</el-button>
            <el-button v-for="item in functions" :key="item" plain @click="append(`${item}(`)">{{ item }}</el-button>
          </div>
        </div>

        <div class="shipping-formula-editor__block">
          <strong>{{ t('productCenter.pricing.priceVariables') }}</strong>
          <div class="shipping-formula-editor__chips">
            <el-button v-for="item in variables" :key="item.name" plain @click="append(item.insert || item.name)">
              {{ variableLabel(item) }}
            </el-button>
          </div>
        </div>

        <div class="shipping-formula-editor__result">
          <span>{{ t('productCenter.formulaSetup.validationResult') }}</span>
          <el-tag :type="result.valid ? 'success' : 'danger'">{{ resultText }}</el-tag>
        </div>
      </section>

      <aside class="shipping-formula-editor__side">
        <h4>{{ t('productCenter.shippingTemplate.formulaHelp') }}</h4>
        <p>{{ t('productCenter.shippingTemplate.formulaHelpIntro') }}</p>
        <dl>
          <template v-for="item in functionHelpItems" :key="item.name">
            <dt>{{ item.name }}</dt>
            <dd>{{ t(item.helpKey) }}</dd>
          </template>
        </dl>
        <div class="shipping-formula-editor__example">
          <strong>{{ t('productCenter.shippingTemplate.example') }}</strong>
          <code>18 + MAX(areaSqft - 20, 0) * 0.35</code>
          <span>{{ t('productCenter.shippingTemplate.exampleText') }}</span>
        </div>
      </aside>
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
import { shippingFormulaVariables, validateShippingFormula } from '../utils/pricingExpression'
import type { PriceExpressionVariable } from '../utils/pricingExpression'

const props = defineProps<{ modelValue: boolean, text: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean], 'update:text': [value: string], confirm: [] }>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const operators = ['+', '-', '*', '/', '(', ')', ',']
const functions = ['MAX', 'MIN', 'ROUND', 'CEIL', 'FLOOR']
const variables = shippingFormulaVariables
const functionHelpItems = [
  { name: 'MAX', helpKey: 'productCenter.pricing.functionHelpMax' },
  { name: 'MIN', helpKey: 'productCenter.pricing.functionHelpMin' },
  { name: 'ROUND', helpKey: 'productCenter.pricing.functionHelpRound' },
  { name: 'CEIL', helpKey: 'productCenter.pricing.functionHelpCeil' },
  { name: 'FLOOR', helpKey: 'productCenter.pricing.functionHelpFloor' }
]
const editorText = computed({ get: () => props.text, set: (value: string) => emit('update:text', value) })
const result = computed(() => validateShippingFormula(props.text))
const resultText = computed(() => {
  if (!result.value.valid) return t(result.value.message === 'empty' ? 'productCenter.formulaSetup.expressionEmpty' : 'productCenter.formulaSetup.formulaInvalid')
  return `${t('productCenter.formulaSetup.sampleResult')}：${formatNumber(result.value.sampleValue)}`
})

function append(value: string) {
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function variableLabel(item: PriceExpressionVariable) {
  return item.labelKey ? t(item.labelKey) : item.label
}

function formatNumber(value: unknown) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(2) : '-'
}
</script>

<style scoped>
.shipping-formula-editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 12px;
}

.shipping-formula-editor__main,
.shipping-formula-editor__side,
.shipping-formula-editor__block {
  display: grid;
  gap: 10px;
}

.shipping-formula-editor__block,
.shipping-formula-editor__side {
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.shipping-formula-editor__block header,
.shipping-formula-editor__result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.shipping-formula-editor__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.shipping-formula-editor__side {
  align-content: start;
  color: #667085;
  line-height: 1.5;
}

.shipping-formula-editor__side h4,
.shipping-formula-editor__side p,
.shipping-formula-editor__side dl {
  margin: 0;
}

.shipping-formula-editor__side dl {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  gap: 6px 8px;
}

.shipping-formula-editor__side dt {
  color: #1d4ed8;
  font-weight: 700;
}

.shipping-formula-editor__side dd {
  margin: 0;
}

.shipping-formula-editor__example {
  display: grid;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid #e5ecf6;
}

.shipping-formula-editor__example code {
  padding: 6px 8px;
  color: #1f2937;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 6px;
}
</style>
