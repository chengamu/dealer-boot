<template>
  <div class="app-container formula-review-detail-page">
    <section class="review-detail-header">
      <div>
        <div class="review-detail-header__breadcrumb">{{ t('productCenter.formulaReview.title') }} / {{ t('productCenter.formulaReview.detailTitle') }}</div>
        <h2>{{ formula.formulaName || '-' }}</h2>
        <div class="review-detail-header__meta">
          <span>{{ t('productCenter.formula.code') }}：{{ formula.formulaCode || '-' }}</span>
          <span>{{ t('productCenter.formula.category') }}：{{ formula.categoryNameCn || '-' }}</span>
          <span>{{ t('productCenter.formula.productType') }}：{{ formula.productTypeNameCn || '-' }}</span>
          <span>{{ t('productCenter.formula.sizeSummary') }}：{{ formula.sizeSummary || '-' }}</span>
          <span>{{ t('productCenter.formulaReview.submitBy') }}：{{ review.submitBy || '-' }}</span>
          <span>{{ t('productCenter.formulaReview.submitTime') }}：{{ formatFormulaReviewMinute(review.submitTime) }}</span>
        </div>
      </div>
      <div class="review-detail-header__actions">
        <el-button icon="Back" @click="router.back()">{{ t('common.back') }}</el-button>
        <el-button type="primary" plain icon="CircleCheck" @click="approve" v-hasPermi="['product:formula:approve']">
          {{ t('productCenter.formula.actions.approve') }}
        </el-button>
        <el-button type="danger" plain icon="CircleClose" @click="reject" v-hasPermi="['product:formula:reject']">
          {{ t('productCenter.formula.actions.reject') }}
        </el-button>
      </div>
    </section>

    <section class="review-detail-panel">
      <div class="section-title">{{ t('productCenter.formulaReview.materialDetail') }}</div>
      <FormulaReviewMaterialSummary
        :loading="loading"
        :materials="materials"
        :usage-rules="allUsageRules"
        :option-materials="allOptionMaterials"
        :price-snapshot-rows="priceSnapshotRows"
        :unit-rows="unitRows"
        :t="t"
      />
    </section>

    <section class="review-detail-panel">
      <div class="section-title">{{ t('productCenter.formulaReview.reviewRecord') }}</div>
      <div class="review-record">
        <span>{{ t('productCenter.formula.currentVersion') }}：{{ review.versionLabel || '-' }}</span>
        <span>
          {{ t('productCenter.formula.status') }}：
          <el-tag :type="formulaStatusTagType(review.versionStatus)" effect="plain">
            {{ formulaStatusText(review.versionStatus, t) }}
          </el-tag>
        </span>
        <span>
          {{ t('productCenter.formula.validationStatus') }}：
          <el-tag :type="formulaValidationTagType(review.validationStatus)" effect="plain">
            {{ formulaValidationStatusText(review.validationStatus, t) }}
          </el-tag>
        </span>
        <span>{{ t('productCenter.formula.auditBy') }}：{{ review.auditBy || '-' }}</span>
        <span>{{ t('productCenter.formula.auditTime') }}：{{ formatFormulaReviewMinute(review.auditTime) }}</span>
        <span>{{ t('productCenter.formula.rejectReason') }}：{{ review.rejectReason || '-' }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productUnitApi } from '@/api/product-capability/base'
import { productFormulaApi } from '@/api/product-formula/formula'
import {
  PRODUCT_STATUS_ENABLED,
  formulaStatusTagType,
  formulaStatusText,
  formulaValidationStatusText,
  formulaValidationTagType
} from '@/constants/productStatus'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVersionVO,
  ProductUnitVO
} from '@/api/product-capability/types'
import FormulaReviewMaterialSummary from './components/FormulaReviewMaterialSummary.vue'
import { formatFormulaReviewMinute, parseFormulaReviewJson } from './utils/formulaReviewDisplay'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const loading = ref(false)
const review = ref<ProductFormulaVersionVO>({})
const unitRows = ref<ProductUnitVO[]>([])
const reviewId = String(route.params.reviewId || '')

const formula = computed(() => parseFormulaReviewJson(review.value.formulaSnapshotJson))
const setup = computed(() => parseFormulaReviewJson(review.value.setupSnapshotJson))
const materials = computed(() => (setup.value.materials || []) as ProductFormulaMaterialVO[])
const allUsageRules = computed(() => (setup.value.usageRules || []) as ProductFormulaUsageRuleVO[])
const allOptionMaterials = computed(() => (setup.value.optionMaterials || []) as ProductFormulaOptionMaterialVO[])
const priceSnapshotRows = computed(() => (setup.value.priceSnapshot || []) as Array<{ materialCode?: string; unitPrice?: number; salesPrice?: number }>)

async function load() {
  if (!reviewId) return
  loading.value = true
  try {
    const [response, unitResponse] = await Promise.all([
      productFormulaApi.review(reviewId),
      productUnitApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
    ])
    review.value = response.data || {}
    unitRows.value = Array.isArray(unitResponse) ? unitResponse : unitResponse?.data || []
  } finally {
    loading.value = false
  }
}

async function approve() {
  await ElMessageBox.confirm(t('productCenter.formula.confirm.approve'), t('common.prompt'), { type: 'warning' })
  await productFormulaApi.approveReview(reviewId)
  ElMessage.success(t('common.success'))
  await router.push('/product-formula/reviews')
}

async function reject() {
  const result = await ElMessageBox.prompt(t('productCenter.formula.rejectPrompt'), t('productCenter.formula.actions.reject'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputType: 'textarea',
    inputValidator: (value) => Boolean(value && value.trim()),
    inputErrorMessage: t('productCenter.formula.rejectReasonRequired')
  })
  await productFormulaApi.rejectReview(reviewId, result.value.trim())
  ElMessage.success(t('common.success'))
  await router.push('/product-formula/reviews')
}

onMounted(load)
</script>

<style scoped>
.formula-review-detail-page {
  display: grid;
  gap: 12px;
}

.review-detail-header,
.review-detail-panel {
  padding: 12px;
  background: #fff;
  border: 1px solid #eef0f5;
  border-radius: 8px;
}

.review-detail-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.review-detail-header h2 {
  margin: 6px 0;
  color: #1d2129;
  font-size: 20px;
  line-height: 1.3;
}

.review-detail-header__breadcrumb,
.review-detail-header__meta {
  color: #6b7280;
  font-size: 13px;
}

.review-detail-header__meta,
.review-detail-header__actions,
.review-record {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
}

.review-detail-header__actions {
  justify-content: flex-end;
  align-items: flex-start;
}

.review-detail-header__actions :deep(.el-button) {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
}

.section-title {
  margin-bottom: 10px;
  color: #1d2129;
  font-size: 15px;
  font-weight: 600;
}

.review-detail-panel :deep(.el-table) {
  border-color: #eef0f5;
}

.review-detail-panel :deep(.el-table__header th) {
  background: #f7f9fc;
  color: #344054;
  font-weight: 600;
}

.review-detail-panel :deep(th.el-table__cell),
.review-detail-panel :deep(td.el-table__cell) {
  border-color: #eef0f5;
}

.review-detail-panel :deep(.el-table__cell) {
  padding: 8px 0;
}

.review-detail-panel :deep(.el-table__body tr:hover > td.el-table__cell) {
  background: #f8fbff;
}

.review-record {
  color: #475467;
  font-size: 13px;
}
</style>
