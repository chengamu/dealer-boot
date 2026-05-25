import { i18n } from '@/i18n'
import { parseUtc } from '@/utils/datetime'

type DictValue = string | number | boolean | null | undefined
type DictItem = {
  label?: DictValue
  value?: DictValue
  [key: string]: unknown
}
type DictData = DictItem[] | Record<string, DictItem>

const WEEKDAY_KEYS = [
  'datetime.weekdays.sun',
  'datetime.weekdays.mon',
  'datetime.weekdays.tue',
  'datetime.weekdays.wed',
  'datetime.weekdays.thu',
  'datetime.weekdays.fri',
  'datetime.weekdays.sat'
]

export function parseTime(time?: string | number | Date | null, pattern = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (!time) return null

  let value: string | number | Date = time
  if (typeof value === 'string' && /^[0-9]+$/.test(value)) {
    value = Number.parseInt(value, 10)
  }
  if (typeof value === 'number' && value.toString().length === 10) {
    value *= 1000
  }

  const parsed = parseUtc(value)
  if (!parsed || !parsed.isValid()) return null

  const date = parsed.local().toDate()
  const formatObj: Record<string, number> = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }

  return pattern.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key: string) => {
    if (key === 'a') return i18n.global.t(WEEKDAY_KEYS[formatObj[key]])
    const item = formatObj[key]
    return result.length > 0 && item < 10 ? `0${item}` : String(item || 0)
  })
}

export function resetForm(this: { $refs?: Record<string, unknown> }, refName: string) {
  const form = this?.$refs?.[refName] as { resetFields?: () => void } | undefined
  form?.resetFields?.()
}

export function addDateRange<T extends { params?: Record<string, unknown> }>(
  params: T,
  dateRange?: unknown[],
  propName?: string
) {
  const search = params
  search.params =
    typeof search.params === 'object' && search.params !== null && !Array.isArray(search.params)
      ? search.params
      : {}

  const range = Array.isArray(dateRange) ? dateRange : []
  if (typeof propName === 'undefined') {
    search.params.beginTime = range[0]
    search.params.endTime = range[1]
  } else {
    search.params[`begin${propName}`] = range[0]
    search.params[`end${propName}`] = range[1]
  }
  return search
}

function dictItems(datas: DictData = []) {
  return Array.isArray(datas) ? datas : Object.values(datas)
}

export function selectDictLabel(datas: DictData, value?: DictValue) {
  if (value === undefined) return ''
  const match = dictItems(datas).find((item) => item.value == `${value}`)
  return String(match?.label ?? value)
}

export function selectDictLabels(datas: DictData, value?: DictValue | DictValue[], separator = ',') {
  if (value === undefined || value === null || value === '') return ''
  const values = Array.isArray(value) ? value : String(value).split(separator)
  return values.map((item) => selectDictLabel(datas, item)).join(separator)
}

export function sprintf(str: string, ...args: unknown[]) {
  let index = 0
  let valid = true
  const result = str.replace(/%s/g, () => {
    const arg = args[index++]
    if (typeof arg === 'undefined') {
      valid = false
      return ''
    }
    return String(arg)
  })
  return valid ? result : ''
}

export function parseStrEmpty(str?: DictValue) {
  if (!str || str === 'undefined' || str === 'null') return ''
  return String(str)
}

export function mergeRecursive<T extends Record<string, unknown>>(source: T, target: Record<string, unknown>) {
  const sourceRecord = source as Record<string, unknown>
  for (const key in target) {
    const sourceValue = sourceRecord[key] as Record<string, unknown> | undefined
    const targetValue = target[key]
    if (
      sourceValue &&
      targetValue &&
      typeof sourceValue === 'object' &&
      typeof targetValue === 'object' &&
      !Array.isArray(sourceValue) &&
      !Array.isArray(targetValue)
    ) {
      sourceRecord[key] = mergeRecursive(sourceValue, targetValue as Record<string, unknown>)
    } else {
      sourceRecord[key] = targetValue
    }
  }
  return source
}

export function handleTree<T>(
  data: T[],
  id = 'id',
  parentId = 'parentId',
  children = 'children'
) {
  const childrenListMap: Record<string, T[]> = {}
  const nodeIds: Record<string, T> = {}
  const tree: T[] = []

  for (const item of data) {
    const record = item as Record<string, unknown>
    const parentKey = String(record[parentId] ?? '')
    if (!childrenListMap[parentKey]) {
      childrenListMap[parentKey] = []
    }
    nodeIds[String(record[id])] = item
    childrenListMap[parentKey].push(item)
  }

  for (const item of data) {
    const record = item as Record<string, unknown>
    const parentKey = String(record[parentId] ?? '')
    if (!nodeIds[parentKey]) {
      tree.push(item)
    }
  }

  function adaptToChildrenList(node: T, parent?: T) {
    const record = node as Record<string, unknown>
    if (parent) {
      const parentInfo = { ...(parent as Record<string, unknown>) }
      delete parentInfo[children]
      record.parentInfo = parentInfo
    }

    record[children] = childrenListMap[String(record[id])] || []
    for (const child of record[children] as T[]) {
      adaptToChildrenList(child, node)
    }
  }

  for (const item of tree) {
    adaptToChildrenList(item)
  }
  return tree
}

export function tansParams(params: Record<string, unknown>) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = `${encodeURIComponent(propName)}=`
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object' && !Array.isArray(value)) {
        for (const key of Object.keys(value as Record<string, unknown>)) {
          const childValue = (value as Record<string, unknown>)[key]
          if (childValue !== null && childValue !== '' && typeof childValue !== 'undefined') {
            const param = `${propName}[${key}]`
            result += `${encodeURIComponent(param)}=${encodeURIComponent(String(childValue))}&`
          }
        }
      } else {
        result += `${part}${encodeURIComponent(String(value))}&`
      }
    }
  }
  return result
}

export function getNormalPath(path: string) {
  if (path.length === 0 || !path || path === 'undefined') return path
  const result = path.replace(/\/+/g, '/')
  return result[result.length - 1] === '/' ? result.slice(0, result.length - 1) : result
}

export function blobValidate(data: Blob | { type?: string }) {
  return data.type !== 'application/json'
}

export function numSub(num1: number, num2: number) {
  const baseNum1 = num1.toString().split('.')[1]?.length ?? 0
  const baseNum2 = num2.toString().split('.')[1]?.length ?? 0
  const baseNum = Math.pow(10, Math.max(baseNum1, baseNum2))
  const precision = baseNum1 >= baseNum2 ? baseNum1 : baseNum2
  return Number(((num1 * baseNum - num2 * baseNum) / baseNum).toFixed(precision))
}

export function generateNo() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const randomNum = Math.floor(Math.random() * 10000)
  return `${month}${day}${String(randomNum).padStart(4, '0')}`
}
