<template>
  <AdminDrawer v-model="open" :title="title" size="720px" variant="detail" append-to-body>
    <el-table v-if="displayRows.length" v-loading="loading" :data="displayRows" border class="formula-review-record">
      <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
      <el-table-column :label="t('productCenter.formulaReview.recordAction')" width="136">
        <template #default="{ row }">
          <el-tag :type="actionTagType(row.actionType)" effect="plain">{{ row.actionName || actionText(row.actionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formula.currentVersion')" prop="versionLabel" width="96" />
      <el-table-column :label="t('productCenter.formulaReview.recordOperator')" prop="operatorName" width="128" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.formulaReview.recordTime')" width="152" align="center">
        <template #default="{ row }">{{ formatFormulaReviewMinute(row.operateTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaReview.recordRemark')" min-width="260" prop="remark" show-overflow-tooltip />
    </el-table>
    <el-empty v-else v-loading="loading" :description="t('productCenter.formulaReview.recordEmpty')" />
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaReviewRecordVO } from '@/api/product-capability/types'
import { formatFormulaReviewMinute } from '../utils/formulaReviewDisplay'

const props = defineProps<{
  modelValue: boolean
  rows: ProductFormulaReviewRecordVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const title = computed(() => t('productCenter.formulaReview.reviewRecord'))
const open = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const displayRows = computed(() => props.rows)

function actionText(actionType?: string) {
  const map: Record<string, string> = {
    SUBMIT_REVIEW: t('productCenter.formulaReview.actionSubmitReview'),
    APPROVE_VERSION: t('productCenter.formulaReview.actionApproveVersion'),
    REJECT: t('productCenter.formulaReview.actionReject')
  }
  return map[actionType || ''] || actionType || '-'
}

function actionTagType(actionType?: string) {
  if (actionType?.includes('FAIL') || actionType === 'REJECT') return 'danger'
  if (actionType === 'SUBMIT_REVIEW') return 'warning'
  if (actionType?.includes('PASS') || actionType === 'APPROVE_VERSION') return 'success'
  return 'info'
}
</script>
