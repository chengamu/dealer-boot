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
          <el-option v-for="group in materialGroupChoices" :key="group.name" :label="group.label" :value="group.name" />
        </el-select>
        <el-select v-model="materialBuilder.field" class="condition-builder__field" filterable>
          <el-option v-for="field in materialFieldChoices" :key="field.name" :label="field.label" :value="field.name" />
        </el-select>
        <el-select v-model="materialBuilder.operator" class="condition-builder__operator">
          <el-option v-for="operator in conditionOperators" :key="operator" :label="operator" :value="operator" />
        </el-select>
        <el-select v-if="materialValueOptions.length" v-model="materialBuilder.value" class="condition-builder__value" filterable>
          <el-option v-for="value in materialValueOptions" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode || ''" />
        </el-select>
        <el-input v-else v-model="materialBuilder.value" class="condition-builder__value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
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
const materialGroupChoices = computed<ConditionField[]>(() => {
  const map = new Map<string, ConditionField>()
  ;(props.materials || []).forEach((material) => {
    if (!material.attributeGroupCode || map.has(material.attributeGroupCode)) return
    map.set(material.attributeGroupCode, {
      label: material.attributeGroupNameCn || material.attributeGroupCode,
      name: material.attributeGroupCode,
      insert: material.attributeGroupNameCn || material.attributeGroupCode
    })
  })
  return Array.from(map.values())
})

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
const selectedMaterialGroup = computed(() => materialGroupChoices.value.find((group) => group.name === materialBuilder.role))
const materialFieldChoices = computed<ConditionField[]>(() => {
  const rows = materialsInGroup(materialBuilder.role)
  const fields: ConditionField[] = [
    { label: t('productCenter.formulaSetup.materialType'), name: 'materialType', insert: `${selectedMaterialGroup.value?.insert || ''}.物料类型`, stringValue: true },
    { label: t('productCenter.formulaSetup.materialCode'), name: 'materialCode', insert: `${selectedMaterialGroup.value?.insert || ''}.物料编码`, stringValue: true },
    { label: t('productCenter.formulaSetup.materialName'), name: 'materialName', insert: `${selectedMaterialGroup.value?.insert || ''}.物料名称`, stringValue: true },
    { label: t('productCenter.formulaSetup.attributeGroup'), name: 'attributeGroup', insert: `${selectedMaterialGroup.value?.insert || ''}.属性分组`, stringValue: true }
  ]
  const attributes = new Map<string, ConditionField>()
  rows.flatMap((material) => material.attributeList || []).forEach((attribute) => {
    if (!attribute.attributeCode || attributes.has(attribute.attributeCode)) return
    const allValues = rows.flatMap((material) => material.attributeList || []).filter((item) => item.attributeCode === attribute.attributeCode)
    attributes.set(attribute.attributeCode, {
      label: attribute.attributeNameCn || attribute.attributeNameEn || attribute.attributeCode,
      name: attribute.attributeCode,
      insert: `${selectedMaterialGroup.value?.insert || ''}.${attribute.attributeNameCn || attribute.attributeCode}`,
      stringValue: !allValues.some((item) => item.valueNumber !== undefined && item.valueNumber !== null)
    })
  })
  return fields.concat(Array.from(attributes.values()))
})
const selectedMaterialField = computed(() => materialFieldChoices.value.find((field) => field.name === materialBuilder.field))
const materialValueOptions = computed<ConditionValue[]>(() => materialValueOptionsFor(materialBuilder.role, materialBuilder.field))

watch(businessFields, () => ensureBuilderField(businessBuilder, businessFields.value), { immediate: true })
watch(() => businessBuilder.field, () => resetBuilderValue(businessBuilder, businessValueOptions.value))
watch(materialGroupChoices, () => {
  materialBuilder.role = materialGroupChoices.value.some((group) => group.name === materialBuilder.role) ? materialBuilder.role : materialGroupChoices.value[0]?.name || ''
  materialBuilder.field = materialFieldChoices.value.some((field) => field.name === materialBuilder.field) ? materialBuilder.field : materialFieldChoices.value[0]?.name || ''
}, { immediate: true })
watch(() => materialBuilder.role, () => {
  materialBuilder.field = materialFieldChoices.value[0]?.name || ''
  resetBuilderValue(materialBuilder, materialValueOptions.value)
})
watch(() => materialBuilder.field, () => {
  resetBuilderValue(materialBuilder, materialValueOptions.value)
})

function appendBusinessCondition() {
  appendCondition(selectedBusinessField.value, businessBuilder, businessValueOptions.value)
}

function appendMaterialCondition() {
  if (!selectedMaterialGroup.value) return
  appendCondition(selectedMaterialField.value, materialBuilder, materialValueOptions.value)
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

function materialsInGroup(groupCode?: string) {
  return (props.materials || []).filter((material) => !groupCode || material.attributeGroupCode === groupCode)
}

function materialValueOptionsFor(groupCode: string, fieldName: string): ConditionValue[] {
  const rows = materialsInGroup(groupCode)
  if (fieldName === 'materialType') {
    return distinctMaterialValues(rows.map((material) => ({
      valueCode: material.materialTypeCode,
      valueNameCn: material.materialTypeNameCn || material.materialTypeCode,
      label: material.materialTypeNameCn && material.materialTypeNameCn !== material.materialTypeCode
        ? `${material.materialTypeNameCn} (${material.materialTypeCode})`
        : material.materialTypeCode
    })))
  }
  if (fieldName === 'materialCode') {
    return distinctMaterialValues(rows.map((material) => ({
      valueCode: material.materialCode,
      valueNameCn: material.materialCode,
      label: `${material.materialCode || ''} ${material.materialNameCn || ''}`.trim()
    })))
  }
  if (fieldName === 'materialName') {
    return distinctMaterialValues(rows.map((material) => ({
      valueCode: material.materialNameCn,
      valueNameCn: material.materialNameCn,
      label: material.materialNameCn
    })))
  }
  if (fieldName === 'attributeGroup') {
    return distinctMaterialValues(rows.map((material) => ({
      valueCode: material.attributeGroupCode,
      valueNameCn: material.attributeGroupNameCn || material.attributeGroupCode,
      label: material.attributeGroupNameCn || material.attributeGroupCode
    })))
  }
  return []
}

function distinctMaterialValues(values: ConditionValue[]) {
  const map = new Map<string, ConditionValue>()
  values.forEach((value) => {
    if (!value.valueCode || map.has(value.valueCode)) return
    map.set(value.valueCode, value)
  })
  return Array.from(map.values())
}

</script>

<style src="./FormulaExpressionConditionBuilder.css" scoped></style>
