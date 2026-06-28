import { requestData } from '@/utils/request'

export type AiQuota = {
  dailyRequestRemaining?: number | null
  dailyTokenRemaining?: number | null
  dailyCostRemaining?: string | number | null
}

export type AiBootstrap = {
  enabled: boolean
  model?: string
  pageAgentBaseUrl?: string
  apiKey?: string
  features?: string[]
  permissions?: string[]
  quota?: AiQuota
}

export function getAiBootstrap() {
  return requestData<AiBootstrap>({
    url: '/ai/bootstrap',
    method: 'get',
    silentError: true
  })
}
