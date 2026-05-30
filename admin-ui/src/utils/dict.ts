import { ref, toRefs, watch, type Ref } from 'vue'
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
}

const frontendDictLabelKeys: Record<string, Record<string, string>> = {
  sys_user_sex: {
    '0': 'dict.sys_user_sex.0',
    '1': 'dict.sys_user_sex.1',
    '2': 'dict.sys_user_sex.2'
  },
  sys_normal_disable: {
    '1': 'dataLabels.normal',
    '0': 'dataLabels.disabled'
  },
  sys_show_hide: {
    '1': 'dataLabels.show',
    '0': 'dataLabels.hide'
  },
  sys_yes_no: {
    Y: 'common.yes',
    N: 'common.no'
  },
  sys_common_status: {
    '0': 'dataLabels.success',
    '1': 'common.failed'
  },
  sys_notice_type: {
    '1': 'message.notice',
    '2': 'dataLabels.systemAnnouncement'
  },
  sys_notice_status: {
    '0': 'dataLabels.normal',
    '1': 'dataLabels.closed'
  },
  sys_oper_type: {
    '0': 'dataLabels.other',
    '1': 'common.add',
    '2': 'common.edit',
    '3': 'common.delete',
    '4': 'dataLabels.grant',
    '5': 'common.export',
    '6': 'common.import',
    '7': 'legacy.forceLogout',
    '8': 'dataLabels.generateCode',
    '9': 'common.clear',
    '10': 'dataLabels.crossTenantQuery',
    '11': 'dataLabels.sensitiveOperation'
  }
}

const fallbackDictValues: Record<string, string[]> = {
  sys_notice_type: ['1', '2'],
  sys_notice_status: ['0', '1']
}

function resolveDictLabel(dictType: string, item: DictOption, locale: string) {
  const frontendKey = item.value ? frontendDictLabelKeys[dictType]?.[item.value] : undefined
  if (frontendKey) {
    const translated = getMessage(frontendKey, locale)
    if (translated && translated !== frontendKey) {
      return translated
    }
  }
  if (item.label) {
    return item.label
  }
  return frontendKey ? getMessage(frontendKey, locale) : item.label
}

export function useDict<T extends string>(...args: T[]): Record<T, Ref<DictOption[]>> {
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
      const items = [...(resp.data || [])]
      const fallbackValues = fallbackDictValues[dictType] || []
      fallbackValues.forEach((value) => {
        if (!items.some((item) => item.dictValue === value)) {
          items.push({ dictValue: value, dictLabel: '' })
        }
      })
      res.value[dictType] = items.map((item) => ({
        label: resolveDictLabel(dictType, {
          label: item.dictLabel,
          value: item.dictValue
        }, localeStore.language),
        value: item.dictValue,
        elTagType: item.listClass,
        elTagClass: item.cssClass,
        status: item.status
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
  return toRefs(res.value) as Record<T, Ref<DictOption[]>>
}
