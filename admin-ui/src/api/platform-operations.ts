import { request, requestPage } from '@/utils/request'
import type { DecimalValue, PageQuery } from '@/types/api'

export interface OperationsMerchant extends Record<string, unknown> {
  source?: 'profile' | 'application'
  applicationId?: number
  merchantId?: number
  tenantId?: number
  merchantName?: string
  companyName?: string
  contactName?: string
  primaryEmail?: string
  officePhone?: string
  mobilePhone?: string
  country?: string
  state?: string
  city?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  status?: string
  auditStatus?: string
  auditBy?: string
  auditTime?: string
  rejectReason?: string
  levelName?: string
  levelCode?: string
  discountRate?: DecimalValue
  creditLimit?: DecimalValue
  orderCount?: number
  createTime?: string
}

export interface OperationsMerchantQuery extends PageQuery {
  keyword?: string
  status?: string
  levelCode?: string
}

export interface OperationsLevelOption {
  levelId?: number
  levelCode?: string
  levelName?: string
}

export interface OperationsOrderLifecycle {
  submittedCount?: number
  unpaidCount?: number
  productionCount?: number
  shippedCount?: number
  completedCount?: number
}

export interface OperationsSummary {
  pendingApplicationCount?: number
  enabledMerchantCount?: number
  disabledMerchantCount?: number
  vipMerchantCount?: number
  orderLifecycle?: OperationsOrderLifecycle
  currencyAmounts?: Record<string, DecimalValue>
  dataAsOf?: string
}

export const platformOperationsApi = {
  summary: () => request<OperationsSummary>({ url: '/platform-sales/operations/summary', method: 'get' }),
  merchants: (params?: OperationsMerchantQuery) => requestPage<OperationsMerchant>({ url: '/platform-sales/operations/merchants', method: 'get', params }),
  applications: (params?: OperationsMerchantQuery) => requestPage<OperationsMerchant>({ url: '/platform-sales/operations/applications', method: 'get', params }),
  levelOptions: (status?: string) => request<OperationsLevelOption[]>({ url: '/platform-sales/operations/levels/options', method: 'get', params: { status } })
}
