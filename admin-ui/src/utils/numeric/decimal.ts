export type DecimalInput = string | number | null | undefined

const DECIMAL_PATTERN = /^[-+]?(?:\d+(?:\.\d*)?|\.\d+)(?:e[-+]?\d+)?$/i

export function decimalText(value: DecimalInput): string | null {
  if (value === null || value === undefined || value === '') return null
  const text = String(value).trim()
  if (!DECIMAL_PATTERN.test(text)) return null
  return expandExponent(text)
}

export function canonicalDecimal(value: DecimalInput): string | null {
  const text = decimalText(value)
  if (text === null) return null
  const negative = text.startsWith('-')
  const unsigned = text.replace(/^[-+]/, '')
  const [rawInteger = '0', rawFraction = ''] = unsigned.split('.')
  const integer = rawInteger.replace(/^0+(?=\d)/, '') || '0'
  const fraction = rawFraction.replace(/0+$/, '')
  const zero = integer === '0' && !fraction
  return `${negative && !zero ? '-' : ''}${integer}${fraction ? `.${fraction}` : ''}`
}

export function decimalScale(value: DecimalInput): number | null {
  const text = canonicalDecimal(value)
  if (text === null) return null
  return text.split('.')[1]?.length ?? 0
}

export function compareDecimals(left: DecimalInput, right: DecimalInput): number | null {
  const a = canonicalDecimal(left)
  const b = canonicalDecimal(right)
  if (a === null || b === null) return null
  if (a === b) return 0
  const aNegative = a.startsWith('-')
  const bNegative = b.startsWith('-')
  if (aNegative !== bNegative) return aNegative ? -1 : 1
  const sign = aNegative ? -1 : 1
  const [aInteger, aFraction = ''] = a.replace('-', '').split('.')
  const [bInteger, bFraction = ''] = b.replace('-', '').split('.')
  if (aInteger.length !== bInteger.length) return aInteger.length > bInteger.length ? sign : -sign
  if (aInteger !== bInteger) return aInteger > bInteger ? sign : -sign
  const length = Math.max(aFraction.length, bFraction.length)
  const aPadded = aFraction.padEnd(length, '0')
  const bPadded = bFraction.padEnd(length, '0')
  return aPadded === bPadded ? 0 : aPadded > bPadded ? sign : -sign
}

export function padDecimal(value: string, minimumScale: number): string {
  const [integer, fraction = ''] = value.split('.')
  const padded = fraction.padEnd(minimumScale, '0')
  return padded ? `${integer}.${padded}` : integer
}

export function addDecimals(left: DecimalInput, right: DecimalInput): string {
  const a = canonicalDecimal(left) ?? '0'
  const b = canonicalDecimal(right) ?? '0'
  const aScale = a.split('.')[1]?.length ?? 0
  const bScale = b.split('.')[1]?.length ?? 0
  const scale = Math.max(aScale, bScale)
  const sum = toScaledInteger(a, scale) + toScaledInteger(b, scale)
  return fromScaledInteger(sum, scale)
}

export function subtractDecimals(left: DecimalInput, right: DecimalInput): string {
  const normalized = canonicalDecimal(right) ?? '0'
  const negative = normalized.startsWith('-') ? normalized.slice(1) : `-${normalized}`
  return addDecimals(left, negative)
}

export function sumDecimals(values: DecimalInput[]): string {
  let total = '0'
  values.forEach((value) => { total = addDecimals(total, value) })
  return total
}

export function decimalToMinorUnits(value: DecimalInput, currencyDigits = 2): string | null {
  const normalized = canonicalDecimal(value)
  if (normalized === null) return null
  const scale = normalized.split('.')[1]?.length ?? 0
  if (scale > currencyDigits) return null
  return toScaledInteger(normalized, currencyDigits).toString()
}

export function minorUnitsToDecimal(value: DecimalInput, currencyDigits = 2): string | null {
  const normalized = canonicalDecimal(value)
  if (normalized === null || normalized.includes('.')) return null
  return fromScaledInteger(BigInt(normalized), currencyDigits)
}

export function shiftDecimal(value: DecimalInput, places: number): string | null {
  const normalized = canonicalDecimal(value)
  if (normalized === null || !Number.isInteger(places)) return null
  const negative = normalized.startsWith('-')
  const unsigned = normalized.replace('-', '')
  const [integer, fraction = ''] = unsigned.split('.')
  const digits = `${integer}${fraction}`
  const currentPoint = integer.length
  const nextPoint = currentPoint + places
  const shifted = nextPoint <= 0
    ? `0.${'0'.repeat(-nextPoint)}${digits}`
    : nextPoint >= digits.length
      ? `${digits}${'0'.repeat(nextPoint - digits.length)}`
      : `${digits.slice(0, nextPoint)}.${digits.slice(nextPoint)}`
  return canonicalDecimal(`${negative ? '-' : ''}${shifted}`)
}

function toScaledInteger(value: string, scale: number) {
  const negative = value.startsWith('-')
  const [integer, fraction = ''] = value.replace('-', '').split('.')
  const digits = `${integer}${fraction.padEnd(scale, '0')}`
  return BigInt(`${negative ? '-' : ''}${digits}`)
}

function fromScaledInteger(value: bigint, scale: number) {
  const negative = value < 0n
  const digits = (negative ? -value : value).toString().padStart(scale + 1, '0')
  const text = scale ? `${digits.slice(0, -scale)}.${digits.slice(-scale)}` : digits
  return canonicalDecimal(`${negative ? '-' : ''}${text}`) ?? '0'
}

function expandExponent(value: string) {
  if (!/[eE]/.test(value)) return value.replace(/^\+/, '')
  const [coefficient, exponentText] = value.toLowerCase().split('e')
  const exponent = Number(exponentText)
  const negative = coefficient.startsWith('-')
  const unsigned = coefficient.replace(/^[-+]/, '')
  const digits = unsigned.replace('.', '')
  const decimalIndex = unsigned.indexOf('.')
  const originalIndex = decimalIndex < 0 ? digits.length : decimalIndex
  const nextIndex = originalIndex + exponent
  const expanded = nextIndex <= 0
    ? `0.${'0'.repeat(-nextIndex)}${digits}`
    : nextIndex >= digits.length
      ? `${digits}${'0'.repeat(nextIndex - digits.length)}`
      : `${digits.slice(0, nextIndex)}.${digits.slice(nextIndex)}`
  return `${negative ? '-' : ''}${expanded}`
}
