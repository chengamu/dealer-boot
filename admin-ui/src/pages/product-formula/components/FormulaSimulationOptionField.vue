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

    <el-select
      v-else
      :model-value="selectModelValue"
      :class="{ 'is-material-select': materialOption }"
      filterable
      clearable
      :multiple="multipleOption"
      :popper-class="materialOption ? 'formula-simulation-material-select-popper' : ''"
      :placeholder="t('productCenter.formulaSetup.optionValueNamePlaceholder')"
      @change="updateSelect"
    >
      <template v-if="materialOption && selectedCodes.length === 1 && imageOf(selectedCodes[0])" #prefix>
        <span class="simulation-select-thumb simulation-select-thumb--small">
          <img :src="imageOf(selectedCodes[0])" :alt="selectedLabel" />
        </span>
      </template>
      <el-option
        v-for="value in values"
        :key="value.valueCode"
        :label="value.valueNameCn || value.valueNameEn || value.valueCode"
        :value="value.valueCode"
      >
        <div class="simulation-select-option" :class="{ 'simulation-select-option--with-image': Boolean(imageOf(value.valueCode)) }">
          <span v-if="imageOf(value.valueCode)" class="simulation-select-thumb">
            <img :src="imageOf(value.valueCode)" :alt="valueLabel(value)" />
          </span>
          <span class="simulation-select-option__text">
            <strong>{{ valueLabel(value) }}</strong>
            <small v-if="materialOption">{{ materialSummary(value.valueCode) }}</small>
          </span>
        </div>
      </el-option>
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
import fabricThumbnail from '@/assets/product-formula/placeholders/fabric-thumbnail.png'
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
const materialOption = computed(() => props.option.sourceType === 'MATERIAL_POOL')
const switchOption = computed(() => props.option.sourceType === 'BOOLEAN' || props.option.selectionMode === 'SWITCH')
const multipleOption = computed(() => props.option.selectionMode === 'MULTIPLE')
const selectedCodes = computed(() => splitCodes(props.modelValue))
const requiredMissing = computed(() => Boolean(props.showValidation && props.option.requiredFlag && !props.modelValue))
const selectModelValue = computed(() => multipleOption.value ? selectedCodes.value : props.modelValue)
const selectedLabel = computed(() => {
  const value = props.values.find((row) => row.valueCode === selectedCodes.value[0])
  return valueLabel(value)
})
const switchValues = computed(() => props.values.slice(0, 2))
const switchActiveValue = computed(() => switchValues.value[0]?.valueCode || 'true')
const switchInactiveValue = computed(() => switchValues.value[1]?.valueCode || 'false')
const switchChecked = computed(() => props.modelValue === switchActiveValue.value)

function materialSummary(valueCode?: string) {
  const codes = valueCode ? [String(valueCode)] : selectedCodes.value
  const rows = props.optionMaterials.filter((row) => row.optionCode === props.option.optionCode && codes.includes(String(row.valueCode || '')))
  if (rows.length === 0) return t('productCenter.formulaSetup.noLinkedMaterial')
  return rows.map((row) => row.materialNameCn || row.materialCode).filter(Boolean).slice(0, 2).join('、')
    + (rows.length > 2 ? ` +${rows.length - 2}` : '')
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}

function valueLabel(value?: ProductFormulaOptionValueVO) {
  return value?.valueNameCn || value?.valueNameEn || value?.valueCode || ''
}

function updateSelect(value: string | string[]) {
  const nextValue = Array.isArray(value) ? value.filter(Boolean).join(',') : String(value || '')
  emit('update:modelValue', nextValue)
}

function updateSwitch(checked: string | number | boolean) {
  emit('update:modelValue', checked ? String(switchActiveValue.value) : String(switchInactiveValue.value))
}

function imageOf(valueCode?: string) {
  const value = props.values.find((row) => row.valueCode === valueCode) as Record<string, unknown> | undefined
  const material = props.optionMaterials.find((row) => row.optionCode === props.option.optionCode && row.valueCode === valueCode) as Record<string, unknown> | undefined
  return stringField(value, ['imageUrl', 'thumbnailUrl', 'picture', 'pictureUrl'])
    || stringField(material, ['imageUrl', 'thumbnailUrl', 'picture', 'pictureUrl', 'materialImageUrl'])
    || (materialOption.value ? fabricThumbnail : '')
}

function stringField(row: Record<string, unknown> | undefined, keys: string[]) {
  if (!row) return ''
  const value = keys.map((key) => row[key]).find((item) => typeof item === 'string' && item)
  return typeof value === 'string' ? value : ''
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

.simulation-config-field :deep(.is-material-select .el-select__prefix) {
  align-self: center;
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

.simulation-select-option {
  display: grid;
  min-width: 0;
  padding: 2px 0;
}

.simulation-select-option--with-image {
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-height: 44px;
}

.simulation-select-option__text {
  min-width: 0;
}

.simulation-select-option strong,
.simulation-select-option small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.simulation-select-option small {
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
}

.simulation-select-thumb {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #dbe7f6;
  border-radius: 6px;
  background: #edf5ff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 700;
}

.simulation-select-thumb--small {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  font-size: 11px;
}

.simulation-select-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

:global(.formula-simulation-material-select-popper .el-select-dropdown__item) {
  height: auto;
  min-height: 48px;
  padding: 6px 12px;
  line-height: 1.35;
}

:global(.formula-simulation-material-select-popper .el-select-dropdown__item.is-hovering),
:global(.formula-simulation-material-select-popper .el-select-dropdown__item.hover) {
  background: #eef5ff;
}

:global(.formula-simulation-material-select-popper .simulation-select-option) {
  width: 100%;
}

:global(.formula-simulation-material-select-popper .simulation-select-thumb) {
  flex: 0 0 auto;
}

</style>
