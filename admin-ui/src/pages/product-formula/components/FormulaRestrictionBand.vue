<template>
  <section class="setup-section restriction-section">
    <div class="setup-section__toolbar">
      <div>
        <h3>{{ t('productCenter.formulaSetup.exceptionRestriction') }}</h3>
        <p>{{ t('productCenter.formulaSetup.exceptionRestrictionHint') }}</p>
      </div>
      <el-button type="primary" plain :icon="Plus" @click="$emit('addRestriction')">{{ t('productCenter.formulaSetup.addRestriction') }}</el-button>
    </div>

    <el-table :data="restrictions" border class="setup-table">
      <el-table-column :label="t('productCenter.formulaSetup.restrictionSentence')" min-width="220">
        <template #default="{ row }">
          <el-input v-model="row.restrictionName" clearable />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.conditionExpression')" min-width="360">
        <template #default="{ row }">
          <div class="restriction-expression">
            <span :class="{ 'restriction-expression__text--invalid': restrictionExpressionInvalid(row) }" :title="restrictionExpressionText(row)">
              {{ restrictionExpressionText(row) }}
            </span>
            <el-button plain @click="openExpressionEditor(row)">{{ t('productCenter.formulaSetup.conditionExpressionEditor') }}</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.restrictionTarget')" min-width="430">
        <template #default="{ row }">
          <div class="restriction-target">
            <el-select v-model="row.actionType" class="restriction-target__action">
              <el-option value="DISABLE" :label="t('productCenter.formulaSetup.disable')" />
              <el-option value="WARN" :label="t('productCenter.formulaSetup.warn')" />
            </el-select>
            <el-select
              :model-value="targetOptionValue(row)"
              class="restriction-target__option"
              filterable
              @update:model-value="handleTargetOptionChange(row, $event)"
            >
              <el-option :label="t('productCenter.formulaSetup.wholeOrder')" :value="WHOLE_ORDER_TARGET" />
              <el-option v-for="option in options" :key="String(option.optionCode)" :label="option.optionNameCn || option.optionCode" :value="option.optionCode" />
            </el-select>
            <el-select v-if="row.targetOptionCode" v-model="row.targetValueCode" class="restriction-target__value" filterable clearable>
              <el-option :label="t('productCenter.formulaSetup.allOptionValues')" value="" />
              <el-option v-for="value in valuesForOption(row.targetOptionCode)" :key="String(value.valueCode)" :label="value.valueNameCn || value.valueCode" :value="value.valueCode" />
            </el-select>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.messageText')" min-width="240">
        <template #default="{ row }"><el-input v-model="row.messageText" clearable /></template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="110" align="center">
        <template #default="{ $index }">
          <AdminTableActions :actions="[
            { label: t('common.delete'), icon: 'Delete', type: 'danger', onClick: () => $emit('removeRestriction', $index) }
          ]" />
        </template>
      </el-table-column>
    </el-table>
    <FormulaConditionEditorDialog
      v-model="expressionEditorOpen"
      v-model:text="expressionEditorText"
      :materials="materials"
      :options="options"
      :option-values="allOptionValues"
      :option-materials="optionMaterials"
      @confirm="confirmExpressionEditor"
    />
  </section>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { ref } from 'vue'
import FormulaConditionEditorDialog from './FormulaConditionEditorDialog.vue'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import { validateConditionExpression } from '../utils/formulaExpression'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO
} from '@/api/product-capability/types'

const props = defineProps<{
  restrictions: ProductFormulaRestrictionVO[]
  options: ProductFormulaOptionVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  materials: ProductFormulaMaterialVO[]
}>()

defineEmits<{
  addRestriction: []
  removeRestriction: [index: number]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const expressionEditorOpen = ref(false)
const expressionEditorText = ref('')
const editingRestriction = ref<ProductFormulaRestrictionVO | null>(null)
const WHOLE_ORDER_TARGET = '__WHOLE_ORDER__'

function valuesForOption(optionCode?: string) {
  return props.allOptionValues
    .filter((row) => row.optionCode === optionCode)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

function targetOptionValue(row: ProductFormulaRestrictionVO) {
  return row.targetOptionCode || WHOLE_ORDER_TARGET
}

function handleTargetOptionChange(row: ProductFormulaRestrictionVO, value: string | number | boolean) {
  row.targetOptionCode = value === WHOLE_ORDER_TARGET ? '' : String(value || '')
  row.targetValueCode = ''
}

function openExpressionEditor(row: ProductFormulaRestrictionVO) {
  editingRestriction.value = row
  expressionEditorText.value = row.conditionText || row.conditionExpression || ''
  expressionEditorOpen.value = true
}

function confirmExpressionEditor(value?: { text: string; expression: string; valid: boolean }) {
  const row = editingRestriction.value
  if (!row) return
  const text = (value?.text || expressionEditorText.value).trim()
  const normalized = value?.expression || normalizeDisplayExpression(text, props.options, props.allOptionValues, props.materials)
  const result = validateConditionExpression(normalized)
  if (!result.valid) return
  row.conditionType = 'EXPRESSION'
  row.conditionOperator = 'EXPRESSION'
  row.conditionExpression = result.expression
  row.conditionText = text
  row.conditionOptionCode = ''
  row.conditionValueCode = ''
  row.conditionValueNumber = undefined
  expressionEditorOpen.value = false
}

function restrictionExpressionText(row: ProductFormulaRestrictionVO) {
  return row.conditionText || row.conditionExpression || t('productCenter.formulaSetup.conditionExpressionPlaceholder')
}

function restrictionExpressionInvalid(row: ProductFormulaRestrictionVO) {
  const text = row.conditionExpression || normalizeDisplayExpression(row.conditionText, props.options, props.allOptionValues, props.materials)
  return !validateConditionExpression(text).valid
}
</script>

<style scoped>
.setup-section {
  padding: 16px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.setup-section__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}

.setup-section__toolbar h3 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.setup-section__toolbar p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.restriction-expression,
.restriction-target {
  display: flex;
  align-items: center;
  gap: 8px;
}

.restriction-expression span {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.restriction-expression__text--invalid {
  color: #dc2626;
}

.restriction-expression :deep(.el-button) {
  flex: 0 0 auto;
}

.restriction-target__action {
  width: 112px;
}

.restriction-target__option,
.restriction-target__value {
  width: 150px;
}

.setup-table {
  width: 100%;
}
</style>
