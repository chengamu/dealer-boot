<template>
  <div class="condition-builder">
    <section class="condition-builder__section condition-builder__section--business">
      <h4>{{ t('productCenter.formulaSetup.businessVariables') }}</h4>
      <div class="condition-builder__row condition-builder__row--business" :class="{ 'condition-builder__row--with-joiner': hasText }">
        <el-select v-model="businessBuilder.field" class="condition-builder__field" filterable>
          <el-option v-for="field in businessFields" :key="field.name" :label="field.label" :value="field.name" />
        </el-select>
        <el-select v-model="businessBuilder.operator" class="condition-builder__operator">
          <el-option v-for="operator in conditionOperators" :key="operator" :label="operator" :value="operator" />
        </el-select>
        <el-select v-if="businessValueOptions.length" v-model="businessBuilder.value" class="condition-builder__value" filterable>
          <el-option v-for="value in businessValueOptions" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode || ''" />
        </el-select>
        <el-input v-else v-model="businessBuilder.value" class="condition-builder__value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
        <span class="condition-builder__break" />
        <FormulaExpressionJoinerSelect v-if="hasText" v-model="businessBuilder.joiner" />
        <el-button class="condition-builder__action" type="primary" plain @click="appendBusinessCondition">{{ t('productCenter.formulaSetup.insertCondition') }}</el-button>
      </div>
    </section>

    <section class="condition-builder__section condition-builder__section--material">
      <h4>{{ t('productCenter.formulaSetup.materialAttributes') }}</h4>
      <div class="condition-builder__row condition-builder__row--material" :class="{ 'condition-builder__row--with-joiner': hasText }">
        <el-select v-model="materialBuilder.role" class="condition-builder__role" filterable @change="materialBuilder.field = ''">
          <el-option v-for="option in optionChoices" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-select v-model="materialBuilder.field" class="condition-builder__field" filterable>
          <el-option v-for="attribute in materialAttributeChoices" :key="attribute.value" :label="attribute.label" :value="attribute.value" />
        </el-select>
        <el-select v-model="materialBuilder.operator" class="condition-builder__operator">
          <el-option v-for="operator in conditionOperators" :key="operator" :label="operator" :value="operator" />
        </el-select>
        <el-input v-model="materialBuilder.value" class="condition-builder__value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
        <span class="condition-builder__break" />
        <FormulaExpressionJoinerSelect v-if="hasText" v-model="materialBuilder.joiner" />
        <el-button class="condition-builder__action" type="primary" plain @click="appendMaterialCondition">{{ t('productCenter.formulaSetup.insertCondition') }}</el-button>
      </div>
    </section>

    <section class="condition-builder__section condition-builder__section--order">
      <h4>{{ t('productCenter.formulaSetup.orderVariables') }}</h4>
      <div class="condition-builder__row condition-builder__row--order" :class="{ 'condition-builder__row--with-joiner': hasText }">
        <el-select v-model="orderBuilder.field" class="condition-builder__field">
          <el-option v-for="field in orderFields" :key="field.name" :label="field.label" :value="field.name" />
        </el-select>
        <el-select v-model="orderBuilder.operator" class="condition-builder__operator">
          <el-option v-for="operator in conditionOperators" :key="operator" :label="operator" :value="operator" />
        </el-select>
        <el-input v-model="orderBuilder.value" class="condition-builder__value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
        <span class="condition-builder__break" />
        <FormulaExpressionJoinerSelect v-if="hasText" v-model="orderBuilder.joiner" />
        <el-button class="condition-builder__action" type="primary" plain @click="appendOrderCondition">{{ t('productCenter.formulaSetup.insertCondition') }}</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formulaVariables } from '../utils/formulaExpression'
import { optionVariableName } from './formulaExpressionDisplay'
import FormulaExpressionJoinerSelect from './FormulaExpressionJoinerSelect.vue'
import { materialAttributeOptions, optionSelectOptions } from './formulaConditionEditor'
import {
  buildConditionText,
  ensureBuilderField,
  resetBuilderValue,
  valueLabel,
  type BuilderState,
  type ConditionField,
  type ConditionValue,
  type MaterialBuilderState
} from './formulaExpressionConditionOptions'
import {
  optionClientKey,
  valueOwnerClientKey
} from '../utils/formulaOptionDraftIdentity'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO
} from '@/api/product-capability/types'

const props = defineProps<{
  hasText: boolean
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
  optionMaterials?: ProductFormulaOptionMaterialVO[]
  materials?: ProductFormulaMaterialVO[]
}>()

const emit = defineEmits<{
  insert: [value: string, joiner: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const conditionOperators = ['=', '!=', '>', '>=', '<', '<=']
const businessBuilder = reactive<BuilderState>({ field: 'productType', operator: '=', value: '', joiner: '并且' })
const materialBuilder = reactive<MaterialBuilderState>({ role: '', field: 'materialType', operator: '=', value: '', joiner: '并且' })
const orderBuilder = reactive<BuilderState>({ field: 'orderWidthIn', operator: '=', value: '', joiner: '并且' })
const optionChoices = computed(() => optionSelectOptions(props.options || []))

const businessFields = computed<ConditionField[]>(() => {
  const optionFields = (props.options || []).map((option) => ({
    label: option.optionNameCn || option.optionCode || '-',
    name: optionClientKey(option) || option.optionCode || optionVariableName(option.optionCode),
    insert: option.optionNameCn || option.optionCode || optionVariableName(option.optionCode),
    optionCode: option.optionCode,
    stringValue: true
  }))
  return [{ label: t('productCenter.formulaSetup.productType'), name: 'productType', insert: t('productCenter.formulaSetup.productType'), stringValue: true }, ...optionFields]
})
const selectedBusinessField = computed(() => businessFields.value.find((field) => field.name === businessBuilder.field))
const businessValueOptions = computed<ConditionValue[]>(() => selectedBusinessField.value?.optionCode
  ? optionValuesFor(selectedBusinessField.value.optionCode)
  : [])

const orderFields = computed(() => formulaVariables
  .filter((item) => ['orderWidthIn', 'orderLengthIn', 'orderWidthCm', 'orderLengthCm', 'orderAreaM2'].includes(item.name))
  .map((variable) => ({ label: variable.label, name: variable.name, insert: variable.label }))
)

const selectedOrderField = computed(() => orderFields.value.find((field) => field.name === orderBuilder.field))
const materialAttributeChoices = computed(() => materialAttributeOptions(materialBuilder.role, props.options || [], props.optionMaterials || [], props.materials || []))

watch(businessFields, () => ensureBuilderField(businessBuilder, businessFields.value), { immediate: true })
watch(() => businessBuilder.field, () => resetBuilderValue(businessBuilder, businessValueOptions.value))
watch(optionChoices, () => {
  materialBuilder.role = optionChoices.value.some((option) => option.value === materialBuilder.role) ? materialBuilder.role : optionChoices.value[0]?.value || ''
  materialBuilder.field = materialAttributeChoices.value.some((attribute) => attribute.value === materialBuilder.field) ? materialBuilder.field : materialAttributeChoices.value[0]?.value || ''
}, { immediate: true })
watch(() => materialBuilder.role, () => {
  materialBuilder.field = materialAttributeChoices.value[0]?.value || ''
})

function appendBusinessCondition() {
  appendCondition(selectedBusinessField.value, businessBuilder, businessValueOptions.value)
}

function appendMaterialCondition() {
  const option = optionChoices.value.find((item) => item.value === materialBuilder.role)
  const attribute = materialAttributeChoices.value.find((item) => item.value === materialBuilder.field)
  if (!option || !attribute) return
  appendCondition({
    label: attribute.label,
    name: `${option.value}.${attribute.value}`,
    insert: `${option.label}.${attribute.label}`
  }, materialBuilder, [])
}

function appendOrderCondition() {
  appendCondition(selectedOrderField.value, orderBuilder, [])
}

function appendCondition(field: ConditionField | undefined, builder: BuilderState, values: ConditionValue[]) {
  const text = buildConditionText(field, builder, values)
  if (text) emit('insert', text, builder.joiner)
}

function optionValuesFor(optionCode?: string) {
  const option = (props.options || []).find((row) => row.optionCode === optionCode)
  const ownerKey = optionClientKey(option)
  return (props.optionValues || []).filter((value) => {
    const valueOwnerKey = valueOwnerClientKey(value)
    return valueOwnerKey ? valueOwnerKey === ownerKey : value.optionCode === optionCode
  })
}

</script>

<style src="./FormulaExpressionConditionBuilder.css" scoped></style>
