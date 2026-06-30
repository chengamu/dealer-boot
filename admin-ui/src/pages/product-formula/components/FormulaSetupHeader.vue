<template>
  <div class="setup-header">
    <div>
      <div class="setup-header__topline">
        <div class="setup-header__breadcrumb">
          {{ t('productCenter.formula.title') }} /
          {{ activeSection === 'options' ? t('productCenter.formula.actions.options') : t('productCenter.formula.actions.materials') }}
        </div>
        <el-select
          class="setup-header__formula-select"
          filterable
          :model-value="selectedFormulaId"
          :loading="formulaSelecting"
          :placeholder="t('productCenter.formulaSetup.selectEditableFormulaPlaceholder')"
          @change="$emit('formula-change', String($event || ''))"
        >
          <el-option
            v-for="item in formulaOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="setup-header__title">
        <span>{{ formula.formulaName || '-' }}</span>
        <el-tag size="small" type="primary">{{ formula.currentVersionLabel || draftVersionLabel }}</el-tag>
        <el-tag size="small" :type="statusTagType(formula.status)">{{ statusText(formula.status) }}</el-tag>
        <el-tag size="small" :type="validationTagType(formula.latestValidationStatus)">{{ validationText(formula.latestValidationStatus) }}</el-tag>
      </div>
      <div class="setup-header__meta">
        <span>{{ t('productCenter.formula.code') }}：{{ formula.formulaCode || '-' }}</span>
        <span>{{ t('productCenter.formula.category') }}：{{ formula.categoryNameCn || '-' }}</span>
        <span>{{ t('productCenter.formula.productType') }}：{{ formula.productTypeNameCn || '-' }}</span>
        <span>{{ t('productCenter.formula.minWidthInch') }}：{{ formatNumber(formula.minWidthInch) }}</span>
        <span>{{ t('productCenter.formula.minHeightInch') }}：{{ formatNumber(formula.minHeightInch) }}</span>
        <span>{{ t('productCenter.formula.maxWidthInch') }}：{{ formatNumber(formula.maxWidthInch) }}</span>
        <span>{{ t('productCenter.formula.maxHeightInch') }}：{{ formatNumber(formula.maxHeightInch) }}</span>
        <span>{{ t('productCenter.formula.updateTime') }}：{{ formatMinute(formula.updateTime) }}</span>
      </div>
    </div>
    <div class="setup-header__actions">
      <el-button icon="CircleCheck" :disabled="!canOperate" :loading="validating" @click="$emit('validate')">{{ t('productCenter.formula.actions.validate') }}</el-button>
      <el-button type="primary" icon="DocumentChecked" :disabled="!canOperate" :loading="saving" @click="$emit('save')">{{ t('productCenter.formulaSetup.saveDraft') }}</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaVO } from '@/api/product-capability/types'

defineProps<{
  formula: ProductFormulaVO
  draftVersionLabel: string
  saving: boolean
  validating: boolean
  formatNumber: (value?: number | string) => string
  formatMinute: (value?: string) => string
  statusText: (status?: string) => string
  validationText: (status?: string) => string
  statusTagType: (status?: string) => string
  validationTagType: (status?: string) => string
  formulaOptions: Array<{ value: string; label: string }>
  selectedFormulaId: string
  formulaSelecting: boolean
  activeSection: 'content' | 'options'
  canOperate: boolean
}>()

defineEmits<{
  validate: []
  save: []
  'formula-change': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.setup-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.setup-header__topline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.setup-header__breadcrumb {
  color: #718096;
  font-size: 13px;
}

.setup-header__formula-select {
  width: min(420px, 46vw);
}

.setup-header__title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
}

.setup-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  margin-top: 10px;
  color: #4b5563;
  font-size: 13px;
}

.setup-header__actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
</style>
