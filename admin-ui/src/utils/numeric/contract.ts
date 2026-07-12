import { canonicalDecimal, compareDecimals, decimalScale, type DecimalInput } from './decimal'

export type BusinessNumberMode = 'COUNT' | 'QUANTITY' | 'UNIT_PRICE' | 'MONEY' | 'RATIO' | 'PERCENT' | 'RATE'

export interface NumericContract {
  mode: BusinessNumberMode
  minScale: number
  maxScale: number
  min?: string
  max?: string
  allowNegative: boolean
  allowZero: boolean
  unitPrecision?: number
  currencyDigits?: number
}

export type NumericValidationCode = 'INVALID_DECIMAL' | 'SCALE_EXCEEDED' | 'NEGATIVE_NOT_ALLOWED'
  | 'ZERO_NOT_ALLOWED' | 'BELOW_MINIMUM' | 'ABOVE_MAXIMUM' | 'INTEGER_REQUIRED'

export interface NumericValidationResult {
  valid: boolean
  value: string | null
  code?: NumericValidationCode
}

export interface NumericContractOptions {
  minFractionDigits?: number
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  min?: DecimalInput
  max?: DecimalInput
  allowNegative?: boolean
  allowZero?: boolean
}

export function resolveNumericContract(mode: BusinessNumberMode, options: NumericContractOptions = {}): NumericContract {
  const normalizedMode = mode === 'RATE' ? 'RATIO' : mode
  const currencyDigits = options.currencyDigits ?? 2
  const defaultMax = normalizedMode === 'COUNT' ? 0
    : normalizedMode === 'UNIT_PRICE' ? 4
      : normalizedMode === 'MONEY' ? currencyDigits
        : normalizedMode === 'QUANTITY' ? options.unitPrecision ?? 6 : 6
  const maxScale = Math.max(0, options.maxFractionDigits ?? defaultMax)
  const defaultMin = normalizedMode === 'COUNT' ? 0 : normalizedMode === 'MONEY' ? maxScale : 2
  return {
    mode: normalizedMode,
    minScale: Math.min(maxScale, Math.max(0, options.minFractionDigits ?? defaultMin)),
    maxScale,
    min: canonicalDecimal(options.min) ?? undefined,
    max: canonicalDecimal(options.max) ?? undefined,
    allowNegative: options.allowNegative ?? false,
    allowZero: options.allowZero ?? true,
    unitPrecision: options.unitPrecision,
    currencyDigits
  }
}

export function validateNumeric(value: DecimalInput, contract: NumericContract): NumericValidationResult {
  if (value === null || value === undefined || value === '') return { valid: true, value: null }
  const normalized = canonicalDecimal(value)
  if (normalized === null) return { valid: false, value: null, code: 'INVALID_DECIMAL' }
  const scale = decimalScale(normalized) ?? 0
  if (scale > contract.maxScale) return { valid: false, value: normalized, code: 'SCALE_EXCEEDED' }
  if (contract.mode === 'COUNT' && scale !== 0) return { valid: false, value: normalized, code: 'INTEGER_REQUIRED' }
  if (!contract.allowNegative && normalized.startsWith('-')) return { valid: false, value: normalized, code: 'NEGATIVE_NOT_ALLOWED' }
  if (!contract.allowZero && compareDecimals(normalized, '0') === 0) return { valid: false, value: normalized, code: 'ZERO_NOT_ALLOWED' }
  if (contract.min !== undefined && (compareDecimals(normalized, contract.min) ?? 0) < 0) {
    return { valid: false, value: normalized, code: 'BELOW_MINIMUM' }
  }
  if (contract.max !== undefined && (compareDecimals(normalized, contract.max) ?? 0) > 0) {
    return { valid: false, value: normalized, code: 'ABOVE_MAXIMUM' }
  }
  return { valid: true, value: normalized }
}
