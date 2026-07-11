export function toQuoteNumber(value: unknown) {
  if (value == null || value === '') return undefined
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : undefined
}

export function toQuoteId(value: unknown) {
  return value == null || value === '' ? undefined : String(value)
}
