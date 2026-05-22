import { request } from '@/utils/request'

export type MerchantApplicationPayload = Record<string, unknown>

export function submitMerchantApplication(data: MerchantApplicationPayload) {
  return request({
    url: '/merchant/applications',
    method: 'post',
    headers: { isToken: false },
    data
  })
}
