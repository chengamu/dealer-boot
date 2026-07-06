<template>
  <div class="simulation-config-field" :class="{ 'is-required-missing': requiredMissing }">
    <div class="simulation-config-field__label">
      <span>{{ label }}</span>
      <em v-if="option.requiredFlag">{{ t('productCenter.formulaSimulation.required') }}</em>
    </div>

    <button v-if="materialOption" type="button" class="simulation-material-picker" @click="$emit('open-material', option)">
      <span>
        <strong>{{ selectedLabel || t('productCenter.formulaSimulation.chooseMaterial') }}</strong>
        <small>{{ materialSummary }}</small>
      </span>
      <el-icon><ArrowRight /></el-icon>
    </button>

    <div v-else-if="buttonValues.length > 0" class="simulation-choice-buttons">
      <button
        v-for="value in buttonValues"
        :key="value.valueCode"
        type="button"
        :class="{ 'is-active': modelValue === value.valueCode }"
        @click="$emit('update:modelValue', value.valueCode || '')"
      >
        {{ value.valueNameCn || value.valueNameEn || value.valueCode }}
      </button>
    </div>

    <el-select
      v-else
      :model-value="modelValue"
      filterable
      clearable
      :placeholder="t('productCenter.formulaSetup.optionValueNamePlaceholder')"
      @change="$emit('update:modelValue', String($event || ''))"
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
import { ArrowRight } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  option: ProductFormulaOptionVO
  values: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  modelValue?: string
  showValidation: boolean
}>()

defineEmits<{
  'update:modelValue': [value: string]
  'open-material': [option: ProductFormulaOptionVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const label = computed(() => props.option.optionNameCn || props.option.optionNameEn || props.option.optionCode || '-')
const materialOption = computed(() => props.option.sourceType === 'MATERIAL_POOL')
const buttonValues = computed(() => {
  if (materialOption.value || props.values.length > 6) return []
  return props.values
})
const selectedValue = computed(() => props.values.find((value) => value.valueCode === props.modelValue))
const selectedLabel = computed(() => selectedValue.value
  ? selectedValue.value.valueNameCn || selectedValue.value.valueNameEn || selectedValue.value.valueCode || ''
  : '')
const requiredMissing = computed(() => Boolean(props.showValidation && props.option.requiredFlag && !props.modelValue))
const materialSummary = computed(() => {
  if (!props.modelValue) return t('productCenter.formulaSimulation.notSelected')
  const rows = props.optionMaterials.filter((row) => row.optionCode === props.option.optionCode && row.valueCode === props.modelValue)
  if (rows.length === 0) return t('productCenter.formulaSetup.noLinkedMaterial')
  return rows.map((row) => row.materialNameCn || row.materialCode).filter(Boolean).slice(0, 2).join('、')
    + (rows.length > 2 ? ` +${rows.length - 2}` : '')
})
</script>

<style scoped>
.simulation-config-field {
  min-width: 0;
}

.simulation-config-field :deep(.el-select) {
  width: 100%;
}

.simulation-config-field__label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 5px;
  color: #475467;
  font-size: 13px;
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

.simulation-material-picker {
  display: flex;
  width: 100%;
  min-height: 44px;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 7px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  color: #1f2937;
  text-align: left;
  cursor: pointer;
}

.simulation-material-picker:hover,
.simulation-config-field.is-required-missing .simulation-material-picker {
  border-color: #1677ff;
}

.simulation-material-picker strong,
.simulation-material-picker small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.simulation-material-picker small {
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
}

.simulation-choice-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.simulation-choice-buttons button {
  min-height: 32px;
  padding: 0 13px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  color: #344054;
  cursor: pointer;
}

.simulation-choice-buttons button:hover,
.simulation-choice-buttons button.is-active {
  border-color: #1677ff;
  background: #eef5ff;
  color: #1677ff;
}
</style>
