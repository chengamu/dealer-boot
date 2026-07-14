import type { PageQuery } from '@/types/api'
import type {
  BusinessOrigin,
  PayOrderDetail,
  ReconciliationSeverity,
  ReconciliationStatus
} from './types'

export interface ReconciliationAction {
  actionId?: string
  caseId?: string
  actionType?: string
  beforeSnapshotJson?: string
  afterSnapshotJson?: string
  resultCode?: string
  resultMessage?: string
  reason?: string
  operatorId?: string
  operatorName?: string
  occurredTime?: string
}

export interface ReconciliationCase {
  caseId?: string
  caseNo?: string
  businessOrigin?: BusinessOrigin
  tenantId?: string
  salesStoreId?: string
  payOrderId?: string
  extensionId?: string
  webhookEventId?: string
  salesDocumentId?: string
  anomalyType?: string
  severity?: ReconciliationSeverity
  status?: ReconciliationStatus
  detectedTime?: string
  lastCheckedTime?: string
  diagnosisCode?: string
  diagnosisMessage?: string
  expectedSnapshotJson?: string
  actualSnapshotJson?: string
  resolvedById?: string
  resolvedBy?: string
  resolvedTime?: string
  resolutionCode?: string
}

export interface ReconciliationCaseDetail {
  reconciliationCase?: ReconciliationCase
  payment?: PayOrderDetail
  actions?: ReconciliationAction[]
}

export interface ReconciliationQuery extends PageQuery {
  beginTime?: string
  endTime?: string
  status?: ReconciliationStatus | ''
  anomalyType?: string
  severity?: ReconciliationSeverity | ''
  businessOrigin?: BusinessOrigin | ''
  subjectId?: string
  keyword?: string
}
