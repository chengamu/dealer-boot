export const PRODUCT_STATUS_ENABLED = 'ENABLED'
export const PRODUCT_STATUS_DISABLED = 'DISABLED'

export const FORMULA_STATUS = {
  DRAFT: 'DRAFT',
  PENDING_REVIEW: 'PENDING_REVIEW',
  REJECTED: 'REJECTED',
  EFFECTIVE: 'EFFECTIVE',
  STOPPED: 'STOPPED'
} as const

export const FORMULA_VALIDATION_STATUS = {
  NOT_VALIDATED: 'NOT_VALIDATED',
  PASS: 'PASS',
  FAIL: 'FAIL'
} as const

export type FormulaStatus = typeof FORMULA_STATUS[keyof typeof FORMULA_STATUS]
export type FormulaValidationStatus = typeof FORMULA_VALIDATION_STATUS[keyof typeof FORMULA_VALIDATION_STATUS]
export type TranslateFn = (key: string) => string

const formulaStatusLabelKeys: Record<FormulaStatus, string> = {
  [FORMULA_STATUS.DRAFT]: 'productCenter.formula.status.draft',
  [FORMULA_STATUS.PENDING_REVIEW]: 'productCenter.formula.status.pendingReview',
  [FORMULA_STATUS.REJECTED]: 'productCenter.formula.status.rejected',
  [FORMULA_STATUS.EFFECTIVE]: 'productCenter.formula.status.effective',
  [FORMULA_STATUS.STOPPED]: 'productCenter.formula.status.stopped'
}

const formulaValidationStatusLabelKeys: Record<FormulaValidationStatus, string> = {
  [FORMULA_VALIDATION_STATUS.NOT_VALIDATED]: 'productCenter.formula.validation.notValidated',
  [FORMULA_VALIDATION_STATUS.PASS]: 'productCenter.formula.validation.pass',
  [FORMULA_VALIDATION_STATUS.FAIL]: 'productCenter.formula.validation.fail'
}

export function normalizeProductStatus(value: unknown) {
  return value === PRODUCT_STATUS_ENABLED || value === '1' || value === 'true' || value === true || value === 1
    ? PRODUCT_STATUS_ENABLED
    : PRODUCT_STATUS_DISABLED
}

export function formulaStatusOptions(t: TranslateFn) {
  return Object.values(FORMULA_STATUS).map((value) => ({
    label: t(formulaStatusLabelKeys[value]),
    value
  }))
}

export function formulaValidationStatusOptions(t: TranslateFn) {
  return Object.values(FORMULA_VALIDATION_STATUS).map((value) => ({
    label: t(formulaValidationStatusLabelKeys[value]),
    value
  }))
}

export function formulaStatusText(status: string | undefined, t: TranslateFn) {
  return formulaStatusLabelKeys[status as FormulaStatus] ? t(formulaStatusLabelKeys[status as FormulaStatus]) : '-'
}

export function formulaValidationStatusText(status: string | undefined, t: TranslateFn) {
  return formulaValidationStatusLabelKeys[status as FormulaValidationStatus] ? t(formulaValidationStatusLabelKeys[status as FormulaValidationStatus]) : '-'
}

export function formulaStatusTagType(status?: string) {
  if (status === FORMULA_STATUS.EFFECTIVE) return 'success'
  if (status === FORMULA_STATUS.STOPPED || status === FORMULA_STATUS.REJECTED) return 'danger'
  if (status === FORMULA_STATUS.PENDING_REVIEW) return 'warning'
  return 'info'
}

export function formulaValidationTagType(status?: string) {
  if (status === FORMULA_VALIDATION_STATUS.PASS) return 'success'
  if (status === FORMULA_VALIDATION_STATUS.FAIL) return 'danger'
  return 'warning'
}
