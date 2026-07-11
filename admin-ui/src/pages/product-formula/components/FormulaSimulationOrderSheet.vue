<template>
  <section class="simulation-panel simulation-order-sheet">
    <div class="simulation-section simulation-section--order">
      <div class="simulation-section__title">{{ t('productCenter.formulaSimulation.orderInfo') }}</div>
      <div class="simulation-order-entry">
        <label class="simulation-order-field">
          <span>{{ t('productCenter.formulaSimulation.orderWidth') }}</span>
          <BusinessInchInput v-model="form.orderWidth" />
        </label>
        <label class="simulation-order-field">
          <span>{{ t('productCenter.formulaSimulation.orderHeight') }}</span>
          <BusinessInchInput v-model="form.orderHeight" />
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
        <FormulaSimulationOptionGroup
          v-for="group in optionGroups"
          :key="group.root.optionCode"
          :root="group.root"
          :options="group.options"
          :option-values="optionValues"
          :option-materials="optionMaterials"
          :selected-option-values="selectedOptionValues"
          :show-validation="showValidation"
          :disabled-option-values="disabledOptionValues"
          :restriction-messages="restrictionMessages"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaSimulationOptionGroup from './FormulaSimulationOptionGroup.vue'
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
  disabledOptionValues: Record<string, string[]>
  restrictionMessages: Record<string, string[]>
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
const optionGroups = computed(() => {
  const map = new Map(props.options.map((option) => [option.optionCode, option]))
  const groups = new Map<string, { root: ProductFormulaOptionVO; options: ProductFormulaOptionVO[] }>()
  props.options.forEach((option) => {
    const root = rootOption(option, map)
    const rootCode = root.optionCode || option.optionCode || ''
    if (!groups.has(rootCode)) groups.set(rootCode, { root, options: [] })
    groups.get(rootCode)?.options.push(option)
  })
  return [...groups.values()]
})

function rootOption(option: ProductFormulaOptionVO, map: Map<string | undefined, ProductFormulaOptionVO>) {
  let current = option
  const visited = new Set<string>()
  while (current.visibilityMode === 'CONDITIONAL' && current.visibleConditionOptionCode && !visited.has(current.visibleConditionOptionCode)) {
    visited.add(current.visibleConditionOptionCode)
    current = map.get(current.visibleConditionOptionCode) || current
    if (current.optionCode === option.optionCode) break
  }
  return current
}
</script>
