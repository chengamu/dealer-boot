import { computed, onMounted, ref } from 'vue'
import { useLocaleStore } from '@/stores/locale'
import { getProductDictItems } from '@/api/product-capability/product-dict'
import type { ProductDictOption } from '@/api/product-capability/types'

const cache = new Map<string, ProductDictOption[]>()

export function productDictLabel(option: ProductDictOption, language: string) {
  return language === 'zh_CN' ? option.labelCn || option.label || option.value : option.labelEn || option.label || option.labelCn || option.value
}

export function useProductDict(...dictTypeCodes: string[]) {
  const localeStore = useLocaleStore()
  const state = ref<Record<string, ProductDictOption[]>>({})

  async function loadOne(dictTypeCode: string) {
    if (cache.has(dictTypeCode)) {
      state.value[dictTypeCode] = cache.get(dictTypeCode) || []
      return
    }
    const response = await getProductDictItems(dictTypeCode)
    const rows = response.data || []
    cache.set(dictTypeCode, rows)
    state.value[dictTypeCode] = rows
  }

  async function reload() {
    await Promise.all(dictTypeCodes.map((code) => loadOne(code)))
  }

  onMounted(reload)

  const options = computed(() => Object.fromEntries(dictTypeCodes.map((code) => [
    code,
    (state.value[code] || []).map((option) => ({
      ...option,
      label: productDictLabel(option, localeStore.language),
      value: option.value
    }))
  ])))

  return {
    options,
    reload
  }
}
