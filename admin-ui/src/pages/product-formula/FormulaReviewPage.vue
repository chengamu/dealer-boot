<template>
  <div class="app-container formula-review-page">
    <section class="review-panel">
      <div class="review-header">
        <div>
          <h2>{{ t('productCenter.formulaReview.title') }}</h2>
          <p>{{ t('productCenter.formulaReview.description') }}</p>
        </div>
        <el-button icon="Refresh" @click="load">{{ t('common.refresh') }}</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column :label="t('productCenter.formula.code')" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ formulaSnapshot(row).formulaCode || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formula.name')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ formulaSnapshot(row).formulaName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="versionLabel" :label="t('productCenter.formula.currentVersion')" width="120" />
        <el-table-column prop="submitBy" :label="t('productCenter.formulaReview.submitBy')" width="120" />
        <el-table-column :label="t('productCenter.formulaReview.submitTime')" width="160">
          <template #default="{ row }">{{ formatMinute(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column prop="validationStatus" :label="t('productCenter.formula.validationStatus')" width="120" />
        <el-table-column :label="t('common.operate')" width="180" fixed="right" align="center" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <AdminTableActions :actions="reviewActions(row)" />
          </template>
        </el-table-column>
      </el-table>
      <div class="review-pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productFormulaApi } from '@/api/product-formula/formula'
import { formatUtc } from '@/utils/datetime'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { ProductFormulaVersionVO, ProductRecord } from '@/api/product-capability/types'

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const loading = ref(false)
const rows = ref<ProductFormulaVersionVO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

function parseJson(value?: string) {
  if (!value) return {}
  try {
    return JSON.parse(value) as ProductRecord
  } catch {
    return {}
  }
}

function formulaSnapshot(row: ProductFormulaVersionVO) {
  return parseJson(row.formulaSnapshotJson)
}

function formatMinute(value?: string) {
  return formatUtc(value, 'YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    const response = await productFormulaApi.reviews(query)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row: ProductFormulaVersionVO) {
  if (!row.versionId) return
  await router.push(`/product-formula/reviews/${row.versionId}`)
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
    { label: t('common.detail'), icon: 'View', primary: true, onClick: () => openDetail(row) },
    { label: t('productCenter.formula.actions.approve'), icon: 'CircleCheck', type: 'success', onClick: () => approve(row) },
    { label: t('productCenter.formula.actions.reject'), icon: 'Close', type: 'warning', onClick: () => reject(row) }
  ]
}

onMounted(load)
</script>

<style scoped>
.review-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.review-header h2 {
  margin: 0 0 4px;
  color: #111827;
}

.review-header p {
  margin: 0;
  color: #6b7280;
}

.review-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
