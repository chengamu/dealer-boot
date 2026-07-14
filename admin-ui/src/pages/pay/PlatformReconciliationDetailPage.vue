<template>
  <div v-loading="loading" class="reconciliation-detail-page">
    <header class="reconciliation-detail-page__header">
      <div class="reconciliation-detail-page__heading">
        <div class="reconciliation-detail-page__title-row">
          <h1>{{ detail?.payment?.payOrderNo || detail?.reconciliationCase?.caseNo || '-' }}</h1>
          <el-tag
            v-if="detail?.reconciliationCase?.severity"
            :type="reconciliationSeverityType(detail.reconciliationCase.severity)"
            effect="plain"
            round
          >
            {{ reconciliationSeverityText(t, detail.reconciliationCase.severity) }}
          </el-tag>
          <el-tag
            v-if="detail?.reconciliationCase?.status"
            :type="detail.reconciliationCase.status === 'OPEN' ? 'danger' : 'success'"
            effect="plain"
            round
          >
            {{ reconciliationStatusText(t, detail.reconciliationCase.status) }}
          </el-tag>
        </div>
        <div v-if="detail?.payment" class="reconciliation-detail-page__meta">
          <span>{{ t('pay.salesOrderNo') }}: {{ detail.payment.salesOrderNo || '-' }}</span>
          <span>{{ t('common.status') }}: {{ paymentStatusText(detail.payment.status) }}</span>
        </div>
      </div>
      <div class="reconciliation-detail-page__actions">
        <el-button :icon="ArrowLeft" @click="emit('back')">{{ t('common.back') }}</el-button>
        <el-button :icon="RefreshRight" :loading="loading || actionLoading" @click="load">
          {{ t('common.refresh') }}
        </el-button>
        <el-button
          v-if="canRepair && detail?.reconciliationCase?.status === 'OPEN'"
          type="primary"
          :loading="actionLoading"
          @click="runAction('repairOrder')"
        >
          {{ t('pay.repair.action') }}
        </el-button>
      </div>
    </header>

    <template v-if="detail">
      <div class="reconciliation-detail-page__summary">
        <PlatformReconciliationSnapshotCard :detail="detail" />
      </div>
      <div class="reconciliation-detail-page__layout">
        <PlatformReconciliationProcessCard :detail="detail" />
        <PlatformReconciliationDiagnosisCard
          v-model:reason="reason"
          :detail="detail"
          :action-loading="actionLoading"
          :can-repair="canRepair"
          :can-rescan="canRescan"
          :can-reconcile="canReconcile"
          :can-ignore="canIgnore"
          @action="runAction"
        />
      </div>
    </template>
    <el-empty v-else-if="!loading" :description="t('pay.checkout.notFound')" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { checkPermi } from '@/utils/permission'
import { platformReconciliationApi, type PayOrderStatus, type ReconciliationCaseDetail } from '@/api/pay'
import PlatformReconciliationDiagnosisCard from './components/PlatformReconciliationDiagnosisCard.vue'
import PlatformReconciliationProcessCard from './components/PlatformReconciliationProcessCard.vue'
import PlatformReconciliationSnapshotCard from './components/PlatformReconciliationSnapshotCard.vue'
import { reconciliationSeverityText, reconciliationSeverityType, reconciliationStatusText, statusText } from './payPresentation'

type ReconciliationActionType = 'rescan' | 'reconcileChannel' | 'repairOrder' | 'ignore'

const props = defineProps<{ caseId: string }>()
const emit = defineEmits<{ back: [] }>()
const { t } = useI18n()
const loading = ref(false)
const actionLoading = ref(false)
const detail = ref<ReconciliationCaseDetail>()
const reason = ref('')
const canRepair = checkPermi(['platform:finance:reconciliation:repair'])
const canRescan = checkPermi(['platform:finance:reconciliation:rescan'])
const canReconcile = checkPermi(['platform:finance:reconciliation:channel'])
const canIgnore = checkPermi(['platform:finance:reconciliation:ignore'])

function paymentStatusText(status?: PayOrderStatus) {
  return statusText(t, status)
}

async function load() {
  if (!props.caseId) return
  loading.value = true
  try {
    detail.value = await platformReconciliationApi.detail(props.caseId)
  } finally {
    loading.value = false
  }
}

async function runAction(action: ReconciliationActionType) {
  if (!detail.value?.reconciliationCase?.caseId) return
  if (!reason.value.trim()) {
    ElMessage.warning(t('pay.reason'))
    return
  }
  actionLoading.value = true
  try {
    await platformReconciliationApi[action](detail.value.reconciliationCase.caseId, reason.value.trim())
    ElMessage.success(t('common.operationSuccess'))
    reason.value = ''
    await load()
  } finally {
    actionLoading.value = false
  }
}

watch(() => props.caseId, () => { void load() }, { immediate: true })
</script>

<style scoped>
.reconciliation-detail-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px;
  background: var(--admin-bg);
}

.reconciliation-detail-page__header,
.reconciliation-detail-page__actions,
.reconciliation-detail-page__title-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.reconciliation-detail-page__header {
  justify-content: space-between;
}

.reconciliation-detail-page__heading {
  min-width: 0;
}

.reconciliation-detail-page__title-row h1 {
  margin: 0;
  color: #1d2129;
  font-size: 20px;
  line-height: 1.3;
}

.reconciliation-detail-page__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
}

.reconciliation-detail-page__actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.reconciliation-detail-page__summary {
  min-width: 0;
}

.reconciliation-detail-page__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(360px, 0.9fr);
  gap: 12px;
  align-items: start;
}

@media (max-width: 1200px) {
  .reconciliation-detail-page__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .reconciliation-detail-page__header {
    flex-direction: column;
  }
}
</style>
