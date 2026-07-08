<template>
  <section class="workbench-header">
    <div class="workbench-header__main">
      <div class="workbench-header__breadcrumb">
        {{ t('productCenter.formula.title') }} / {{ t('productCenter.formulaWorkbench.title') }}
      </div>
      <div class="workbench-header__title">
        <h2>{{ formula.formulaName || '-' }}</h2>
        <el-tag :type="formulaStatusTagType(formula.status)" effect="plain">{{ formulaStatusText(formula.status, t) }}</el-tag>
        <el-tag :type="formulaValidationTagType(formula.latestValidationStatus)" effect="plain">
          {{ formulaValidationStatusText(formula.latestValidationStatus, t) }}
        </el-tag>
      </div>
      <div class="workbench-header__meta">
        <span>{{ t('productCenter.formula.code') }}：{{ formula.formulaCode || '-' }}</span>
        <span>{{ t('productCenter.formula.category') }}：{{ formula.categoryNameCn || '-' }}</span>
        <span>{{ t('productCenter.formula.productType') }}：{{ formula.productTypeNameCn || '-' }}</span>
        <span>{{ t('productCenter.formula.currentVersion') }}：{{ versionText }}</span>
        <span>{{ t('productCenter.formula.updateTime') }}：{{ updateTime }}</span>
      </div>
    </div>
    <div class="workbench-header__actions">
      <el-button icon="Back" @click="$emit('back')">{{ t('common.back') }}</el-button>
      <el-button icon="CopyDocument" @click="$emit('copy')">{{ t('productCenter.formula.actions.copyFormula') }}</el-button>
      <el-button
        v-if="canValidate"
        type="primary"
        plain
        icon="CircleCheck"
        :loading="loading"
        @click="$emit('validate')"
      >
        {{ t('productCenter.formula.actions.validate') }}
      </el-button>
      <el-button
        v-if="canSubmit"
        type="primary"
        icon="Promotion"
        :loading="loading"
        @click="$emit('submit')"
      >
        {{ t('productCenter.formula.actions.submitReview') }}
      </el-button>
      <el-button
        v-if="canRevise"
        type="warning"
        plain
        icon="EditPen"
        :loading="loading"
        @click="$emit('start-revision')"
      >
        {{ t('productCenter.formula.actions.startRevision') }}
      </el-button>
      <el-button
        v-if="canWithdraw"
        type="warning"
        icon="RefreshLeft"
        :loading="loading"
        @click="$emit('withdraw')"
      >
        {{ t('productCenter.formula.actions.withdraw') }}
      </el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ProductFormulaVO } from '@/api/product-capability/types'
import {
  FORMULA_STATUS,
  formulaStatusTagType,
  formulaStatusText,
  formulaValidationStatusText,
  formulaValidationTagType
} from '@/constants/productStatus'
import { formatFormulaReviewMinute } from '../utils/formulaReviewDisplay'

const props = defineProps<{
  formula: ProductFormulaVO
  loading?: boolean
  t: (key: string) => string
}>()

defineEmits<{
  back: []
  copy: []
  validate: []
  submit: []
  'start-revision': []
  withdraw: []
}>()

const t = props.t
const editableStatuses = new Set<string>([FORMULA_STATUS.DRAFT, FORMULA_STATUS.REJECTED])
const canValidate = computed(() => editableStatuses.has(String(props.formula.status || '')))
const canSubmit = canValidate
const canRevise = computed(() => props.formula.status === FORMULA_STATUS.EFFECTIVE)
const canWithdraw = computed(() => props.formula.status === FORMULA_STATUS.EFFECTIVE || props.formula.status === FORMULA_STATUS.PENDING_REVIEW)
const updateTime = computed(() => formatFormulaReviewMinute(props.formula.updateTime) || '-')
const versionText = computed(() => props.formula.currentVersionLabel || (props.formula.draftVersionNo ? `V${props.formula.draftVersionNo}` : '-'))
</script>

<style scoped>
.workbench-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fff;
}

.workbench-header__main {
  min-width: 0;
}

.workbench-header__breadcrumb {
  color: #667085;
  font-size: 13px;
}

.workbench-header__title,
.workbench-header__actions,
.workbench-header__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.workbench-header__title h2 {
  margin: 8px 2px 6px 0;
  color: #1d2129;
  font-size: 22px;
  line-height: 1.25;
}

.workbench-header__meta {
  color: #475467;
  font-size: 13px;
}

.workbench-header__actions {
  justify-content: flex-end;
  align-content: flex-start;
  min-width: 460px;
}
</style>
