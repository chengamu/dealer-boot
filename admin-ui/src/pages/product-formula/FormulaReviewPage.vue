<template>
  <div class="app-container formula-review-page">
    <el-form
      v-show="showSearch"
      ref="queryRef"
      :model="query"
      :inline="true"
      label-width="72px"
      class="formula-review-page__search"
    >
      <el-form-item :label="t('productCenter.formula.code')" prop="formulaCode">
        <el-input
          v-model="query.formulaCode"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 190px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('productCenter.formula.name')" prop="formulaName">
        <el-input
          v-model="query.formulaName"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 190px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('productCenter.formula.currentVersion')" prop="versionLabel">
        <el-input
          v-model="query.versionLabel"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('productCenter.formulaReview.submitBy')" prop="submitBy">
        <el-input
          v-model="query.submitBy"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('productCenter.formula.validationStatus')" prop="validationStatus">
        <el-select
          v-model="query.validationStatus"
          :placeholder="t('productCenter.common.selectPlaceholder')"
          clearable
          filterable
          style="width: 160px"
        >
          <el-option v-for="option in validationStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery" v-hasPermi="['product:formula:review']">
          {{ t('common.search') }}
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 formula-review-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="View" :disabled="!currentRow" @click="handleCurrentDetail" v-hasPermi="['product:formula:review']">
          {{ t('common.detail') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="CircleCheck" :disabled="!currentRow" @click="handleCurrentApprove" v-hasPermi="['product:formula:approve']">
          {{ t('productCenter.formula.actions.approve') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="CircleClose" :disabled="!currentRow" @click="handleCurrentReject" v-hasPermi="['product:formula:reject']">
          {{ t('productCenter.formula.actions.reject') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Clock" :disabled="!currentRow" @click="handleCurrentReviewRecord" v-hasPermi="['product:formula:reference']">
          {{ t('productCenter.formulaReview.reviewRecord') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      highlight-current-row
      class="formula-review-page__table"
      @row-click="handleRowClick"
      @row-dblclick="openDetail"
    >
      <el-table-column type="index" :index="rowIndex" :label="t('common.index')" width="64" align="center" fixed />
      <el-table-column :label="t('productCenter.formula.code')" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ parseFormulaReviewJson(row.formulaSnapshotJson).formulaCode || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formula.name')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ parseFormulaReviewJson(row.formulaSnapshotJson).formulaName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="versionLabel" :label="t('productCenter.formula.currentVersion')" width="120" />
      <el-table-column :label="t('productCenter.formula.status')" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="formulaStatusTagType(row.versionStatus)" effect="plain">
            {{ formulaStatusText(row.versionStatus, t) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitBy" :label="t('productCenter.formulaReview.submitBy')" width="120" />
      <el-table-column :label="t('productCenter.formulaReview.submitTime')" width="160">
        <template #default="{ row }">{{ formatFormulaReviewMinute(row.submitTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formula.validationStatus')" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="formulaValidationTagType(row.validationStatus)" effect="plain">
            {{ formulaValidationStatusText(row.validationStatus, t) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="180" fixed="right" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="reviewActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      class="formula-review-page__pagination"
      @pagination="load"
    />
    <FormulaReviewRecordDrawer v-model="reviewRecordOpen" :rows="reviewRecordRows" :loading="reviewRecordLoading" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productFormulaApi } from '@/api/product-formula/formula'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { ProductFormulaReviewQuery, ProductFormulaReviewRecordVO, ProductFormulaVersionVO } from '@/api/product-capability/types'
import FormulaReviewRecordDrawer from './components/FormulaReviewRecordDrawer.vue'
import {
  formulaValidationStatusOptions,
  formulaStatusTagType,
  formulaStatusText,
  formulaValidationStatusText,
  formulaValidationTagType
} from '@/constants/productStatus'
import { formatFormulaReviewMinute, parseFormulaReviewJson } from './utils/formulaReviewDisplay'
import './FormulaReviewPage.css'

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const loading = ref(false)
const rows = ref<ProductFormulaVersionVO[]>([])
const total = ref(0)
const showSearch = ref(true)
const queryRef = ref()
const currentRow = ref<ProductFormulaVersionVO>()
const reviewRecordOpen = ref(false)
const reviewRecordLoading = ref(false)
const reviewRecordRows = ref<ProductFormulaReviewRecordVO[]>([])
const validationStatusOptions = computed(() => formulaValidationStatusOptions(t))
const query = reactive<ProductFormulaReviewQuery>({ pageNum: 1, pageSize: 10 })

async function load() {
  loading.value = true
  try {
    const response = await productFormulaApi.reviews(query)
    rows.value = response.rows || []
    total.value = response.total || 0
    currentRow.value = undefined
  } finally {
    loading.value = false
  }
}

function rowIndex(index: number) {
  return ((query.pageNum || 1) - 1) * (query.pageSize || 10) + index + 1
}

function handleQuery() {
  query.pageNum = 1
  load()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function handleRowClick(row: ProductFormulaVersionVO) {
  currentRow.value = row
}

async function openDetail(row: ProductFormulaVersionVO) {
  if (!row.versionId) return
  await router.push(`/product-formula/reviews/${row.versionId}`)
}

function handleCurrentDetail() {
  if (currentRow.value) {
    void openDetail(currentRow.value)
  }
}

function handleCurrentApprove() {
  if (currentRow.value) {
    void approve(currentRow.value)
  }
}

function handleCurrentReject() {
  if (currentRow.value) {
    void reject(currentRow.value)
  }
}

async function handleCurrentReviewRecord() {
  if (!currentRow.value?.versionId) return
  reviewRecordOpen.value = true
  reviewRecordLoading.value = true
  try {
    const response = await productFormulaApi.reviewRecords(currentRow.value.versionId)
    reviewRecordRows.value = response.data || []
  } finally {
    reviewRecordLoading.value = false
  }
}

async function approve(row: ProductFormulaVersionVO) {
  if (!row.versionId) return
  await ElMessageBox.confirm(t('productCenter.formula.confirm.approve'), t('common.prompt'), { type: 'warning' })
  await productFormulaApi.approveReview(row.versionId)
  ElMessage.success(t('common.success'))
  await load()
}

async function reject(row: ProductFormulaVersionVO) {
  if (!row.versionId) return
  const result = await ElMessageBox.prompt(t('productCenter.formula.rejectPrompt'), t('productCenter.formula.actions.reject'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputType: 'textarea',
    inputValidator: (value) => Boolean(value && value.trim()),
    inputErrorMessage: t('productCenter.formula.rejectReasonRequired')
  })
  await productFormulaApi.rejectReview(row.versionId, result.value.trim())
  ElMessage.success(t('common.success'))
  await load()
}

function reviewActions(row: ProductFormulaVersionVO): AdminTableAction[] {
  return [
    { label: t('productCenter.formula.actions.approve'), icon: 'CircleCheck', permission: 'product:formula:approve', type: 'primary', onClick: () => approve(row) },
    { label: t('productCenter.formula.actions.reject'), icon: 'Close', permission: 'product:formula:reject', type: 'danger', danger: true, onClick: () => reject(row) }
  ]
}

onMounted(load)
</script>
