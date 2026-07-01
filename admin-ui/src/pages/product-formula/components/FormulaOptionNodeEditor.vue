<template>
  <div class="node-editor">
    <div class="node-editor__header">
      <div>
        <h3>{{ selectedNode?.type === 'value' ? t('productCenter.formulaSetup.valueSetting') : t('productCenter.formulaSetup.optionSetting') }}</h3>
        <p>{{ selectedOption ? visibilityText(selectedOption) : '' }}</p>
      </div>
      <div class="node-editor__actions">
        <el-button-group v-if="selectedNode?.type !== 'value'">
          <el-button :icon="ArrowUp" :disabled="currentOptionIndex <= 0" @click="$emit('move-option', 'UP')">{{ t('productCenter.formulaSetup.moveUp') }}</el-button>
          <el-button :icon="ArrowDown" :disabled="currentOptionIndex < 0 || currentOptionIndex >= siblingOptions.length - 1" @click="$emit('move-option', 'DOWN')">{{ t('productCenter.formulaSetup.moveDown') }}</el-button>
        </el-button-group>
        <el-button
          v-if="selectedNode?.type === 'value' && selectedValue"
          type="primary"
          plain
          :icon="Plus"
          @click="$emit('add-child-option')"
        >
          {{ t('productCenter.formulaSetup.addChildOption') }}
        </el-button>
        <el-button v-if="selectedNode?.type !== 'value'" link type="danger" @click="$emit('remove-option')">{{ t('common.delete') }}</el-button>
      </div>
    </div>

    <div v-if="selectedOption" class="option-form">
      <label>
        <span>{{ t('productCenter.formulaSetup.optionName') }}</span>
        <el-input v-model="selectedOption.optionNameCn" :placeholder="t('productCenter.formulaSetup.optionNamePlaceholder')" />
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.sourceType') }}</span>
        <el-select v-model="selectedOption.sourceType" @change="$emit('source-type-change')">
          <el-option value="MATERIAL_POOL" :label="t('productCenter.formulaSetup.sourceMaterialPool')" />
          <el-option value="PRODUCT_DICT" :label="t('productCenter.formulaSetup.sourceProductDict')" />
          <el-option value="BOOLEAN" :label="t('productCenter.formulaSetup.sourceBoolean')" />
          <el-option value="MANUAL" :label="t('productCenter.formulaSetup.sourceManual')" />
        </el-select>
      </label>
      <label v-if="selectedOption.sourceType === 'MATERIAL_POOL'">
        <span>{{ t('productCenter.formulaSetup.sourceMaterialGroup') }}</span>
        <el-select :model-value="sourceGroupCode(selectedOption)" filterable @change="$emit('source-group-change', String($event))">
          <el-option v-for="group in materialGroupOptions" :key="group.value" :label="group.label" :value="group.value" />
        </el-select>
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.selectionMode') }}</span>
        <el-select v-model="selectedOption.selectionMode">
          <el-option value="SINGLE" :label="t('productCenter.formulaSetup.single')" />
          <el-option value="MULTIPLE" :label="t('productCenter.formulaSetup.multiple')" />
          <el-option value="SWITCH" :label="t('productCenter.formulaSetup.switch')" />
        </el-select>
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.orderVisibility') }}</span>
        <div class="visibility-summary">{{ visibilityText(selectedOption) }}</div>
      </label>
      <label class="option-form__switch">
        <span>{{ t('productCenter.formulaSetup.requiredFlag') }}</span>
        <el-switch v-model="selectedOption.requiredFlag" />
      </label>
      <label class="option-form__switch">
        <span>{{ t('productCenter.formulaSetup.businessVisible') }}</span>
        <el-switch v-model="selectedOption.businessVisibleFlag" />
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, ArrowUp, Plus } from '@element-plus/icons-vue'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import type { OptionTreeNode } from './useFormulaOptionTree'

defineProps<{
  selectedNode?: OptionTreeNode
  selectedOption?: ProductFormulaOptionVO
  selectedValue?: ProductFormulaOptionValueVO
  currentOptionIndex: number
  siblingOptions: ProductFormulaOptionVO[]
  materialGroupOptions: Array<{ value: string; label: string }>
  t: (key: string, params?: Record<string, string | number>) => string
  visibilityText: (option: ProductFormulaOptionVO) => string
  sourceGroupCode: (option?: ProductFormulaOptionVO) => string
}>()

defineEmits<{
  'move-option': [direction: 'UP' | 'DOWN']
  'add-child-option': []
  'remove-option': []
  'source-type-change': []
  'source-group-change': [value: string]
}>()
</script>

<style scoped>
.node-editor {
  padding: 16px;
  border-bottom: 1px solid #edf1f7;
}

.node-editor__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.node-editor__header h3 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.node-editor__header p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.node-editor__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.option-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.option-form label {
  display: grid;
  gap: 6px;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.option-form__switch {
  align-content: end;
  grid-template-columns: auto auto;
  align-items: center;
  justify-content: start;
}

.visibility-summary {
  display: flex;
  align-items: center;
  min-height: 34px;
  padding: 0 10px;
  color: #1f2937;
  background: #f8fbff;
  border: 1px solid #dce6f2;
  border-radius: 6px;
}

@media (max-width: 1180px) {
  .option-form {
    grid-template-columns: repeat(2, minmax(150px, 1fr));
  }
}
</style>
