import type { ProductRecord } from '@/api/product-capability/types'

export function compactParts(...parts: unknown[]) {
  return parts.map((part) => String(part ?? '').trim()).filter(Boolean).join(' ')
}

export function localizedRecordLabel(row: ProductRecord, language: string, codeKey: string, cnKey: string, enKey?: string) {
  const code = String(row[codeKey] || '')
  const name = language === 'zh_CN' ? row[cnKey] : row[enKey || cnKey] || row[cnKey]
  return compactParts(code, name)
}
