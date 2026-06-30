import { request, requestData, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

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
  apiKey?: string
  createTime?: string
  updateTime?: string
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
  createTime?: string
  updateTime?: string
}

export interface AiUserQuota {
  quotaId?: number
  tenantId?: number
  userId?: number
  userName?: string
  nickName?: string
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
  userName?: string
  nickName?: string
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
  userName?: string
  nickName?: string
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

export interface AiCredentialQuery extends PageQuery {
  serviceName?: string
  status?: string
}

export interface AiProviderQuery extends PageQuery {
  providerCode?: string
  providerName?: string
  enabled?: boolean
  status?: string
}

export interface AiProviderModelQuery extends PageQuery {
  providerCode?: string
  modelCode?: string
  modelName?: string
  modelType?: string
  status?: string
}

export interface AiTenantUserQuery extends PageQuery {
  tenantId?: number
  userId?: number
  status?: string
}

export interface AiUsageQuery extends AiTenantUserQuery {
  provider?: string
  model?: string
  requestId?: string
  status?: string
}

export interface AiAuditQuery extends AiTenantUserQuery {
  actionType?: string
  toolCode?: string
  riskLevel?: string
  approvalStatus?: string
  requestId?: string
}

export function listAiServiceCredentials(query: AiCredentialQuery) {
  return requestPage<AiServiceCredential>({
    url: '/ai/admin/service-credentials/list',
    method: 'get',
    params: query
  })
}

export function generateAiServiceCredential(data: { serviceName?: string; remark?: string }) {
  return requestData<AiServiceCredentialSecret>({
    url: '/ai/admin/service-credentials/generate',
    method: 'post',
    data
  })
}

export function enableAiServiceCredential(credentialId: number) {
  return request({
    url: `/ai/admin/service-credentials/${credentialId}/enable`,
    method: 'put'
  })
}

export function disableAiServiceCredential(credentialId: number) {
  return request({
    url: `/ai/admin/service-credentials/${credentialId}/disable`,
    method: 'put'
  })
}

export function deleteAiServiceCredential(credentialId: number) {
  return request({
    url: `/ai/admin/service-credentials/${credentialId}`,
    method: 'delete'
  })
}

export function listAiProviders(query: AiProviderQuery) {
  return requestPage<AiProviderConfig>({
    url: '/ai/admin/providers/list',
    method: 'get',
    params: query
  })
}

export function listAiProviderOptions() {
  return requestData<AiProviderConfig[]>({
    url: '/ai/admin/providers/options',
    method: 'get'
  })
}

export function addAiProvider(data: AiProviderConfig) {
  return request({
    url: '/ai/admin/providers',
    method: 'post',
    data
  })
}

export function updateAiProvider(data: AiProviderConfig) {
  return request({
    url: '/ai/admin/providers',
    method: 'put',
    data
  })
}

export function saveAiProviderApiKey(providerId: number, data: { providerCode?: string; apiKey?: string; remark?: string }) {
  return request({
    url: `/ai/admin/providers/${providerId}/api-key`,
    method: 'put',
    data
  })
}

export function listAiProviderModels(query: AiProviderModelQuery) {
  return requestPage<AiProviderModel>({
    url: '/ai/admin/provider-models/list',
    method: 'get',
    params: query
  })
}

export function addAiProviderModel(data: AiProviderModel) {
  return request({
    url: '/ai/admin/provider-models',
    method: 'post',
    data
  })
}

export function updateAiProviderModel(data: AiProviderModel) {
  return request({
    url: '/ai/admin/provider-models',
    method: 'put',
    data
  })
}

export function setDefaultAiProviderModel(modelId: number) {
  return request({
    url: `/ai/admin/provider-models/${modelId}/default`,
    method: 'put'
  })
}

export function listAiUserQuotas(query: AiTenantUserQuery) {
  return requestPage<AiUserQuota>({
    url: '/ai/admin/user-quotas/list',
    method: 'get',
    params: query
  })
}

export function addAiUserQuota(data: AiUserQuota) {
  return request({
    url: '/ai/admin/user-quotas',
    method: 'post',
    data
  })
}

export function updateAiUserQuota(data: AiUserQuota) {
  return request({
    url: '/ai/admin/user-quotas',
    method: 'put',
    data
  })
}

export function listAiUsageLedgers(query: AiUsageQuery) {
  return requestPage<AiUsageLedger>({
    url: '/ai/admin/usage-ledgers/list',
    method: 'get',
    params: query
  })
}

export function listAiAuditSummaries(query: AiAuditQuery) {
  return requestPage<AiAuditSummary>({
    url: '/ai/admin/audit-summaries/list',
    method: 'get',
    params: query
  })
}
