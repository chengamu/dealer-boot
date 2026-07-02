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
        <el-select v-model="materialBuilder.role" class="condition-builder__role" filterable>
          <el-option v-for="role in materialRoles" :key="role.valueCode" :label="role.valueNameCn || role.valueCode" :value="role.valueCode || ''" />
        </el-select>
        <span class="condition-builder__fixed-field">{{ shortLabel('物料类型', 'Material Type') }}</span>
        <el-select v-model="materialBuilder.operator" class="condition-builder__operator">
          <el-option v-for="operator in conditionOperators" :key="operator" :label="operator" :value="operator" />
        </el-select>
        <el-select v-model="materialBuilder.value" class="condition-builder__value" filterable>
          <el-option v-for="value in materialValueOptions" :key="value.valueCode" :label="value.valueNameCn || value.valueCode" :value="value.valueCode || ''" />
        </el-select>
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
import { materialAttributeVariableName, optionVariableName } from './formulaExpressionDisplay'
import FormulaExpressionJoinerSelect from './FormulaExpressionJoinerSelect.vue'
import {
  buildConditionText,
  distinctOptions,
  ensureBuilderField,
  resetBuilderValue,
  valueLabel,
  type BuilderState,
  type ConditionField,
  type ConditionValue,
  type MaterialBuilderState
} from './formulaExpressionConditionOptions'
import type { ProductFormulaMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  hasText: boolean
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
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
const orderBuilder = reactive<BuilderState>({ field: 'orderWidth', operator: '=', value: '', joiner: '并且' })

const businessFields = computed<ConditionField[]>(() => {
  const optionFields = (props.options || []).map((option) => ({
    label: option.optionNameCn || option.optionCode || '-',
    name: optionVariableName(option.optionCode),
    insert: option.optionNameCn || option.optionCode || optionVariableName(option.optionCode),
    optionCode: option.optionCode,
    stringValue: true
  }))
  return [{ label: t('productCenter.formulaSetup.productType'), name: 'productType', insert: t('productCenter.formulaSetup.productType'), stringValue: true }, ...optionFields]
})
const selectedBusinessField = computed(() => businessFields.value.find((field) => field.name === businessBuilder.field))
const businessValueOptions = computed<ConditionValue[]>(() => selectedBusinessField.value?.optionCode
  ? (props.optionValues || []).filter((value) => value.optionCode === selectedBusinessField.value?.optionCode)
  : [])

const orderFields = computed(() => formulaVariables
  .filter((item) => ['orderLength', 'orderWidth', 'orderHeight', 'orderWeight', 'orderArea'].includes(item.name))
  .map((variable) => ({ label: variable.label, name: variable.name, insert: variable.label }))
)

const selectedOrderField = computed(() => orderFields.value.find((field) => field.name === orderBuilder.field))
const materialRoles = computed(() => distinctOptions((props.materials || []).map((item) => ({
  valueCode: item.attributeGroupCode || '',
  valueNameCn: item.attributeGroupNameCn || item.attributeGroupCode
}))))
const materialValueOptions = computed<ConditionValue[]>(() => materialTypeValues())

watch(businessFields, () => ensureBuilderField(businessBuilder, businessFields.value), { immediate: true })
watch(() => businessBuilder.field, () => resetBuilderValue(businessBuilder, businessValueOptions.value))
watch(materialRoles, () => {
  materialBuilder.role = materialRoles.value.some((role) => role.valueCode === materialBuilder.role) ? materialBuilder.role : materialRoles.value[0]?.valueCode || ''
  resetBuilderValue(materialBuilder, materialValueOptions.value)
}, { immediate: true })
watch(() => materialBuilder.role, () => resetBuilderValue(materialBuilder, materialValueOptions.value))

function appendBusinessCondition() {
  appendCondition(selectedBusinessField.value, businessBuilder, businessValueOptions.value)
}

function appendMaterialCondition() {
  const role = materialRoles.value.find((item) => item.valueCode === materialBuilder.role)
  if (!role) return
  const fieldLabel = shortLabel('物料类型', 'Material Type')
  appendCondition({
    label: fieldLabel,
    name: materialAttributeVariableName(role.valueCode, 'materialType'),
    insert: `${role.valueNameCn || role.valueCode}.${fieldLabel}`,
    stringValue: true
  }, materialBuilder, materialValueOptions.value)
}

function appendOrderCondition() {
  appendCondition(selectedOrderField.value, orderBuilder, [])
}

function appendCondition(field: ConditionField | undefined, builder: BuilderState, values: ConditionValue[]) {
  const text = buildConditionText(field, builder, values)
  if (text) emit('insert', text, builder.joiner)
}

function materialTypeValues() {
  return distinctOptions(filteredMaterials().map((item) => ({
    valueCode: item.materialTypeCode || '',
    valueNameCn: item.materialTypeNameCn || item.materialTypeCode
  })))
}

function shortLabel(zh: string, en: string) {
  return localeStore.language === 'zh_CN' ? zh : en
}

function filteredMaterials() {
  return (props.materials || []).filter((item) => item.attributeGroupCode === materialBuilder.role)
}

</script>

<style src="./FormulaExpressionConditionBuilder.css" scoped></style>
