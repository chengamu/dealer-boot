<template>
  <section class="diagnosis-card">
    <header class="diagnosis-card__header">
      <h3>{{ t('pay.reconciliation.diagnosis') }}</h3>
    </header>
    <div class="diagnosis-card__body">
      <el-alert
        :title="detail.reconciliationCase?.diagnosisMessage || '-'"
        type="warning"
        :closable="false"
        show-icon
      />
      <dl class="diagnosis-card__facts">
        <div>
          <dt>{{ t('pay.reconciliation.caseNo') }}</dt>
          <dd>{{ detail.reconciliationCase?.caseNo || '-' }}</dd>
        </div>
        <div>
          <dt>{{ t('pay.reconciliation.anomalyType') }}</dt>
          <dd>{{ detail.reconciliationCase?.anomalyType || '-' }}</dd>
        </div>
        <div>
          <dt>{{ t('pay.reconciliation.severity.label') }}</dt>
          <dd>{{ reconciliationSeverityText(t, detail.reconciliationCase?.severity) }}</dd>
        </div>
        <div>
          <dt>{{ t('common.status') }}</dt>
          <dd>{{ reconciliationStatusText(t, detail.reconciliationCase?.status) }}</dd>
        </div>
      </dl>
      <el-input
        v-model="localReason"
        type="textarea"
        :rows="4"
        :maxlength="200"
        show-word-limit
        :placeholder="t('pay.reason')"
      />
      <div class="diagnosis-card__actions">
        <el-button
          v-if="canRescan && detail.reconciliationCase?.status === 'OPEN'"
          :loading="actionLoading"
          @click="emit('action', 'rescan')"
        >
          {{ t('pay.reconciliation.rescan') }}
        </el-button>
        <el-button
          v-if="canReconcile && detail.reconciliationCase?.status === 'OPEN'"
          :loading="actionLoading"
          @click="emit('action', 'reconcileChannel')"
        >
          {{ t('pay.reconcile.action') }}
        </el-button>
        <el-button
          v-if="canRepair && detail.reconciliationCase?.status === 'OPEN'"
          type="primary"
          :loading="actionLoading"
          @click="emit('action', 'repairOrder')"
        >
          {{ t('pay.repair.action') }}
        </el-button>
        <el-button
          v-if="canIgnore && detail.reconciliationCase?.status === 'OPEN'"
          :loading="actionLoading"
          @click="emit('action', 'ignore')"
        >
          {{ t('pay.reconciliation.ignore') }}
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ReconciliationCaseDetail } from '@/api/pay'
import { reconciliationSeverityText, reconciliationStatusText } from '../payPresentation'

defineProps<{
  detail: ReconciliationCaseDetail
  actionLoading: boolean
  canRepair: boolean
  canRescan: boolean
  canReconcile: boolean
  canIgnore: boolean
}>()
const emit = defineEmits<{
  'update:reason': [value: string]
  action: [type: 'rescan' | 'reconcileChannel' | 'repairOrder' | 'ignore']
}>()
const reason = defineModel<string>('reason', { required: true })
const { t } = useI18n()
const localReason = computed({
  get: () => reason.value,
  set: (value) => { reason.value = value }
})
</script>

<style scoped>
.diagnosis-card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.diagnosis-card__header {
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.diagnosis-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.diagnosis-card__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
}

.diagnosis-card__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.diagnosis-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .diagnosis-card__facts {
    grid-template-columns: 1fr;
  }
}
</style>
