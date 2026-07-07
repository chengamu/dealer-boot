<template>
  <el-select
    :model-value="selectModelValue"
    class="formula-order-material-select"
    filterable
    clearable
    :multiple="multiple"
    popper-class="formula-order-material-select-popper"
    :placeholder="placeholder"
    @change="updateValue"
  >
    <template v-if="selectedCodes.length === 1" #prefix>
      <span class="formula-order-material-select__thumb formula-order-material-select__thumb--small">
        <img :src="imageOf(selectedCodes[0])" :alt="selectedLabel" />
      </span>
    </template>
    <el-option
      v-for="value in values"
      :key="value.valueCode"
      :label="valueLabel(value)"
      :value="value.valueCode"
      :disabled="isValueDisabled(value.valueCode)"
    >
      <div class="formula-order-material-select__option">
        <span class="formula-order-material-select__thumb">
          <img :src="imageOf(value.valueCode)" :alt="valueLabel(value)" />
        </span>
        <span class="formula-order-material-select__text">
          <strong>{{ valueLabel(value) }}</strong>
          <small v-if="isValueDisabled(value.valueCode)">{{ disabledHint }}</small>
        </span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import fabricThumbnail from '@/assets/product-formula/placeholders/fabric-thumbnail.png'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  modelValue?: string
  values: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  optionCode?: string
  multiple: boolean
  placeholder: string
  disabledValueCodes: string[]
  disabledHint: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const selectedCodes = computed(() => splitCodes(props.modelValue))
const selectModelValue = computed(() => props.multiple ? selectedCodes.value : props.modelValue)
const selectedLabel = computed(() => valueLabel(props.values.find((row) => row.valueCode === selectedCodes.value[0])))

function updateValue(value: string | string[]) {
  emit('update:modelValue', Array.isArray(value) ? value.filter(Boolean).join(',') : String(value || ''))
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}

function valueLabel(value?: ProductFormulaOptionValueVO) {
  return value?.valueNameCn || value?.valueNameEn || value?.valueCode || ''
}

function imageOf(valueCode?: string) {
  const value = props.values.find((row) => row.valueCode === valueCode) as Record<string, unknown> | undefined
  const material = props.optionMaterials.find((row) => row.optionCode === props.optionCode && row.valueCode === valueCode) as Record<string, unknown> | undefined
  return stringField(value, ['imageUrl', 'thumbnailUrl', 'picture', 'pictureUrl'])
    || stringField(material, ['imageUrl', 'thumbnailUrl', 'picture', 'pictureUrl', 'materialImageUrl'])
    || fabricThumbnail
}

function isValueDisabled(valueCode?: string) {
  return props.disabledValueCodes.includes('*') || props.disabledValueCodes.includes(String(valueCode || ''))
}

function stringField(row: Record<string, unknown> | undefined, keys: string[]) {
  if (!row) return ''
  const value = keys.map((key) => row[key]).find((item) => typeof item === 'string' && item)
  return typeof value === 'string' ? value : ''
}
</script>

<style scoped>
.formula-order-material-select {
  width: 100%;
}

.formula-order-material-select :deep(.el-select__prefix) {
  align-self: center;
}

.formula-order-material-select__option {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.formula-order-material-select__option strong {
  overflow: hidden;
  color: #243044;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.formula-order-material-select__text {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.formula-order-material-select__text small {
  overflow: hidden;
  color: #f59e0b;
  font-size: 12px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.formula-order-material-select__thumb {
  display: inline-flex;
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #dbe7f6;
  border-radius: 6px;
  background: #edf5ff;
}

.formula-order-material-select__thumb--small {
  width: 22px;
  height: 22px;
  border-radius: 4px;
}

.formula-order-material-select__thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

:global(.formula-order-material-select-popper .el-select-dropdown__item) {
  display: flex;
  height: auto !important;
  min-height: 46px;
  align-items: center;
  padding: 6px 12px !important;
  line-height: normal !important;
}

:global(.formula-order-material-select-popper .el-select-dropdown__item.hover),
:global(.formula-order-material-select-popper .el-select-dropdown__item:hover) {
  background: #eef5ff;
}
</style>
