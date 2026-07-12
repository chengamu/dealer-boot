import { canonicalDecimal, decimalText, padDecimal, shiftDecimal, type DecimalInput } from './numeric/decimal'
import {
  resolveNumericContract,
  validateNumeric,
  type BusinessNumberMode,
  type NumericContract,
  type NumericContractOptions
} from './numeric/contract'
import { decimalToInchParts, reducedFraction } from './numeric/inch'

export type BusinessNumberValue = DecimalInput
export type BusinessNumberFormatOptions = NumericContractOptions & { emptyText?: string }
export type { BusinessNumberMode, NumericContract }
export { canonicalDecimal, decimalText, resolveNumericContract, validateNumeric }
export { addDecimals, compareDecimals, decimalScale, decimalToMinorUnits, minorUnitsToDecimal, shiftDecimal, subtractDecimals, sumDecimals } from './numeric/decimal'
export { decimalToInchParts, inchPartsToDecimal, isExactInch, isSupportedDenominator } from './numeric/inch'

export function formatBusinessNumber(value: BusinessNumberValue, mode: BusinessNumberMode,
                                     options: BusinessNumberFormatOptions = {}): string {
  const contract = resolveNumericContract(mode, options)
  const result = validateNumeric(value, contract)
  if (result.value === null) return options.emptyText ?? '-'
  if (!result.valid) return result.value
  return padDecimal(result.value, contract.minScale)
}

export function formatQuantity(value: BusinessNumberValue, unitPrecision = 6, emptyText = '-') {
  return formatBusinessNumber(value, 'QUANTITY', { unitPrecision, emptyText })
}

export function formatUnitPrice(value: BusinessNumberValue, emptyText = '-') {
  return formatBusinessNumber(value, 'UNIT_PRICE', { emptyText })
}

export function formatMoney(value: BusinessNumberValue, currencyDigits = 2, emptyText = '-') {
  return formatBusinessNumber(value, 'MONEY', { currencyDigits, emptyText })
}

export function formatCurrency(value: BusinessNumberValue, currencyCode = 'USD', currencyDigits = 2, emptyText = '-') {
  const amount = formatMoney(value, currencyDigits, emptyText)
  if (amount === emptyText) return amount
  return currencyCode === 'USD' ? `$${amount}` : `${currencyCode} ${amount}`
}

export function formatRate(value: BusinessNumberValue, maxFractionDigits = 6, emptyText = '-') {
  return formatBusinessNumber(value, 'RATIO', { maxFractionDigits, emptyText })
}

export function formatRatioAsPercent(value: BusinessNumberValue, maxFractionDigits = 4, emptyText = '-') {
  const shifted = shiftDecimal(value, 2)
  return shifted === null ? emptyText : `${formatBusinessNumber(shifted, 'PERCENT', { maxFractionDigits, emptyText })}%`
}

export function formatInch(value: BusinessNumberValue, denominator = 8, emptyText = '-'): string {
  const text = canonicalDecimal(value)
  if (text === null) return emptyText
  const parts = decimalToInchParts(text, denominator)
  if (!parts) return `${text} in (!)`
  const fraction = reducedFraction(parts.numerator, parts.denominator)
  return parts.numerator === 0 ? `${parts.whole} in` : `${parts.whole} ${fraction} in`
}

export function formatInchRange(minWidth: BusinessNumberValue, maxWidth: BusinessNumberValue,
                                minHeight: BusinessNumberValue, maxHeight: BusinessNumberValue, denominator = 8) {
  const value = (input: BusinessNumberValue) => formatInch(input, denominator)
    .replace(/ in \(!\)$/, ' (!)')
    .replace(/ in$/, '')
  return `${value(minWidth)}≤W≤${value(maxWidth)} in, ${value(minHeight)}≤H≤${value(maxHeight)} in`
}

export function fractionOptions(denominator = 8) {
  return Array.from({ length: denominator }, (_, numerator) => ({
    label: reducedFraction(numerator, denominator),
    value: numerator
  }))
}
