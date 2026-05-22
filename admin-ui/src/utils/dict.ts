import { ref, toRefs, watch } from 'vue'
import { getDicts } from '@/api/system/dict/data'
import useDictStore from '@/stores/dict'
import useLocaleStore from '@/stores/locale'
import { getMessage } from '@/locales'

export interface DictOption {
  label?: string
  value?: string
  elTagType?: string
  elTagClass?: string
  status?: string
  i18nKey?: string
}

const frontendDictLabelKeys: Record<string, Record<string, string>> = {
  sys_yes_no: {
    Y: 'common.yes',
    N: 'common.no'
  }
}

function resolveDictLabel(dictType: string, item: DictOption, locale: string) {
  if (item.i18nKey) {
    const translated = getMessage(item.i18nKey, locale)
    if (translated !== item.i18nKey) return translated
  }
  const frontendKey = item.value ? frontendDictLabelKeys[dictType]?.[item.value] : undefined
  return frontendKey ? getMessage(frontendKey, locale) : item.label
}

export function useDict(...args: string[]) {
  const res = ref<Record<string, DictOption[]>>({})
  const dictStore = useDictStore()
  const localeStore = useLocaleStore()

  const loadDict = (dictType: string) => {
    const cacheKey = `${localeStore.language}:${dictType}`
    res.value[dictType] = []
    const dicts = dictStore.getDict<DictOption[]>(cacheKey)
    if (dicts) {
      res.value[dictType] = dicts
      return
    }

    getDicts(dictType).then((resp) => {
      res.value[dictType] = (resp.data || []).map((item) => ({
        label: resolveDictLabel(dictType, {
          label: item.dictLabel,
          value: item.dictValue,
          i18nKey: item.i18nKey
        }, localeStore.language),
        value: item.dictValue,
        elTagType: item.listClass,
        elTagClass: item.cssClass,
        status: item.status,
        i18nKey: item.i18nKey
      }))
      dictStore.setDict(cacheKey, res.value[dictType])
    })
  }

  watch(
    () => localeStore.language,
    () => {
      args.forEach(loadDict)
    }
  )

  args.forEach(loadDict)
  return toRefs(res.value)
}
