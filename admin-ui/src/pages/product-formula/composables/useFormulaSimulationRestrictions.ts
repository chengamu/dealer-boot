import jsep from 'jsep'
import { computed, type Ref } from 'vue'
import { normalizeConditionExpression } from '../utils/formulaExpression'
import type { ProductFormulaRestrictionVO, ProductFormulaSimulationBO, ProductFormulaVO } from '@/api/product-capability/types'

type ExpressionNode = {
  type: string
  operator?: string
  name?: string
  value?: unknown
  left?: ExpressionNode
  right?: ExpressionNode
  argument?: ExpressionNode
}

type SimulationRestrictionOptions = {
  form: ProductFormulaSimulationBO
  formula: Ref<ProductFormulaVO>
  restrictions: Ref<ProductFormulaRestrictionVO[]>
  selectedOptionValues: Record<string, string>
}

export function useFormulaSimulationRestrictions(options: SimulationRestrictionOptions) {
  const matchedRestrictions = computed(() => options.restrictions.value
    .filter((row) => row.status === 'ENABLED' && row.actionType === 'DISABLE')
    .filter((row) => restrictionMatched(row, context())))

  const disabledOptionValues = computed(() => {
    const result: Record<string, string[]> = {}
    matchedRestrictions.value.forEach((row) => {
      if (!row.targetOptionCode) return
      result[row.targetOptionCode] ||= []
      result[row.targetOptionCode].push(row.targetValueCode || '*')
    })
    return result
  })

  const restrictionMessages = computed(() => {
    const result: Record<string, string[]> = {}
    matchedRestrictions.value.forEach((row) => {
      if (!row.targetOptionCode) return
      result[row.targetOptionCode] ||= []
      result[row.targetOptionCode].push(row.messageText || 'product.formula.simulationRestrictionHit')
    })
    return result
  })

  const hasRestrictedSelection = computed(() => Object.entries(disabledOptionValues.value).some(([optionCode, disabledCodes]) => {
    const selectedCodes = splitCodes(options.selectedOptionValues[optionCode])
    return selectedCodes.some((code) => disabledCodes.includes('*') || disabledCodes.includes(code))
  }))

  function context(): Record<string, number | string> {
    const widthIn = Number(options.form.orderWidth || 0)
    const lengthIn = Number(options.form.orderHeight || 0)
    const widthCm = widthIn * 2.54
    const lengthCm = lengthIn * 2.54
    const ctx: Record<string, number | string> = {
      orderWidthIn: widthIn,
      orderLengthIn: lengthIn,
      orderWidthCm: widthCm,
      orderLengthCm: lengthCm,
      orderAreaM2: widthCm * lengthCm / 10000,
      productType: options.formula.value.productTypeCode || '',
      fabric: options.selectedOptionValues.FABRIC || '',
      optionValue: ''
    }
    Object.entries(options.selectedOptionValues).forEach(([code, value]) => {
      ctx[`option_${code}`] = value
    })
    return ctx
  }

  return { disabledOptionValues, restrictionMessages, hasRestrictedSelection }
}

function restrictionMatched(row: ProductFormulaRestrictionVO, ctx: Record<string, number | string>) {
  if (row.conditionType === 'WIDTH') return compareNumber(ctx.orderWidthIn, row.conditionValueNumber, row.conditionOperator)
  if (row.conditionType === 'HEIGHT') return compareNumber(ctx.orderLengthIn, row.conditionValueNumber, row.conditionOperator)
  if (row.conditionType === 'OPTION_VALUE') return compareString(ctx[`option_${row.conditionOptionCode}`], row.conditionValueCode, row.conditionOperator)
  return evaluateCondition(row.conditionExpression || row.conditionText, ctx)
}

function evaluateCondition(expressionText: string | undefined, ctx: Record<string, number | string>) {
  const expression = normalizeConditionExpression(expressionText)
  if (!expression) return false
  try {
    return Boolean(evaluate(jsep(expression) as ExpressionNode, ctx))
  } catch {
    return false
  }
}

function evaluate(node: ExpressionNode, ctx: Record<string, number | string>): number | boolean | string {
  if (node.type === 'Literal') return node.value as number | string
  if (node.type === 'Identifier') return ctx[node.name || ''] ?? ''
  if (node.type === 'UnaryExpression') {
    const value = Number(evaluate(requiredNode(node.argument), ctx))
    return node.operator === '-' ? -value : value
  }
  if (node.type === 'BinaryExpression' || node.type === 'LogicalExpression') {
    const left = evaluate(requiredNode(node.left), ctx)
    if (node.operator === '&&') return Boolean(left) && Boolean(evaluate(requiredNode(node.right), ctx))
    if (node.operator === '||') return Boolean(left) || Boolean(evaluate(requiredNode(node.right), ctx))
    const right = evaluate(requiredNode(node.right), ctx)
    return compareValues(left, right, node.operator)
  }
  return false
}

function compareValues(left: number | boolean | string, right: number | boolean | string, operator?: string) {
  if (operator === '==' || operator === '!=') {
    const equals = selectedMatches(left, right)
    return operator === '!=' ? !equals : equals
  }
  const compared = Number(left) - Number(right)
  if (!Number.isFinite(compared)) return false
  if (operator === '>') return compared > 0
  if (operator === '>=') return compared >= 0
  if (operator === '<') return compared < 0
  if (operator === '<=') return compared <= 0
  if (operator === '+') return Number(left) + Number(right)
  if (operator === '-') return Number(left) - Number(right)
  if (operator === '*') return Number(left) * Number(right)
  if (operator === '/') return Number(right) === 0 ? Number.NaN : Number(left) / Number(right)
  return false
}

function compareNumber(left: unknown, right: unknown, operator?: string) {
  return Boolean(compareValues(Number(left), Number(right), operator || '=='))
}

function compareString(left: unknown, right: unknown, operator?: string) {
  const equals = selectedMatches(left, right)
  return operator === 'NE' || operator === '!=' ? !equals : equals
}

function selectedMatches(left: unknown, right: unknown) {
  return splitCodes(String(left || '')).includes(String(right || ''))
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}

function requiredNode(node?: ExpressionNode) {
  if (!node) throw new Error('missing expression node')
  return node
}
