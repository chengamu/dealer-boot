import jsep from 'jsep'
import { normalizeConditionExpression, normalizeFormulaExpression } from './formulaNormalize'
import { formatQuantity } from '@/utils/businessNumber'

export { formulaVariables, normalizeConditionExpression, normalizeFormulaExpression } from './formulaNormalize'
export type { FormulaVariable } from './formulaNormalize'

type ExpressionNode = {
  type: string
  operator?: string
  name?: string
  value?: unknown
  left?: ExpressionNode
  right?: ExpressionNode
  argument?: ExpressionNode
  arguments?: ExpressionNode[]
  object?: ExpressionNode
  property?: ExpressionNode
  test?: ExpressionNode
  consequent?: ExpressionNode
  alternate?: ExpressionNode
  callee?: ExpressionNode
}

export interface ExpressionValidationResult {
  valid: boolean
  expression: string
  message?: string
  sampleValue?: number | boolean | string
}

const FORMULA_VARIABLES = new Set(['orderWidthIn', 'orderHeightIn', 'orderWidthCm', 'orderHeightCm', 'orderAreaM2'])
const CONDITION_VARIABLES = new Set(['orderWidthIn', 'orderHeightIn', 'orderWidthCm', 'orderHeightCm', 'orderAreaM2', 'store', 'fabric', 'productType', 'optionValue'])
const SAMPLE_CONTEXT: Record<string, number | string> = {
  orderWidthIn: 12,
  orderHeightIn: 20,
  orderWidthCm: 30.48,
  orderHeightCm: 50.8,
  orderAreaM2: 0.1548,
  store: 'SHOP_A',
  fabric: 'XLF241801',
  productType: 'CUSTOM_CURTAIN',
  optionValue: 'MOTOR'
}

export function validateFormulaExpression(input?: string): ExpressionValidationResult {
  const expression = normalizeFormulaExpression(input)
  if (!expression) return { valid: false, expression, message: 'empty' }
  try {
    const node = jsep(expression) as ExpressionNode
    validateFormulaNode(node)
    const sampleValue = evaluate(node)
    if (typeof sampleValue !== 'number' || !Number.isFinite(sampleValue) || sampleValue < 0) {
      return { valid: false, expression, message: 'invalidResult' }
    }
    return { valid: true, expression, sampleValue }
  } catch (error) {
    return { valid: false, expression, message: error instanceof Error ? error.message : 'invalid' }
  }
}

export function validateConditionExpression(input?: string): ExpressionValidationResult {
  const expression = normalizeConditionExpression(input)
  if (!expression) return { valid: false, expression, message: 'empty' }
  if (expression === 'DEFAULT') return { valid: true, expression, sampleValue: true }
  try {
    const node = jsep(expression) as ExpressionNode
    validateConditionNode(node)
    const sampleValue = evaluate(node)
    if (typeof sampleValue !== 'boolean') {
      return { valid: false, expression, message: 'conditionMustBeBoolean' }
    }
    return { valid: true, expression, sampleValue }
  } catch (error) {
    return { valid: false, expression, message: error instanceof Error ? error.message : 'invalid' }
  }
}

export function conditionExpressionForOption(optionCode?: string, valueCode?: string, optionRefKey?: string, valueRefKey?: string) {
  const optionRef = optionRefKey || optionCode
  const valueRef = valueRefKey || valueCode
  if (!optionRef || !valueRef) return ''
  return `option_${identifierPart(optionRef)} == "${escapeString(valueRef)}"`
}

export function conditionKeyForOption(optionCode?: string, valueCode?: string, optionRefKey?: string, valueRefKey?: string) {
  const optionRef = optionRefKey || optionCode
  const valueRef = valueRefKey || valueCode
  if (!optionRef || !valueRef) return ''
  return `OPTION:${optionRef}:${valueRef}`
}

export function defaultConditionKey() {
  return 'DEFAULT'
}

export function formatUsageNumber(value?: number | string | null) {
  return formatQuantity(value)
}

function validateFormulaNode(node: ExpressionNode) {
  if (node.type === 'Literal') {
    if (typeof node.value !== 'number') throw new Error('formula literal must be number')
    return
  }
  if (node.type === 'Identifier') {
    if (!node.name || (!FORMULA_VARIABLES.has(node.name) && !node.name.startsWith('var_'))) throw new Error(`unknown variable: ${node.name || ''}`)
    return
  }
  if (node.type === 'UnaryExpression') {
    if (node.operator !== '+' && node.operator !== '-') throw new Error('invalid unary operator')
    validateFormulaNode(requiredNode(node.argument))
    return
  }
  if (node.type === 'BinaryExpression') {
    if (!['+', '-', '*', '/'].includes(node.operator || '')) throw new Error('invalid formula operator')
    validateFormulaNode(requiredNode(node.left))
    validateFormulaNode(requiredNode(node.right))
    return
  }
  if (node.type === 'CallExpression') {
    validateFormulaFunction(node)
    return
  }
  throw new Error(`unsupported formula node: ${node.type}`)
}

function validateFormulaFunction(node: ExpressionNode) {
  const name = node.callee?.name
  if (!name || !['round', 'ceil', 'floor'].includes(name)) throw new Error('unsupported function')
  if (!node.arguments?.length || node.arguments.length > 2) throw new Error('invalid function arguments')
  node.arguments.forEach((argument) => validateFormulaNode(argument))
}

function validateConditionNode(node: ExpressionNode) {
  if (node.type === 'Literal') return
  if (node.type === 'Identifier') {
    if (!node.name || (!CONDITION_VARIABLES.has(node.name) && !node.name.startsWith('option_') && !node.name.startsWith('material_'))) {
      throw new Error(`unknown variable: ${node.name || ''}`)
    }
    return
  }
  if (node.type === 'UnaryExpression') {
    if (node.operator !== '+' && node.operator !== '-') throw new Error('invalid unary operator')
    validateConditionNode(requiredNode(node.argument))
    return
  }
  if (node.type === 'BinaryExpression') {
    if (['&&', '||'].includes(node.operator || '')) {
      validateConditionNode(requiredNode(node.left))
      validateConditionNode(requiredNode(node.right))
      return
    }
    if (!['+', '-', '*', '/', '==', '!=', '>', '>=', '<', '<='].includes(node.operator || '')) {
      throw new Error('invalid condition operator')
    }
    validateConditionNode(requiredNode(node.left))
    validateConditionNode(requiredNode(node.right))
    return
  }
  if (node.type === 'LogicalExpression') {
    if (!['&&', '||'].includes(node.operator || '')) throw new Error('invalid logical operator')
    validateConditionNode(requiredNode(node.left))
    validateConditionNode(requiredNode(node.right))
    return
  }
  throw new Error(`unsupported condition node: ${node.type}`)
}

function evaluate(node: ExpressionNode): number | boolean | string {
  if (node.type === 'Literal') return node.value as number | string
  if (node.type === 'Identifier') return SAMPLE_CONTEXT[node.name || ''] ?? sampleDynamicVariable(node.name)
  if (node.type === 'UnaryExpression') {
    const value = Number(evaluate(requiredNode(node.argument)))
    return node.operator === '-' ? -value : value
  }
  if (node.type === 'BinaryExpression') {
    const left = evaluate(requiredNode(node.left))
    const right = evaluate(requiredNode(node.right))
    switch (node.operator) {
      case '&&': return Boolean(left) && Boolean(right)
      case '||': return Boolean(left) || Boolean(right)
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
      default: throw new Error('invalid operator')
    }
  }
  if (node.type === 'LogicalExpression') {
    const left = Boolean(evaluate(requiredNode(node.left)))
    return node.operator === '&&'
      ? left && Boolean(evaluate(requiredNode(node.right)))
      : left || Boolean(evaluate(requiredNode(node.right)))
  }
  if (node.type === 'CallExpression') return evaluateFunction(node)
  throw new Error('unsupported expression')
}

function evaluateFunction(node: ExpressionNode) {
  const args = (node.arguments || []).map((argument) => Number(evaluate(argument)))
  const value = args[0] || 0
  const scale = Math.max(0, args[1] || 0)
  const factor = 10 ** scale
  switch (node.callee?.name) {
    case 'round': return Math.round(value * factor) / factor
    case 'ceil': return Math.ceil(value * factor) / factor
    case 'floor': return Math.floor(value * factor) / factor
    default: throw new Error('unsupported function')
  }
}

function sampleDynamicVariable(name?: string) {
  if (!name) return 0
  if (name.startsWith('option_')) return 'MOTOR'
  if (name.startsWith('material_') && name.endsWith('_materialType')) return 'MOTOR'
  if (name.startsWith('material_') && name.endsWith('_materialCode')) return 'XLF241801'
  if (name.startsWith('material_') && name.endsWith('_materialName')) return 'XLF241801 Cream'
  if (name.startsWith('material_') && name.endsWith('_attributeGroup')) return 'FABRIC'
  if (name.startsWith('var_')) return 1
  return 0
}

function requiredNode(node?: ExpressionNode) {
  if (!node) throw new Error('missing expression node')
  return node
}

function identifierPart(value: string) {
  return value.replace(/[^A-Za-z0-9_]/g, '_')
}

function escapeString(value: string) {
  return String(value || '').replace(/\\/g, '\\\\').replace(/"/g, '\\"')
}
