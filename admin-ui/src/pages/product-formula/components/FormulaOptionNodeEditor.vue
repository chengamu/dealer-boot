<template>
  <div class="node-editor">
    <div class="node-editor__header">
      <div>
        <h3>{{ t('productCenter.formulaSetup.optionSetting') }}</h3>
      </div>
    </div>

    <div v-if="selectedOption" class="option-form">
      <label>
        <span>{{ t('productCenter.formulaSetup.optionName') }}</span>
        <el-input ref="optionNameInputRef" v-model="selectedOption.optionNameCn" :placeholder="t('productCenter.formulaSetup.optionNamePlaceholder')" />
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.optionNameEn') }}</span>
        <el-input v-model="selectedOption.optionNameEn" :placeholder="t('productCenter.formulaSetup.optionNameEnPlaceholder')" />
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.sourceType') }}</span>
        <el-select :model-value="selectedOption.sourceType" @change="$emit('source-type-change', String($event))">
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
      <label v-if="selectedOption.sourceType === 'MATERIAL_POOL'">
        <span>{{ t('productCenter.formulaSetup.displayMode') }}</span>
        <el-select
          :model-value="selectedOption.displayMode || 'SELECT'"
          @change="selectedOption.displayMode = String($event)"
        >
          <el-option value="SELECT" :label="t('productCenter.formulaSetup.displaySelect')" />
          <el-option value="IMAGE_SELECT" :label="t('productCenter.formulaSetup.displayImageSelect')" />
        </el-select>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProductFormulaOptionVO } from '@/api/product-capability/types'
import { nextTick, ref } from 'vue'

defineProps<{
  selectedOption?: ProductFormulaOptionVO
  materialGroupOptions: Array<{ value: string; label: string }>
  t: (key: string, params?: Record<string, string | number>) => string
  sourceGroupCode: (option?: ProductFormulaOptionVO) => string
}>()

defineEmits<{
  'source-type-change': [value: string]
  'source-group-change': [value: string]
}>()

const optionNameInputRef = ref<{ focus: () => void }>()

async function focusOptionName() {
  await nextTick()
  optionNameInputRef.value?.focus()
}

defineExpose({ focusOptionName })
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

.option-form {
  display: grid;
  grid-template-columns:
    minmax(150px, 1fr)
    minmax(140px, 0.9fr)
    minmax(128px, 0.8fr)
    minmax(120px, 0.75fr)
    minmax(108px, 0.7fr)
    minmax(120px, 0.75fr);
  gap: 12px;
  margin-top: 14px;
}

.option-form label {
  display: grid;
  gap: 6px;
  min-width: 0;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 980px) {
  .option-form {
    grid-template-columns: repeat(2, minmax(150px, 1fr));
  }
}
</style>
