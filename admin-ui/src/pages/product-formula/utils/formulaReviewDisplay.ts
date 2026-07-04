import type { ProductRecord } from '@/api/product-capability/types'
import { formatUtc } from '@/utils/datetime'

export function parseFormulaReviewJson<T extends ProductRecord = ProductRecord>(value?: string): T {
  if (!value) return {} as T
  try {
    return JSON.parse(value) as T
  } catch {
    return {} as T
  }
}

export function formatFormulaReviewMinute(value?: string) {
  return formatUtc(value, 'YYYY-MM-DD HH:mm')
}
