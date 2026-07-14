import type { MerchantProfileOption } from '@/api/merchant/profile'
import type { SalesStoreOption } from '@/api/dealer-sales'

export function resolveMerchantName(tenantId: string | number | undefined, merchantOptions: MerchantProfileOption[]) {
  const key = String(tenantId || '')
  if (!key) return '-'
  const target = merchantOptions.find((item) => String(item.tenantId || '') === key)
  return target?.merchantName || key
}

export function resolveSalesStoreName(salesStoreId: string | number | undefined, salesStoreOptions: SalesStoreOption[]) {
  const key = String(salesStoreId || '')
  if (!key) return '-'
  const target = salesStoreOptions.find((item) => String(item.salesStoreId || '') === key)
  return target?.storeName || target?.storeCode || key
}
