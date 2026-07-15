<template>
  <div class="app-container platform-operations-page">
    <header class="platform-operations-page__toolbar"><span v-if="summary?.dataAsOf">{{ t('dashboard.operations.dataAsOf') }} {{ formatUtc(summary.dataAsOf, 'YYYY-MM-DD HH:mm') }}</span><el-button plain :icon="Refresh" :loading="summaryLoading" @click="refresh">{{ t('dashboard.operations.refresh') }}</el-button></header>
    <OperationsSummaryCards :summary="summary" @filter="applyCardFilter" />
    <section class="platform-operations-page__workspace"><div class="platform-operations-page__list"><el-form :inline="true" class="platform-operations-page__filters" @submit.prevent="search"><el-form-item><el-input v-model="query.keyword" :placeholder="t('dashboard.operations.searchPlaceholder')" clearable :prefix-icon="Search" @keyup.enter="search" /></el-form-item><template v-if="source === 'profile'"><el-form-item><el-select v-model="query.status" :placeholder="t('dashboard.operations.statusFilter')" clearable @change="search"><el-option :label="t('dashboard.operations.allStatuses')" value="" /><el-option :label="t('common.enabled')" value="1" /><el-option :label="t('common.disabled')" value="0" /></el-select></el-form-item><el-form-item><el-select v-model="query.levelCode" :placeholder="t('dashboard.operations.levelFilter')" clearable @change="search"><el-option :label="t('dashboard.operations.allLevels')" value="" /><el-option v-for="level in levels" :key="level.levelId" :label="level.levelName" :value="level.levelCode" /></el-select></el-form-item></template><el-form-item><el-button @click="resetFilters">{{ t('common.reset') }}</el-button></el-form-item></el-form><p v-if="source === 'application'" class="platform-operations-page__mode"><el-tag type="warning">{{ t('dashboard.operations.pendingApplications') }}</el-tag><el-button link type="primary" @click="showAllMerchants">{{ t('dashboard.operations.viewAllMerchants') }}</el-button></p><OperationsMerchantTable :rows="rows" :loading="rowsLoading" :total="total" :page="query.pageNum || 1" :page-size="query.pageSize || 10" @select="selectMerchant" @pagination="paginate" /></div><OperationsMerchantDetail :merchant="selected" @close="selected = undefined" @approve="approve" @reject="openReject" /></section>
    <OperationsOrderPipeline :summary="summary" @open-orders="openOrders" />
    <AdminDialog v-model="rejectVisible" :title="t('tenant.reject')" width="460px" :close-on-click-modal="false"><el-input v-model="rejectReason" type="textarea" :rows="4" :placeholder="t('tenant.rejectReasonPlaceholder')" /><template #footer><AdminDialogFooter><el-button @click="rejectVisible = false">{{ t('common.cancel') }}</el-button><el-button type="primary" @click="reject">{{ t('common.confirm') }}</el-button></AdminDialogFooter></template></AdminDialog>
  </div>
</template>

<script setup lang="ts" name="PlatformOperationsOverviewPage">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { approveMerchantApplication, rejectMerchantApplication } from '@/api/merchant/application'
import { platformOperationsApi, type OperationsLevelOption, type OperationsMerchant, type OperationsMerchantQuery, type OperationsSummary } from '@/api/platform-operations'
import { formatUtc } from '@/utils/datetime'
import { resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'
import { usePermissionStore } from '@/stores/permission'
import OperationsMerchantDetail from './components/OperationsMerchantDetail.vue'
import OperationsMerchantTable from './components/OperationsMerchantTable.vue'
import OperationsOrderPipeline from './components/OperationsOrderPipeline.vue'
import OperationsSummaryCards from './components/OperationsSummaryCards.vue'

const { t } = useI18n()
const router = useRouter()
const permissionStore = usePermissionStore()
const summary = ref<OperationsSummary>()
const rows = ref<OperationsMerchant[]>([])
const levels = ref<OperationsLevelOption[]>([])
const selected = ref<OperationsMerchant>()
const summaryLoading = ref(false)
const rowsLoading = ref(false)
const total = ref(0)
const source = ref<'profile' | 'application'>('profile')
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref<OperationsMerchant>()
const query = reactive<OperationsMerchantQuery>({ pageNum: 1, pageSize: 10, keyword: '', status: '', levelCode: '' })

async function loadSummary() { summaryLoading.value = true; try { summary.value = (await platformOperationsApi.summary()).data } finally { summaryLoading.value = false } }
async function loadLevels() { levels.value = (await platformOperationsApi.levelOptions('ENABLED')).data || [] }
async function loadRows() {
  rowsLoading.value = true
  try {
    if (source.value === 'profile') {
      const response = await platformOperationsApi.merchants(query)
      rows.value = response.rows || []
      total.value = response.total || 0
    } else {
      const response = await platformOperationsApi.applications({ pageNum: query.pageNum, pageSize: query.pageSize, keyword: query.keyword })
      rows.value = response.rows || []
      total.value = response.total || 0
    }
    if (!selected.value || !rows.value.some((row) => identity(row) === identity(selected.value))) selected.value = rows.value[0]
  } finally { rowsLoading.value = false }
}
function identity(row?: OperationsMerchant) { return row?.source === 'application' ? `apply-${row.applicationId}` : `merchant-${row?.merchantId}` }
async function refresh() { await Promise.all([loadSummary(), loadRows()]) }
function resetFilters() { Object.assign(query, { pageNum: 1, keyword: '', status: '', levelCode: '' }); void loadRows() }
function search() { query.pageNum = 1; void loadRows() }
function paginate(payload: { page: number; limit: number }) { query.pageNum = payload.page; query.pageSize = payload.limit; void loadRows() }
function selectMerchant(row: OperationsMerchant) { selected.value = row }
function applyCardFilter(key: 'pending' | 'enabled' | 'disabled' | 'vip') { source.value = key === 'pending' ? 'application' : 'profile'; Object.assign(query, { pageNum: 1, keyword: '', status: key === 'enabled' ? '1' : key === 'disabled' ? '0' : '', levelCode: key === 'vip' ? 'VIP' : '' }); selected.value = undefined; void loadRows() }
function showAllMerchants() { source.value = 'profile'; resetFilters() }
async function approve(merchant: OperationsMerchant) { if (!merchant.applicationId) return; try { await ElMessageBox.confirm(t('tenant.approveConfirm'), t('common.prompt'), { type: 'warning' }) } catch { return } await approveMerchantApplication(merchant.applicationId); ElMessage.success(t('tenant.approveSuccess')); selected.value = undefined; await refresh() }
function openReject(merchant: OperationsMerchant) { rejectTarget.value = merchant; rejectReason.value = t('tenant.rejectDefaultReason'); rejectVisible.value = true }
async function reject() { if (!rejectTarget.value?.applicationId) return; await rejectMerchantApplication(rejectTarget.value.applicationId, rejectReason.value); ElMessage.success(t('tenant.rejectSuccess')); rejectVisible.value = false; selected.value = undefined; await refresh() }
function openOrders() { void router.push({ path: resolveRoutePath(permissionStore.routers, 'platform-sales/orders', '/salesOperations/orders') }) }

onMounted(() => { void Promise.all([loadSummary(), loadLevels(), loadRows()]) })
</script>

<style scoped>
.platform-operations-page { display: grid; gap: 12px; padding: 12px; background: var(--admin-bg); }.platform-operations-page__toolbar { display: flex; min-height: 36px; align-items: center; justify-content: flex-end; gap: 12px; color: #667085; font-size: 12px; }.platform-operations-page__workspace { display: grid; grid-template-columns: minmax(0, 1fr) minmax(320px, 352px); gap: 12px; align-items: stretch; }.platform-operations-page__list { min-width: 0; padding: 14px; border: 1px solid #e7ecf4; border-radius: 12px; background: #fff; }.platform-operations-page__filters { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }.platform-operations-page__filters :deep(.el-form-item) { margin: 0; }.platform-operations-page__filters :deep(.el-input) { width: 270px; }.platform-operations-page__filters :deep(.el-select) { width: 140px; }.platform-operations-page__mode { display: flex; align-items: center; gap: 8px; margin: -2px 0 10px; }
@media (max-width: 1200px) { .platform-operations-page__workspace { grid-template-columns: 1fr; }.platform-operations-page__list { order: 1; }.platform-operations-page :deep(.operations-detail) { min-height: auto; } }
@media (max-width: 640px) { .platform-operations-page__toolbar { justify-content: space-between; }.platform-operations-page__filters :deep(.el-input),.platform-operations-page__filters :deep(.el-select) { width: min(100%, 280px); } }
</style>
