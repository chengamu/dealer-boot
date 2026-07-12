<template>
  <div class="simulation-header">
    <div class="simulation-header__main">
      <div class="simulation-header__topline">
        <div class="simulation-header__breadcrumb">
          {{ t('productCenter.formula.title') }} / {{ t('productCenter.formula.actions.simulation') }}
        </div>
        <el-select
          class="simulation-header__formula-select"
          filterable
          :model-value="selectedFormulaId"
          :loading="formulaSelecting"
          :placeholder="t('productCenter.formulaSimulation.selectFormulaPlaceholder')"
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
      <div class="simulation-header__title-row">
        <h2>{{ formula.formulaName || '-' }}</h2>
        <el-tag size="small">{{ formulaStatusText }}</el-tag>
        <el-tag size="small" :type="validationTagType">{{ validationText(status) }}</el-tag>
      </div>
      <div class="simulation-header__meta">
        <span>{{ t('productCenter.formula.code') }}：{{ formula.formulaCode || '-' }}</span>
        <span>{{ t('productCenter.formula.category') }}：{{ formula.categoryNameCn || '-' }}</span>
        <span>{{ t('productCenter.formula.productType') }}：{{ formula.productTypeNameCn || '-' }}</span>
        <span>{{ t('productCenter.formulaSimulation.sizeRange') }}：{{ sizeRange }}</span>
      </div>
      <div v-if="message" class="simulation-header__message">{{ messageText(message) }}</div>
    </div>
    <div class="simulation-header__actions">
      <el-button icon="Back" @click="$emit('back')">{{ t('common.back') }}</el-button>
      <el-button icon="DataAnalysis" plain :disabled="!canOperate" :loading="running" @click="$emit('run')">
        {{ t('productCenter.formulaSimulation.run') }}
      </el-button>
      <el-button type="primary" icon="CircleCheck" :disabled="!canOperate" :loading="validating" @click="$emit('validate')">
        {{ t('productCenter.formulaSimulation.validate') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaVO } from '@/api/product-capability/types'
import { FORMULA_VALIDATION_STATUS, formulaStatusText as resolveFormulaStatusText, formulaValidationTagType } from '@/constants/productStatus'
import { formatInch } from '@/utils/businessNumber'

const props = defineProps<{
  formula: ProductFormulaVO
  status?: string
  message?: string
  running: boolean
  validating: boolean
  formulaOptions: Array<{ value: string; label: string }>
  selectedFormulaId: string
  formulaSelecting: boolean
  canOperate: boolean
  validationText: (status?: string) => string
  messageText: (message?: string) => string
}>()

defineEmits<{
  back: []
  run: []
  validate: []
  'formula-change': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const formulaStatusText = computed(() => props.formula.status ? resolveFormulaStatusText(props.formula.status, t) : '-')
const validationTagType = computed(() => formulaValidationTagType(props.status || FORMULA_VALIDATION_STATUS.NOT_VALIDATED))
const sizeRange = computed(() => {
  return `W<=${formatInch(props.formula.maxWidthInch)}, H<=${formatInch(props.formula.maxHeightInch)}`
})
</script>

<style scoped>
.simulation-header {
  display: flex;
  min-width: 0;
  padding: 18px 20px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #fff;
}

.simulation-header__main { min-width: 0; }

.simulation-header__topline {
  display: flex;
  margin-bottom: 8px;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.simulation-header__breadcrumb,
.simulation-header__meta,
.simulation-header__message {
  color: #667085;
  font-size: 13px;
}

.simulation-header__formula-select { width: min(420px, 46vw); }

.simulation-header__title-row,
.simulation-header__meta,
.simulation-header__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.simulation-header__title-row h2 {
  margin: 0;
  color: #1d2129;
  font-size: 22px;
  line-height: 1.3;
}

.simulation-header__meta { margin-top: 10px; }
.simulation-header__actions { justify-content: flex-end; }
.simulation-header__actions :deep(.el-button) { height: 32px; padding: 0 12px; border-radius: 6px; }

@media (max-width: 860px) {
  .simulation-header { display: grid; }
  .simulation-header__actions { justify-content: flex-start; }
}
</style>
