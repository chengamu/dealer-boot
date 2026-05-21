import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'
import useLocaleStore from '@/store/modules/locale'

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({});
  const dictStore = useDictStore();
  const localeStore = useLocaleStore();
  const loadDict = (dictType) => {
    const cacheKey = `${localeStore.language}:${dictType}`;
    res.value[dictType] = [];
    const dicts = dictStore.getDict(cacheKey);
    if (dicts) {
      res.value[dictType] = dicts;
    } else {
      getDicts(dictType).then(resp => {
        res.value[dictType] = resp.data.map(p => ({ label: p.dictLabel, value: p.dictValue, elTagType: p.listClass, elTagClass: p.cssClass, status: p.status }))
        dictStore.setDict(cacheKey, res.value[dictType]);
      })
    }
  }
  watch(() => localeStore.language, () => {
    args.forEach(loadDict);
  });
  return (() => {
    args.forEach((dictType, index) => {
      loadDict(dictType);
    })
    return toRefs(res.value);
  })()
}
