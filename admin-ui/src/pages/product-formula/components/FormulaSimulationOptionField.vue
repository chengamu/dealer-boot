<template>
  <div class="simulation-config-field" :class="{ 'is-required-missing': requiredMissing }">
    <div class="simulation-config-field__label">
      <span>{{ label }}</span>
      <em v-if="option.requiredFlag">{{ t('productCenter.formulaSimulation.required') }}</em>
    </div>

    <el-switch
      v-if="switchOption"
      :model-value="switchChecked"
      inline-prompt
      @update:model-value="updateSwitch"
    />

    <FormulaSimulationMaterialSelect
      v-else-if="materialOption"
      :model-value="modelValue"
      :values="values"
      :option-materials="optionMaterials"
      :option-code="option.optionCode"
      :multiple="multipleOption"
      :placeholder="t('productCenter.formulaSetup.optionValueNamePlaceholder')"
      @update:model-value="emit('update:modelValue', $event)"
    />

    <el-select
      v-else
      :model-value="selectModelValue"
      filterable
      clearable
      :multiple="multipleOption"
      :placeholder="t('productCenter.formulaSetup.optionValueNamePlaceholder')"
      @change="updateSelect"
    >
      <el-option
        v-for="value in values"
        :key="value.valueCode"
        :label="value.valueNameCn || value.valueNameEn || value.valueCode"
        :value="value.valueCode"
      />
    </el-select>

    <div v-if="requiredMissing" class="simulation-config-field__error">
      {{ t('productCenter.formulaSimulation.requiredMissing') }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaSimulationMaterialSelect from './FormulaSimulationMaterialSelect.vue'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  option: ProductFormulaOptionVO
  values: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  modelValue?: string
  showValidation: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const label = computed(() => props.option.optionNameCn || props.option.optionNameEn || props.option.optionCode || '-')
const materialOption = computed(() => props.option.sourceType === 'MATERIAL_POOL' && props.option.displayMode === 'IMAGE_SELECT')
const switchOption = computed(() => props.option.sourceType === 'BOOLEAN' || props.option.selectionMode === 'SWITCH')
const multipleOption = computed(() => props.option.selectionMode === 'MULTIPLE')
const selectedCodes = computed(() => splitCodes(props.modelValue))
const requiredMissing = computed(() => Boolean(props.showValidation && props.option.requiredFlag && !props.modelValue))
const selectModelValue = computed(() => multipleOption.value ? selectedCodes.value : props.modelValue)
const switchValues = computed(() => props.values.slice(0, 2))
const switchActiveValue = computed(() => {
  return props.option.defaultValueCode
    || switchValues.value.find((row) => row.defaultFlag)?.valueCode
    || switchValues.value.find((row) => truthyValue(row))?.valueCode
    || switchValues.value[0]?.valueCode
    || 'true'
})
const switchInactiveValue = computed(() => switchValues.value.find((row) => row.valueCode !== switchActiveValue.value)?.valueCode || 'false')
const switchChecked = computed(() => props.modelValue === switchActiveValue.value)

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}

function updateSelect(value: string | string[]) {
  const nextValue = Array.isArray(value) ? value.filter(Boolean).join(',') : String(value || '')
  emit('update:modelValue', nextValue)
}

function updateSwitch(checked: string | number | boolean) {
  emit('update:modelValue', checked ? String(switchActiveValue.value) : String(switchInactiveValue.value))
}

function truthyValue(value?: ProductFormulaOptionValueVO) {
  return [value?.valueCode, value?.valueNameCn, value?.valueNameEn]
    .map((item) => String(item || '').trim().toLowerCase())
    .some((item) => ['true', 'yes', 'y', '1'].includes(item))
}
</script>

<style scoped>
.simulation-config-field {
  min-width: 0;
  padding: 2px 0 4px;
}

.simulation-config-field :deep(.el-select) {
  width: 100%;
}

.simulation-config-field :deep(.el-input__wrapper) {
  min-height: 34px;
}

.simulation-config-field :deep(.el-select__wrapper) {
  min-height: 36px;
  height: auto;
}

.simulation-config-field :deep(.el-select__selection) {
  flex-wrap: wrap;
}

.simulation-config-field :deep(.el-switch) {
  min-height: 32px;
}

.simulation-config-field__label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 5px;
  color: #344054;
  font-size: 14px;
  font-weight: 600;
}

.simulation-config-field__label em {
  color: #ef4444;
  font-size: 12px;
  font-style: normal;
}

.simulation-config-field__error {
  margin-top: 5px;
  color: #ef4444;
  font-size: 12px;
}

.simulation-config-field.is-required-missing :deep(.el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #ef4444 inset;
}

.simulation-config-field.is-required-missing :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #ef4444 inset;
}

</style>
