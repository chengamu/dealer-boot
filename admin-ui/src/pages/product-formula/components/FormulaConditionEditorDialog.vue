<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.conditionExpressionEditor')"
    width="min(980px, calc(100vw - 32px))"
    class="formula-condition-dialog"
    append-to-body
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="formula-condition-editor">
      <el-segmented v-model="activeTab" :options="tabOptions" />

      <section class="formula-condition-editor__panel">
        <FormulaConditionOrderPanel
          v-if="activeTab === 'ORDER'"
          :t="t"
          @change="setResult"
        />
        <FormulaConditionOptionPanel
          v-if="activeTab === 'OPTION'"
          :t="t"
          :options="options"
          :option-values="optionValues"
          @change="setResult"
        />
        <FormulaConditionMaterialAttributePanel
          v-if="activeTab === 'MATERIAL_ATTRIBUTE'"
          :t="t"
          :options="options"
          :option-materials="optionMaterials"
          :materials="materials"
          @change="setResult"
        />
        <FormulaConditionAdvancedPanel
          v-if="activeTab === 'ADVANCED'"
          :t="t"
          :text="text"
          :options="options"
          :option-values="optionValues"
          :materials="materials"
          @update:text="$emit('update:text', $event)"
          @change="setResult"
        />
      </section>

      <FormulaExpressionValidationPanel
        :title="t('productCenter.formulaSetup.validationResult')"
        :status-text="validationText"
        :normalized-title="t('productCenter.formulaSetup.normalizedExpression')"
        :expression="currentResult.expression"
        :valid="currentResult.valid"
      />
    </div>

    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!currentResult.valid" @click="confirm">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaConditionAdvancedPanel from './FormulaConditionAdvancedPanel.vue'
import FormulaConditionMaterialAttributePanel from './FormulaConditionMaterialAttributePanel.vue'
import FormulaConditionOptionPanel from './FormulaConditionOptionPanel.vue'
import FormulaConditionOrderPanel from './FormulaConditionOrderPanel.vue'
import FormulaExpressionValidationPanel from './FormulaExpressionValidationPanel.vue'
import type { ConditionBuildResult } from './formulaConditionEditor'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO
} from '@/api/product-capability/types'

type ConditionTab = 'ORDER' | 'OPTION' | 'MATERIAL_ATTRIBUTE' | 'ADVANCED'

const props = defineProps<{
  modelValue: boolean
  text: string
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  materials: ProductFormulaMaterialVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  confirm: [value: ConditionBuildResult]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const activeTab = ref<ConditionTab>('ORDER')
const currentResult = ref<ConditionBuildResult>({ text: '', expression: '', valid: false, message: 'empty' })

const tabOptions = computed(() => [
  { label: t('productCenter.formulaSetup.orderCondition'), value: 'ORDER' },
  { label: t('productCenter.formulaSetup.optionCondition'), value: 'OPTION' },
  { label: t('productCenter.formulaSetup.materialAttributeCondition'), value: 'MATERIAL_ATTRIBUTE' },
  { label: t('productCenter.formulaSetup.conditionAdvancedExpression'), value: 'ADVANCED' }
])

const validationText = computed(() => {
  if (currentResult.value.valid) return t('productCenter.formulaSetup.expressionValid')
  const map: Record<string, string> = {
    empty: 'productCenter.formulaSetup.expressionEmpty',
    conditionMustBeBoolean: 'productCenter.formulaSetup.conditionMustBeBoolean'
  }
  return t(map[currentResult.value.message || ''] || 'productCenter.formulaSetup.formulaInvalid')
})

watch(() => props.modelValue, (open) => {
  if (open && props.text) activeTab.value = 'ADVANCED'
})

function setResult(value: ConditionBuildResult) {
  currentResult.value = value
  if (value.text && activeTab.value !== 'ADVANCED') emit('update:text', value.text)
}

function confirm() {
  if (!currentResult.value.valid) return
  emit('confirm', currentResult.value)
}
</script>

<style src="./FormulaConditionEditorDialog.css"></style>
