import { canonicalDecimal } from '@/utils/businessNumber'

export function toQuoteDecimal(value: unknown) {
  return canonicalDecimal(value as string | number | null) ?? undefined
}

export function toQuoteId(value: unknown) {
  return value == null || value === '' ? undefined : String(value)
}
