import { request, requestData } from '@/utils/request'

export interface AiServiceCredential {
  credentialId?: number
  serviceName?: string
  keyVersion?: string
  secretFingerprint?: string
  status?: string
  lastUsedTime?: string
  remark?: string
  createTime?: string
}

export interface AiServiceCredentialSecret {
  credential?: AiServiceCredential
  secret?: string
}

export interface AiProviderConfig {
  providerId?: number
  providerCode?: string
  providerName?: string
  baseUrl?: string
  chatCompletionsPath?: string
  defaultModel?: string
  timeoutSeconds?: number
  enabled?: boolean
  status?: string
  remark?: string
  keyFingerprint?: string
}

export interface AiProviderModel {
  modelId?: number
  providerId?: number
  providerCode?: string
  modelCode?: string
  modelName?: string
  modelType?: string
  contextWindow?: number
  inputPrice?: number
  outputPrice?: number
  defaultModel?: boolean
  status?: string
  remark?: string
}

export interface AiUserQuota {
  quotaId?: number
  tenantId?: number
  userId?: number
  dailyRequestLimit?: number
  dailyTokenLimit?: number
  dailyCostLimit?: number
  status?: string
  createTime?: string
  updateTime?: string
}

export interface AiUsageLedger {
  usageId?: number
  tenantId?: number
  userId?: number
  sessionId?: string
  requestId?: string
  providerCallId?: string
  provider?: string
  model?: string
  inputTokens?: number
  outputTokens?: number
  costAmount?: number
  latencyMs?: number
  status?: string
  createdTime?: string
}

export interface AiAuditSummary {
  auditId?: number
  tenantId?: number
  userId?: number
  sessionId?: string
  requestId?: string
  actionType?: string
  toolCode?: string
  businessTarget?: string
  riskLevel?: string
  approvalStatus?: string
  status?: string
  createdTime?: string
}

export type AiListQuery = {
  tenantId?: number
  userId?: number
  limit?: number
}

export function listAiServiceCredentials() {
  return requestData<AiServiceCredential[]>({
    url: '/ai/admin/service-credentials',
    method: 'get'
  })
}

export function generateAiServiceCredential(data: { serviceName?: string; remark?: string }) {
  return requestData<AiServiceCredentialSecret>({
    url: '/ai/admin/service-credentials/generate',
    method: 'post',
    data
  })
}

export function disableAiServiceCredential(credentialId: number) {
  return request({
    url: `/ai/admin/service-credentials/${credentialId}/disable`,
    method: 'put'
  })
}

export function listAiProviders() {
  return requestData<AiProviderConfig[]>({
    url: '/ai/admin/providers',
    method: 'get'
  })
}

export function saveAiProvider(data: AiProviderConfig) {
  return request({
    url: '/ai/admin/providers',
    method: 'put',
    data
  })
}

export function saveAiProviderApiKey(data: { providerCode?: string; apiKey?: string; remark?: string }) {
  return request({
    url: '/ai/admin/providers/api-key',
    method: 'post',
    data
  })
}

export function listAiProviderModels(providerCode: string) {
  return requestData<AiProviderModel[]>({
    url: `/ai/admin/providers/${providerCode}/models`,
    method: 'get'
  })
}

export function saveAiProviderModel(data: AiProviderModel) {
  return request({
    url: '/ai/admin/providers/models',
    method: 'put',
    data
  })
}

export function setDefaultAiProviderModel(providerCode: string, modelId: number) {
  return request({
    url: `/ai/admin/providers/${providerCode}/models/${modelId}/default`,
    method: 'put'
  })
}

export function listAiUserQuotas(params: AiListQuery) {
  return requestData<AiUserQuota[]>({
    url: '/ai/admin/user-quotas',
    method: 'get',
    params
  })
}

export function saveAiUserQuota(data: AiUserQuota) {
  return request({
    url: '/ai/admin/user-quotas',
    method: 'put',
    data
  })
}

export function listAiUsageLedgers(params: AiListQuery) {
  return requestData<AiUsageLedger[]>({
    url: '/ai/admin/usage-ledgers',
    method: 'get',
    params
  })
}

export function listAiAuditSummaries(params: AiListQuery) {
  return requestData<AiAuditSummary[]>({
    url: '/ai/admin/audit-summaries',
    method: 'get',
    params
  })
}
