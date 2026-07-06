<template>
  <section class="simulation-panel simulation-order-sheet">
    <div class="simulation-section simulation-section--order">
      <div class="simulation-section__title">{{ t('productCenter.formulaSimulation.orderInfo') }}</div>
      <div class="simulation-order-entry">
        <label class="simulation-order-field">
          <span>{{ t('productCenter.formulaSimulation.orderWidth') }}</span>
          <FormulaSimulationInchInput v-model="form.orderWidth" />
        </label>
        <label class="simulation-order-field">
          <span>{{ t('productCenter.formulaSimulation.orderHeight') }}</span>
          <FormulaSimulationInchInput v-model="form.orderHeight" />
        </label>
        <label class="simulation-order-field simulation-order-field--quantity">
          <span>{{ t('productCenter.formulaSimulation.quantity') }}</span>
          <el-input-number :model-value="quantity" :min="1" :step="1" controls-position="right" @update:model-value="$emit('update:quantity', Number($event || 1))" />
        </label>
        <label class="simulation-order-field simulation-order-field--room">
          <span>{{ t('productCenter.formulaSimulation.room') }}</span>
          <el-input :model-value="room" clearable :placeholder="t('productCenter.formulaSimulation.roomPlaceholder')" @update:model-value="$emit('update:room', String($event || ''))" />
        </label>
      </div>
    </div>

    <div class="simulation-section">
      <div class="simulation-section__title-row">
        <div class="simulation-section__title">{{ t('productCenter.formulaSimulation.orderConfig') }}</div>
        <span v-if="hiddenCount > 0" class="simulation-section__hint">
          {{ t('productCenter.formulaSimulation.hiddenOptionsNotice', { count: hiddenCount }) }}
        </span>
      </div>
      <el-empty v-if="options.length === 0" :description="t('productCenter.formulaSimulation.noOptions')" />
      <div v-else class="simulation-config-list">
        <FormulaSimulationOptionField
          v-for="option in options"
          :key="option.optionCode"
          v-model="selectedOptionValues[option.optionCode || '']"
          :option="option"
          :values="optionValuesOf(option.optionCode)"
          :option-materials="optionMaterials"
          :show-validation="showValidation"
          @open-material="openMaterialDialog"
        />
      </div>
    </div>

    <FormulaSimulationMaterialDialog
      v-if="activeMaterialOption"
      v-model:open="materialDialogOpen"
      :model-value="selectedOptionValues[activeMaterialOption.optionCode || '']"
      :title="activeMaterialOption.optionNameCn || activeMaterialOption.optionNameEn || activeMaterialOption.optionCode || '-'"
      :values="optionValuesOf(activeMaterialOption.optionCode)"
      :materials="materialsOfOption(activeMaterialOption.optionCode)"
      @update:model-value="selectMaterialValue(String($event || ''))"
    />
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import FormulaSimulationInchInput from './FormulaSimulationInchInput.vue'
import FormulaSimulationMaterialDialog from './FormulaSimulationMaterialDialog.vue'
import FormulaSimulationOptionField from './FormulaSimulationOptionField.vue'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO, ProductFormulaSimulationBO } from '@/api/product-capability/types'

const props = defineProps<{
  form: ProductFormulaSimulationBO
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  selectedOptionValues: Record<string, string>
  quantity: number
  room: string
  hiddenCount: number
  showValidation: boolean
  valueLabel: (optionCode?: string, valueCode?: string) => string
}>()

defineEmits<{
  'update:quantity': [value: number]
  'update:room': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string, named?: Record<string, string | number>) => {
  let message = getMessage(key, localeStore.language)
  Object.entries(named || {}).forEach(([name, value]) => {
    message = message.replace(`{${name}}`, String(value))
  })
  return message
}
const materialDialogOpen = ref(false)
const activeMaterialOption = ref<ProductFormulaOptionVO | null>(null)

function optionValuesOf(optionCode?: string) {
  return props.optionValues
    .filter((value) => value.status === PRODUCT_STATUS_ENABLED && value.optionCode === optionCode)
    .sort((left, right) => (left.sortOrder ?? 999999) - (right.sortOrder ?? 999999))
}

function materialsOfOption(optionCode?: string) {
  return props.optionMaterials.filter((row) => row.optionCode === optionCode)
}

function openMaterialDialog(option: ProductFormulaOptionVO) {
  activeMaterialOption.value = option
  materialDialogOpen.value = true
}

function selectMaterialValue(valueCode: string) {
  const optionCode = activeMaterialOption.value?.optionCode
  if (!optionCode) return
  if (valueCode) props.selectedOptionValues[optionCode] = valueCode
  else delete props.selectedOptionValues[optionCode]
}
</script>
