import { canonicalDecimal, type DecimalInput } from './decimal'

export interface InchParts {
  whole: string
  numerator: number
  denominator: number
}

export function decimalToInchParts(value: DecimalInput, denominator = 8): InchParts | null {
  const text = canonicalDecimal(value)
  if (text === null || text.startsWith('-') || !isSupportedDenominator(denominator)) return null
  const [integer = '0', fraction = ''] = text.split('.')
  const scaleBase = 10n ** BigInt(fraction.length)
  const unscaled = BigInt(`${integer}${fraction}`)
  const scaledNumerator = unscaled * BigInt(denominator)
  if (scaledNumerator % scaleBase !== 0n) return null
  const totalNumerator = scaledNumerator / scaleBase
  return {
    whole: String(totalNumerator / BigInt(denominator)),
    numerator: Number(totalNumerator % BigInt(denominator)),
    denominator
  }
}

export function inchPartsToDecimal(whole: string, numerator: number, denominator = 8): string | null {
  if (!/^\d+$/.test(whole) || !Number.isInteger(numerator) || numerator < 0 || numerator >= denominator) return null
  if (!isSupportedDenominator(denominator)) return null
  const total = BigInt(whole) * BigInt(denominator) + BigInt(numerator)
  const integer = total / BigInt(denominator)
  let remainder = total % BigInt(denominator)
  if (remainder === 0n) return String(integer)
  let fraction = ''
  for (let index = 0; remainder !== 0n && index < 18; index += 1) {
    remainder *= 10n
    fraction += String(remainder / BigInt(denominator))
    remainder %= BigInt(denominator)
  }
  return remainder === 0n ? canonicalDecimal(`${integer}.${fraction}`) : null
}

export function isExactInch(value: DecimalInput, denominator = 8) {
  return value === null || value === undefined || value === '' || decimalToInchParts(value, denominator) !== null
}

export function isSupportedDenominator(denominator: number) {
  return Number.isInteger(denominator) && denominator > 0 && denominator <= 16 && (16 % denominator === 0)
}

export function reducedFraction(numerator: number, denominator: number) {
  if (numerator === 0) return '0'
  const divisor = greatestCommonDivisor(numerator, denominator)
  return `${numerator / divisor}/${denominator / divisor}`
}

function greatestCommonDivisor(left: number, right: number): number {
  let a = Math.abs(left)
  let b = Math.abs(right)
  while (b) [a, b] = [b, a % b]
  return a || 1
}
