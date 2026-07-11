export type BusinessNumberMode = 'COUNT' | 'QUANTITY' | 'UNIT_PRICE' | 'MONEY' | 'RATE'
export type BusinessNumberValue = number | string | null | undefined

export interface BusinessNumberFormatOptions {
  minFractionDigits?: number
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  emptyText?: string
}

const DECIMAL_PATTERN = /^[-+]?(?:\d+(?:\.\d*)?|\.\d+)(?:e[-+]?\d+)?$/i

export function decimalText(value: BusinessNumberValue): string | null {
  if (value === null || value === undefined || value === '') return null
  const text = String(value).trim()
  if (!DECIMAL_PATTERN.test(text)) return null
  return expandExponent(text)
}

export function formatBusinessNumber(
  value: BusinessNumberValue,
  mode: BusinessNumberMode,
  options: BusinessNumberFormatOptions = {}
): string {
  const text = decimalText(value)
  if (text === null) return options.emptyText ?? '-'
  const digits = resolveFractionDigits(mode, options)
  const rounded = roundDecimalText(text, digits.max)
  return formatFractionDigits(rounded, digits.min)
}

export function formatQuantity(value: BusinessNumberValue, unitPrecision = 6, emptyText = '-'): string {
  return formatBusinessNumber(value, 'QUANTITY', { unitPrecision, emptyText })
}

export function formatUnitPrice(value: BusinessNumberValue, emptyText = '-'): string {
  return formatBusinessNumber(value, 'UNIT_PRICE', { emptyText })
}

export function formatMoney(value: BusinessNumberValue, currencyDigits = 2, emptyText = '-'): string {
  return formatBusinessNumber(value, 'MONEY', { currencyDigits, emptyText })
}

export function formatRate(value: BusinessNumberValue, maxFractionDigits = 6, emptyText = '-'): string {
  return formatBusinessNumber(value, 'RATE', { maxFractionDigits, emptyText })
}

export function formatInch(value: BusinessNumberValue, denominator = 8, emptyText = '-'): string {
  const text = decimalText(value)
  if (text === null) return emptyText
  const numeric = Number(text)
  if (!Number.isFinite(numeric)) return emptyText
  const sign = numeric < 0 ? '-' : ''
  const absolute = Math.abs(numeric)
  let whole = Math.floor(absolute)
  let numerator = Math.round((absolute - whole) * denominator)
  if (numerator === denominator) {
    whole += 1
    numerator = 0
  }
  if (numerator === 0) return `${sign}${whole} in`
  const divisor = greatestCommonDivisor(numerator, denominator)
  return `${sign}${whole} ${numerator / divisor}/${denominator / divisor} in`
}

export function formatInchRange(minWidth: BusinessNumberValue, maxWidth: BusinessNumberValue,
                                minHeight: BusinessNumberValue, maxHeight: BusinessNumberValue, denominator = 8) {
  const value = (input: BusinessNumberValue) => formatInch(input, denominator).replace(/ in$/, '')
  return `${value(minWidth)}≤W≤${value(maxWidth)} in, ${value(minHeight)}≤H≤${value(maxHeight)} in`
}

export function fractionOptions(denominator = 8) {
  return Array.from({ length: denominator }, (_, numerator) => {
    if (numerator === 0) return { label: '0', value: 0 }
    const divisor = greatestCommonDivisor(numerator, denominator)
    return { label: `${numerator / divisor}/${denominator / divisor}`, value: numerator }
  })
}

function resolveFractionDigits(mode: BusinessNumberMode, options: BusinessNumberFormatOptions) {
  if (mode === 'COUNT') return { min: 0, max: 0 }
  if (mode === 'MONEY') {
    const digits = options.currencyDigits ?? 2
    return { min: digits, max: digits }
  }
  const defaultMax = mode === 'UNIT_PRICE' ? 4 : mode === 'QUANTITY' ? options.unitPrecision ?? 6 : 6
  const max = Math.max(0, options.maxFractionDigits ?? defaultMax)
  const min = Math.min(max, Math.max(0, options.minFractionDigits ?? 2))
  return { min, max }
}

function formatFractionDigits(value: string, minFractionDigits: number) {
  const negative = value.startsWith('-')
  const unsigned = negative || value.startsWith('+') ? value.slice(1) : value
  const [integerPart = '0', fractionPart = ''] = unsigned.split('.')
  let fraction = fractionPart.replace(/0+$/, '')
  if (fraction.length < minFractionDigits) fraction = fraction.padEnd(minFractionDigits, '0')
  return `${negative ? '-' : ''}${integerPart || '0'}${fraction ? `.${fraction}` : ''}`
}

function roundDecimalText(value: string, scale: number) {
  const negative = value.startsWith('-')
  const unsigned = value.replace(/^[-+]/, '')
  const [rawInteger = '0', rawFraction = ''] = unsigned.split('.')
  const integer = rawInteger.replace(/^0+(?=\d)/, '') || '0'
  if (rawFraction.length <= scale) return `${negative ? '-' : ''}${integer}${rawFraction ? `.${rawFraction}` : ''}`
  const kept = rawFraction.slice(0, scale)
  if (rawFraction.charCodeAt(scale) < 53) return `${negative ? '-' : ''}${integer}${scale ? `.${kept}` : ''}`
  const digits = `${integer}${kept}`.split('')
  let carry = 1
  for (let index = digits.length - 1; index >= 0 && carry; index -= 1) {
    const next = Number(digits[index]) + carry
    digits[index] = String(next % 10)
    carry = next > 9 ? 1 : 0
  }
  if (carry) digits.unshift('1')
  const split = digits.length - scale
  const result = scale ? `${digits.slice(0, split).join('')}.${digits.slice(split).join('')}` : digits.join('')
  return `${negative ? '-' : ''}${result}`
}

function expandExponent(value: string) {
  if (!/[eE]/.test(value)) return value.replace(/^\+/, '')
  const [coefficient, exponentText] = value.toLowerCase().split('e')
  const exponent = Number(exponentText)
  const negative = coefficient.startsWith('-')
  const digits = coefficient.replace(/^[-+]/, '').replace('.', '')
  const decimalIndex = coefficient.replace(/^[-+]/, '').indexOf('.')
  const originalIndex = decimalIndex < 0 ? digits.length : decimalIndex
  const nextIndex = originalIndex + exponent
  const expanded = nextIndex <= 0
    ? `0.${'0'.repeat(-nextIndex)}${digits}`
    : nextIndex >= digits.length
      ? `${digits}${'0'.repeat(nextIndex - digits.length)}`
      : `${digits.slice(0, nextIndex)}.${digits.slice(nextIndex)}`
  return `${negative ? '-' : ''}${expanded}`
}

function greatestCommonDivisor(left: number, right: number): number {
  let a = Math.abs(left)
  let b = Math.abs(right)
  while (b) [a, b] = [b, a % b]
  return a || 1
}
