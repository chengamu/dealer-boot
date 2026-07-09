import jsep from 'jsep'

type ExpressionNode = {
  type: string
  operator?: string
  name?: string
  value?: unknown
  left?: ExpressionNode
  right?: ExpressionNode
  argument?: ExpressionNode
  arguments?: ExpressionNode[]
  callee?: ExpressionNode
}

export type PriceExpressionVariable = {
  label: string
  labelKey?: string
  name: string
  insert?: string
  sample: number | string
}

export type PriceExpressionResult = {
  valid: boolean
  expression: string
  message?: string
  sampleValue?: number | boolean | string
}

export const priceFormulaVariables: PriceExpressionVariable[] = [
  { label: 'unitPrice', labelKey: 'productCenter.pricing.variableUnitPrice', name: 'unitPrice', sample: 50 },
  { label: 'width', labelKey: 'productCenter.pricing.variableWidth', name: 'width', sample: 25 },
  { label: 'drop', labelKey: 'productCenter.pricing.variableDrop', name: 'drop', sample: 72 },
  { label: 'widthCm', labelKey: 'productCenter.pricing.variableWidthCm', name: 'widthCm', insert: 'width * 2.54', sample: 63.5 },
  { label: 'dropCm', labelKey: 'productCenter.pricing.variableDropCm', name: 'dropCm', insert: 'drop * 2.54', sample: 182.88 },
  { label: 'areaM2', labelKey: 'productCenter.pricing.variableAreaM2', name: 'areaM2', insert: 'drop * 2.54 * width * 2.54 / 10000', sample: 1.16 },
  { label: 'areaSqft', labelKey: 'productCenter.pricing.variableAreaSqft', name: 'areaSqft', insert: 'drop * width / 144', sample: 12.5 }
]

export const priceConditionVariables = priceFormulaVariables.filter((item) => item.name !== 'unitPrice')

const FUNCTIONS = new Set(['max', 'min', 'round', 'ceil', 'floor'])
export function priceOperators() {
  return ['+', '-', '*', '/', '(', ')', ',', '>', '>=', '<', '<=', '=', '!=', '&&', '||']
}

export function validatePriceFormula(input?: string) {
  return validateExpression(input, priceFormulaVariables, false)
}

export function validatePriceCondition(input?: string): PriceExpressionResult {
  const text = String(input || '').trim()
  if (!text || text === 'DEFAULT') return { valid: true, expression: 'DEFAULT', sampleValue: true }
  return validateExpression(text, priceConditionVariables, true)
}

export function normalizePriceExpression(input?: string, condition = false) {
  let value = String(input || '').trim()
    .replace(/×/g, '*')
    .replace(/÷/g, '/')
    .replace(/（/g, '(')
    .replace(/）/g, ')')
    .replace(/，/g, ',')
    .replace(/\bMAX\b/g, 'max')
    .replace(/\bMIN\b/g, 'min')
    .replace(/\bROUND\b/g, 'round')
    .replace(/\bCEIL\b/g, 'ceil')
    .replace(/\bFLOOR\b/g, 'floor')
    .replace(/四舍五入/g, 'round')
    .replace(/向上取整/g, 'ceil')
    .replace(/向下取整/g, 'floor')
  if (condition) {
    value = value.replace(/并且|且/g, '&&')
      .replace(/或者|或/g, '||')
      .replace(/(?<![!<>=])=(?![=])/g, '==')
  }
  return value
}

function validateExpression(input: string | undefined, variables: PriceExpressionVariable[], condition: boolean): PriceExpressionResult {
  const expression = normalizePriceExpression(input, condition)
  if (!expression) return { valid: false, expression, message: 'empty' }
  try {
    const node = jsep(expression) as ExpressionNode
    validateNode(node, new Set(variables.map((item) => item.name)), condition)
    const sampleValue = evaluate(node, sampleContext(variables))
    const valid = condition
      ? typeof sampleValue === 'boolean'
      : typeof sampleValue === 'number' && Number.isFinite(sampleValue) && sampleValue >= 0
    return { valid, expression, sampleValue, message: valid ? undefined : condition ? 'conditionMustBeBoolean' : 'invalidResult' }
  } catch (error) {
    return { valid: false, expression, message: error instanceof Error ? error.message : 'invalid' }
  }
}

function validateNode(node: ExpressionNode, variables: Set<string>, condition: boolean) {
  if (node.type === 'Literal') return
  if (node.type === 'Identifier') {
    if (!node.name || (!variables.has(node.name) && !(condition && (node.name.startsWith('option_') || node.name.startsWith('material_'))))) {
      throw new Error(`unknown variable: ${node.name || ''}`)
    }
    return
  }
  if (node.type === 'UnaryExpression') {
    if (!['+', '-'].includes(node.operator || '')) throw new Error('invalid unary operator')
    validateNode(requiredNode(node.argument), variables, condition)
    return
  }
  if (node.type === 'BinaryExpression' || node.type === 'LogicalExpression') {
    const allowed = condition ? ['+', '-', '*', '/', '==', '!=', '>', '>=', '<', '<=', '&&', '||'] : ['+', '-', '*', '/']
    if (!allowed.includes(node.operator || '')) throw new Error('invalid operator')
    validateNode(requiredNode(node.left), variables, condition)
    validateNode(requiredNode(node.right), variables, condition)
    return
  }
  if (node.type === 'CallExpression') {
    if (!node.callee?.name || !FUNCTIONS.has(node.callee.name)) throw new Error('unsupported function')
    ;(node.arguments || []).forEach((item) => validateNode(item, variables, condition))
    return
  }
  throw new Error(`unsupported node: ${node.type}`)
}

function evaluate(node: ExpressionNode, context: Record<string, number | string>): number | boolean | string {
  if (node.type === 'Literal') return node.value as number | string
  if (node.type === 'Identifier') return context[node.name || ''] ?? sampleDynamicValue(node.name)
  if (node.type === 'UnaryExpression') return node.operator === '-' ? -Number(evaluate(requiredNode(node.argument), context)) : Number(evaluate(requiredNode(node.argument), context))
  if (node.type === 'BinaryExpression' || node.type === 'LogicalExpression') return evaluateBinary(node, context)
  if (node.type === 'CallExpression') return evaluateFunction(node, context)
  throw new Error('unsupported expression')
}

function evaluateBinary(node: ExpressionNode, context: Record<string, number | string>) {
  const left = evaluate(requiredNode(node.left), context)
  const right = evaluate(requiredNode(node.right), context)
  switch (node.operator) {
    case '+': return Number(left) + Number(right)
    case '-': return Number(left) - Number(right)
    case '*': return Number(left) * Number(right)
    case '/': return Number(right) === 0 ? Number.NaN : Number(left) / Number(right)
    case '==': return left === right
    case '!=': return left !== right
    case '>': return Number(left) > Number(right)
    case '>=': return Number(left) >= Number(right)
    case '<': return Number(left) < Number(right)
    case '<=': return Number(left) <= Number(right)
    case '&&': return Boolean(left) && Boolean(right)
    case '||': return Boolean(left) || Boolean(right)
    default: throw new Error('invalid operator')
  }
}

function evaluateFunction(node: ExpressionNode, context: Record<string, number | string>) {
  const args = (node.arguments || []).map((item) => Number(evaluate(item, context)))
  const value = args[0] || 0
  const scale = Math.max(0, args[1] || 0)
  const factor = 10 ** scale
  switch (node.callee?.name) {
    case 'max': return Math.max(...args)
    case 'min': return Math.min(...args)
    case 'round': return Math.round(value * factor) / factor
    case 'ceil': return Math.ceil(value * factor) / factor
    case 'floor': return Math.floor(value * factor) / factor
    default: throw new Error('unsupported function')
  }
}

function sampleContext(variables: PriceExpressionVariable[]) {
  return Object.fromEntries(variables.map((item) => [item.name, item.sample]))
}

function sampleDynamicValue(name?: string) {
  if (name?.startsWith('option_')) return 'OPTION_VALUE'
  if (name?.startsWith('material_')) return 'MATERIAL_VALUE'
  return 0
}

function requiredNode(node?: ExpressionNode) {
  if (!node) throw new Error('missing expression node')
  return node
}
