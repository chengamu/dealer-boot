import dayjs from 'dayjs'
import { computed, ref, watch } from 'vue'
import { listSalesStoreOptions, type SalesStoreOption } from '@/api/dealer-sales'
import { listMerchantProfileOptions, type MerchantProfileOption } from '@/api/merchant/profile'
import { formatUtc } from '@/utils/datetime'
import type { BusinessOrigin } from '@/api/dealer-fulfillment'

type SubjectQuery = {
  businessOrigin?: BusinessOrigin | ''
  tenantId?: string
  salesStoreId?: string
}

export function currentMonthDateRange() {
  const now = dayjs()
  return [now.startOf('month').format('YYYY-MM-DD'), now.endOf('day').format('YYYY-MM-DD')]
}

export function formatMinute(value?: string) {
  return formatUtc(value, 'YYYY-MM-DD HH:mm')
}

export function useFulfillmentSubjectFilter(query: SubjectQuery) {
  const loading = ref(false)
  const merchantOptions = ref<MerchantProfileOption[]>([])
  const salesStoreOptions = ref<SalesStoreOption[]>([])

  const subjectOptions = computed(() => {
    if (query.businessOrigin === 'INTERNAL') {
      return salesStoreOptions.value.map((item) => ({
        value: String(item.salesStoreId || ''),
        label: [item.storeCode, item.storeName].filter(Boolean).join(' · ') || String(item.salesStoreId || '')
      }))
    }
    return merchantOptions.value.map((item) => ({
      value: String(item.tenantId || ''),
      label: item.merchantName || String(item.tenantId || '')
    }))
  })

  async function load() {
    loading.value = true
    try {
      const [merchantRes, storeRes] = await Promise.all([
        listMerchantProfileOptions(),
        listSalesStoreOptions()
      ])
      merchantOptions.value = merchantRes.data || []
      salesStoreOptions.value = storeRes.data || []
    } finally {
      loading.value = false
    }
  }

  watch(
    () => query.businessOrigin,
    () => {
      query.tenantId = ''
      query.salesStoreId = ''
    }
  )

  return {
    loading,
    subjectOptions,
    load
  }
}
