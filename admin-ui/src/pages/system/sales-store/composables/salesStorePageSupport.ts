import { SALES_STORE_STATUS, type SalesStore, type SalesStoreDeptOption } from '@/api/system/sales-store'

export function createEmptySalesStoreForm(): SalesStore {
  return {
    salesStoreId: undefined,
    storeCode: '',
    storeName: '',
    deptId: undefined,
    deptName: '',
    contactName: '',
    contactPhone: '',
    country: '',
    state: '',
    city: '',
    addressLine1: '',
    addressLine2: '',
    postalCode: '',
    currencyCode: 'USD',
    creditLimit: null,
    paymentTermDays: undefined,
    status: SALES_STORE_STATUS.ENABLED,
    remark: ''
  }
}

export function normalizeSalesStoreDeptOptions(options: SalesStoreDeptOption[], deptId?: number, deptName?: string) {
  if (!deptId || options.some((item) => item.deptId === deptId)) return options
  return [{ deptId, deptName: deptName || String(deptId), linked: true }, ...options]
}

export function isEnabledSalesStore(status?: string) {
  return String(status) === SALES_STORE_STATUS.ENABLED
}

export function buildSalesStorePayload(form: SalesStore): SalesStore {
  return {
    salesStoreId: form.salesStoreId,
    storeCode: form.storeCode?.trim(),
    storeName: form.storeName?.trim(),
    deptId: form.deptId,
    contactName: form.contactName?.trim(),
    contactPhone: form.contactPhone?.trim(),
    country: form.country?.trim(),
    state: form.state?.trim(),
    city: form.city?.trim(),
    addressLine1: form.addressLine1?.trim(),
    addressLine2: form.addressLine2?.trim(),
    postalCode: form.postalCode?.trim(),
    creditLimit: form.creditLimit ?? null,
    paymentTermDays: form.paymentTermDays ?? undefined,
    status: form.status || SALES_STORE_STATUS.ENABLED,
    remark: form.remark?.trim()
  }
}
