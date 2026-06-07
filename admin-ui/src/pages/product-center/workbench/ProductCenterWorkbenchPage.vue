<template>
  <div class="app-container product-workbench">
    <section class="product-workbench__hero">
      <div>
        <p>{{ t('productCenter.menu.title') }}</p>
        <h1>{{ t('productCenter.workbench.title') }}</h1>
        <span>{{ t('productCenter.workbench.description') }}</span>
      </div>
      <el-space wrap>
        <span class="product-workbench__sync-time">{{ t('productCenter.workbench.lastSync') }}：{{ formatUtc(summary.lastSyncTime) }}</span>
        <el-button type="primary" icon="Refresh" @click="loadAll">{{ t('common.refresh') }}</el-button>
        <el-button icon="Plus" v-hasPermi="['product:model:add']">{{ t('productCenter.quick.newModel') }}</el-button>
      </el-space>
    </section>

    <section class="product-workbench__task-flow">
      <div class="product-workbench__section-head">
        <div>
          <h2>{{ t('productCenter.workbench.taskFlow') }}</h2>
          <p>{{ t('productCenter.workbench.taskFlowHint') }}</p>
        </div>
      </div>
      <task-flow-cards :items="stats" @select="handleStatSelect" />
    </section>

    <el-row :gutter="12" class="product-workbench__body">
      <el-col :xs="24" :xl="17">
        <product-progress-table
          :rows="progressRows"
          :loading="progressLoading"
          :status-tone="statusTone"
          @open-row="openProgressRow"
          @batch-action="openGapTasks"
          @export="openPublishPackages"
        />
      </el-col>
      <el-col :xs="24" :xl="7">
        <priority-queue-panel :rows="priorityRows" :loading="priorityLoading" :status-tone="statusTone" @open-task="openPriorityTask" @view-more="openGapTasks" />
        <quick-action-grid :actions="quickActions" @run="runQuickAction" />
        <recent-sync-events :rows="syncRows" :loading="syncLoading" :status-tone="statusTone" @open-event="openSyncEvent" @view-more="openSyncLog" />
      </el-col>
    </el-row>

    <product-workbench-trend :summary="summary" @open-gaps="openGapsBySeverity" @open-sync-log="openSyncLog" />
  </div>
</template>

<script setup lang="ts" name="ProductCenterWorkbenchPage">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import { getWorkbenchSummary, listWorkbenchPriority, listWorkbenchProgress, listWorkbenchSyncEvents } from '@/api/product-capability/workbench'
import type { WorkbenchPriority, WorkbenchProgress, WorkbenchSummary, WorkbenchSyncEvent } from '@/api/product-capability/types'
import TaskFlowCards, { type TaskFlowItem } from '@/pages/product-center/components/TaskFlowCards.vue'
import ProductProgressTable from '@/pages/product-center/components/ProductProgressTable.vue'
import PriorityQueuePanel from '@/pages/product-center/components/PriorityQueuePanel.vue'
import QuickActionGrid, { type QuickAction } from '@/pages/product-center/components/QuickActionGrid.vue'
import RecentSyncEvents from '@/pages/product-center/components/RecentSyncEvents.vue'
import ProductWorkbenchTrend from '@/pages/product-center/components/ProductWorkbenchTrend.vue'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)

const summary = ref<WorkbenchSummary>({})
const progressRows = ref<WorkbenchProgress[]>([])
const priorityRows = ref<WorkbenchPriority[]>([])
const syncRows = ref<WorkbenchSyncEvent[]>([])
const summaryLoading = ref(false)
const progressLoading = ref(false)
const priorityLoading = ref(false)
const syncLoading = ref(false)

const stats = computed<TaskFlowItem[]>(() => [
  { key: 'modelCount', labelKey: 'productCenter.workbench.modelCount', value: summary.value.modelCount ?? 0, icon: 'M', tone: 'blue' },
  { key: 'draftCount', labelKey: 'productCenter.workbench.draftCount', value: summary.value.draftCount ?? 0, icon: 'D', tone: 'slate' },
  { key: 'publishedCount', labelKey: 'productCenter.workbench.publishedCount', value: summary.value.publishedCount ?? 0, icon: 'P', tone: 'green' },
  { key: 'blockerCount', labelKey: 'productCenter.workbench.blockerCount', value: summary.value.blockerCount ?? 0, icon: '!', tone: 'red' },
  { key: 'warningCount', labelKey: 'productCenter.workbench.warningCount', value: summary.value.warningCount ?? 0, icon: 'W', tone: 'amber' },
  { key: 'pendingSyncCount', labelKey: 'productCenter.workbench.pendingSyncCount', value: summary.value.pendingSyncCount ?? 0, icon: 'S', tone: 'blue' }
])

const quickActions: QuickAction[] = [
  { key: 'newModel', labelKey: 'productCenter.quick.newModel', icon: 'Box', permission: 'product:model:add' },
  { key: 'batchPaste', labelKey: 'productCenter.template.batchPaste', icon: 'DocumentCopy', permission: 'product:template:edit' },
  { key: 'calculate', labelKey: 'productCenter.price.calculate', icon: 'Money', permission: 'product:price:edit' },
  { key: 'publishCheck', labelKey: 'productCenter.publish.check', icon: 'UploadFilled', permission: 'product:publish:check' }
]

function statusTone(status?: string) {
  const normalized = String(status || '').toUpperCase()
  if (normalized.includes('BLOCK') || normalized.includes('ERROR') || normalized.includes('FAIL') || normalized.includes('MISSING')) return 'danger'
  if (normalized.includes('WARN') || normalized.includes('PEND') || normalized.includes('PARTIAL')) return 'warning'
  if (normalized.includes('PASS') || normalized.includes('SUCCESS') || normalized.includes('COMPLETE')) return 'success'
  return 'info'
}

function push(path: string, query?: Record<string, string>) {
  router.push({ path, query })
}

function handleStatSelect(key: string) {
  if (key === 'blockerCount' || key === 'warningCount') {
    push('/product-release/gap-tasks', { severity: key === 'blockerCount' ? 'BLOCKER' : 'WARNING' })
    return
  }
  if (key === 'pendingSyncCount') {
    openSyncLog()
    return
  }
  if (key === 'publishedCount') {
    openPublishPackages()
    return
  }
  push('/product-config/models')
}

function openProgressRow(row: WorkbenchProgress) {
  const query = row.modelCode ? { modelCode: row.modelCode } : undefined
  if (String(row.templateStatus || '').toUpperCase().includes('MISSING')) {
    push('/product-config/template', query)
    return
  }
  if (String(row.priceStatus || '').toUpperCase().includes('MISSING')) {
    push('/product-config/pricing', query)
    return
  }
  if (String(row.assetStatus || '').toUpperCase().includes('MISSING')) {
    push('/product-master/media-assets', query)
    return
  }
  push('/product-release/publish', query)
}

function openPriorityTask(row: WorkbenchPriority) {
  push('/product-release/gap-tasks', {
    targetCode: String(row.targetCode || ''),
    severity: String(row.severity || '')
  })
}

function openGapTasks() {
  push('/product-release/gap-tasks')
}

function openGapsBySeverity(severity: 'BLOCKER' | 'WARNING') {
  push('/product-release/gap-tasks', { severity })
}

function openPublishPackages() {
  push('/product-release/packages')
}

function openSyncLog() {
  push('/product-release/sync-outbox')
}

function openSyncEvent(row: WorkbenchSyncEvent) {
  push('/product-release/sync-outbox', {
    targetCode: String(row.targetCode || ''),
    syncStatus: String(row.status || '')
  })
}

function runQuickAction(key: string) {
  const routeMap: Record<string, string> = {
    newModel: '/product-config/models',
    batchPaste: '/product-config/template',
    calculate: '/product-config/pricing',
    publishCheck: '/product-release/publish'
  }
  const path = routeMap[key]
  if (!path) {
    ElMessage.info(t('productCenter.common.empty'))
    return
  }
  push(path)
}

async function loadSummary() {
  summaryLoading.value = true
  try {
    const response = await getWorkbenchSummary()
    summary.value = response.data || {}
  } finally {
    summaryLoading.value = false
  }
}

async function loadProgress() {
  progressLoading.value = true
  try {
    const response = await listWorkbenchProgress({ pageNum: 1, pageSize: 8 })
    progressRows.value = response.rows || []
  } finally {
    progressLoading.value = false
  }
}

async function loadPriority() {
  priorityLoading.value = true
  try {
    const response = await listWorkbenchPriority({ pageNum: 1, pageSize: 8 })
    priorityRows.value = response.rows || []
  } finally {
    priorityLoading.value = false
  }
}

async function loadSyncEvents() {
  syncLoading.value = true
  try {
    const response = await listWorkbenchSyncEvents({ pageNum: 1, pageSize: 8 })
    syncRows.value = response.rows || []
  } finally {
    syncLoading.value = false
  }
}

function loadAll() {
  loadSummary()
  loadProgress()
  loadPriority()
  loadSyncEvents()
}

loadAll()
</script>

<style scoped lang="scss">
.product-workbench {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #111827;
}

.product-workbench__hero {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.product-workbench__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;

  p {
    margin: 0 0 4px;
    color: #64748b;
    font-size: 13px;
  }

  h1 {
    margin: 0;
    color: #111827;
    font-size: 22px;
    font-weight: 750;
  }

  span {
    display: block;
    margin-top: 8px;
    color: #64748b;
    font-size: 13px;
  }
}

.product-workbench__sync-time {
  color: #64748b;
  font-size: 12px;
}

.product-workbench__task-flow {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-workbench__section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;

  h2 {
    margin: 0;
    color: #111827;
    font-size: 16px;
    font-weight: 750;
  }

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 12px;
  }
}

.product-workbench__stats {
  row-gap: 12px;
}

.product-workbench__stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  min-height: 82px;

  &.is-blue .product-workbench__stat-icon { color: #2563eb; background: #eff6ff; }
  &.is-green .product-workbench__stat-icon { color: #16a34a; background: #f0fdf4; }
  &.is-amber .product-workbench__stat-icon { color: #d97706; background: #fffbeb; }
  &.is-red .product-workbench__stat-icon { color: #dc2626; background: #fef2f2; }
  &.is-slate .product-workbench__stat-icon { color: #475569; background: #f8fafc; }

  .product-workbench__stat-icon {
    display: grid;
    width: 42px;
    height: 42px;
    place-items: center;
    border-radius: 8px;
    font-weight: 750;
  }

  span {
    display: block;
    color: #64748b;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #111827;
    font-size: 24px;
  }
}

.product-workbench__panel {
  padding: 14px;
  margin-bottom: 12px;
}

.product-workbench__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;

  h2 {
    margin: 0;
    color: #111827;
    font-size: 16px;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.product-workbench__table :deep(.el-table__cell) {
  padding: 9px 0;
}

.product-workbench__queue,
.product-workbench__timeline {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-workbench__queue-item,
.product-workbench__timeline-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #fbfdff;

  strong {
    display: block;
    color: #111827;
    font-size: 13px;
    line-height: 1.4;
  }

  span {
    display: block;
    margin-top: 2px;
    color: #64748b;
    font-size: 12px;
  }
}

.product-workbench__quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;

  :deep(.el-button) {
    width: 100%;
    margin: 0;
    justify-content: flex-start;
  }
}

.product-workbench__timeline-item i {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #94a3b8;

  &.success { background: #22c55e; }
  &.warning { background: #f59e0b; }
  &.danger { background: #ef4444; }
}

@media (max-width: 768px) {
  .product-workbench__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .product-workbench__quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
