export interface FormulaVariable {
  label: string
  name: string
  sample: number | string
}

const VARIABLE_ALIASES: Record<string, string> = {
  '订单宽(in)': 'orderWidthIn',
  '订单宽in': 'orderWidthIn',
  订单宽: 'orderWidthIn',
  宽: 'orderWidthIn',
  '订单长(in)': 'orderLengthIn',
  '订单长in': 'orderLengthIn',
  订单长: 'orderLengthIn',
  长: 'orderLengthIn',
  '订单宽(cm)': 'orderWidthCm',
  '订单宽cm': 'orderWidthCm',
  宽cm: 'orderWidthCm',
  '订单长(cm)': 'orderLengthCm',
  '订单长cm': 'orderLengthCm',
  长cm: 'orderLengthCm',
  '订单面积(m²)': 'orderAreaM2',
  '订单面积m²': 'orderAreaM2',
  '订单面积m2': 'orderAreaM2',
  '面积m²': 'orderAreaM2',
  '面积m2': 'orderAreaM2',
  订单面积: 'orderAreaM2',
  面积: 'orderAreaM2',
  店铺: 'store',
  面料: 'fabric',
  产品类型: 'productType',
  配置项值: 'optionValue'
}

const FUNCTION_ALIASES: Record<string, string> = {
  四舍五入: 'round',
  向上取整: 'ceil',
  向下取整: 'floor'
}

export const formulaVariables: FormulaVariable[] = [
  { label: '订单宽(in)', name: 'orderWidthIn', sample: 12 },
  { label: '订单长(in)', name: 'orderLengthIn', sample: 20 },
  { label: '订单宽(cm)', name: 'orderWidthCm', sample: 30.48 },
  { label: '订单长(cm)', name: 'orderLengthCm', sample: 50.8 },
  { label: '订单面积(m²)', name: 'orderAreaM2', sample: 0.1548 },
  { label: '面料', name: 'fabric', sample: 'XLF241801' },
  { label: '产品类型', name: 'productType', sample: 'CUSTOM_CURTAIN' },
  { label: '配置项值', name: 'optionValue', sample: 'MOTOR' }
]

export function normalizeFormulaExpression(input?: string) {
  return normalizeOutsideQuotes(String(input || '').trim(), normalizeBaseSegment)
}

export function normalizeConditionExpression(input?: string) {
  return normalizeOutsideQuotes(String(input || '').trim(), normalizeConditionSegment)
}

export function normalizeOutsideQuotes(input: string, normalizeSegment: (segment: string) => string) {
  const result: string[] = []
  let segment = ''
  let quote = ''
  let escaped = false

  const flushSegment = () => {
    if (!segment) return
    result.push(normalizeSegment(segment))
    segment = ''
  }

  Array.from(input).forEach((char) => {
    if (quote) {
      result.push(char)
      if (escaped) {
        escaped = false
      } else if (char === '\\') {
        escaped = true
      } else if (char === quote) {
        quote = ''
      }
      return
    }

    if (char === '"' || char === "'") {
      flushSegment()
      quote = char
      result.push(char)
      return
    }

    segment += char
  })

  flushSegment()
  return result.join('')
}

function normalizeConditionSegment(segment: string) {
  return normalizeBaseSegment(segment)
    .replace(/并且|且/g, '&&')
    .replace(/或者|或/g, '||')
    .replace(/(?<![!<>=])=(?![=])/g, '==')
}

function normalizeBaseSegment(input: string) {
  let expression = input
    .replace(/×/g, '*')
    .replace(/÷/g, '/')
    .replace(/（/g, '(')
    .replace(/）/g, ')')
    .replace(/，/g, ',')
  Object.entries(VARIABLE_ALIASES)
    .sort((left, right) => right[0].length - left[0].length)
    .forEach(([label, name]) => {
      expression = expression.replace(new RegExp(escapeRegExp(label), 'g'), name)
    })
  Object.entries(FUNCTION_ALIASES)
    .sort((left, right) => right[0].length - left[0].length)
    .forEach(([label, name]) => {
      expression = expression.replace(new RegExp(escapeRegExp(label), 'g'), name)
    })
  return expression
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
