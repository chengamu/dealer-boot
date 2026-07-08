<template>
  <section class="version-panel">
    <div class="version-panel__head">
      <div>
        <h3>{{ t('productCenter.formulaWorkbench.versionHistory') }}</h3>
        <p>{{ t('productCenter.formulaWorkbench.versionHistoryDesc') }}</p>
      </div>
      <el-button icon="Refresh" :loading="loading" @click="$emit('refresh')">{{ t('common.refresh') }}</el-button>
    </div>

    <el-empty v-if="!versions.length && !loading" :description="t('productCenter.formulaWorkbench.versionEmpty')" />
    <div v-else class="version-list" v-loading="loading">
      <article v-for="version in versions" :key="version.versionId" class="version-card">
        <div class="version-card__main">
          <strong>{{ version.versionLabel || `V${version.versionNo || '-'}` }}</strong>
          <el-tag :type="formulaStatusTagType(version.versionStatus)" effect="plain">
            {{ formulaStatusText(version.versionStatus, t) }}
          </el-tag>
          <el-tag :type="formulaValidationTagType(version.validationStatus)" effect="plain">
            {{ formulaValidationStatusText(version.validationStatus, t) }}
          </el-tag>
        </div>
        <div class="version-card__meta">
          <span>{{ t('productCenter.formulaReview.submitBy') }}：{{ version.submitBy || '-' }}</span>
          <span>{{ t('productCenter.formulaReview.submitTime') }}：{{ formatFormulaReviewMinute(version.submitTime) }}</span>
          <span>{{ t('productCenter.formula.auditBy') }}：{{ version.auditBy || '-' }}</span>
          <span>{{ t('productCenter.formula.auditTime') }}：{{ formatFormulaReviewMinute(version.auditTime) }}</span>
        </div>
        <div v-if="version.rejectReason" class="version-card__reason">
          {{ t('productCenter.formula.rejectReason') }}：{{ version.rejectReason }}
        </div>
        <div class="version-card__actions">
          <el-button type="primary" plain icon="View" @click="$emit('view', version)">
            {{ t('productCenter.formulaWorkbench.viewSnapshot') }}
          </el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ProductFormulaVersionVO } from '@/api/product-capability/types'
import {
  formulaStatusTagType,
  formulaStatusText,
  formulaValidationStatusText,
  formulaValidationTagType
} from '@/constants/productStatus'
import { formatFormulaReviewMinute } from '../utils/formulaReviewDisplay'

defineProps<{
  versions: ProductFormulaVersionVO[]
  loading?: boolean
  t: (key: string) => string
}>()

defineEmits<{
  refresh: []
  view: [version: ProductFormulaVersionVO]
}>()
</script>

<style scoped>
.version-panel {
  display: grid;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fff;
}

.version-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.version-panel h3,
.version-panel p {
  margin: 0;
}

.version-panel h3 {
  color: #1d2129;
  font-size: 16px;
}

.version-panel p {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.version-list {
  display: grid;
  gap: 10px;
  min-height: 160px;
}

.version-card {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid #eef0f5;
  border-radius: 8px;
  background: #fbfcff;
}

.version-card__main,
.version-card__meta,
.version-card__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.version-card__main strong {
  color: #1d2129;
  font-size: 15px;
}

.version-card__meta,
.version-card__reason {
  color: #667085;
  font-size: 12px;
}
</style>
