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
  object?: ExpressionNode
  property?: ExpressionNode
  test?: ExpressionNode
  consequent?: ExpressionNode
  alternate?: ExpressionNode
}

export interface ExpressionValidationResult {
  valid: boolean
  expression: string
  message?: string
  sampleValue?: number | boolean | string
}

export interface FormulaVariable {
  label: string
  name: string
  sample: number | string
}

const VARIABLE_ALIASES: Record<string, string> = {
  订单长: 'orderLength',
  长: 'orderLength',
  订单宽: 'orderWidth',
  宽: 'orderWidth',
  订单高: 'orderHeight',
  订单厚: 'orderHeight',
  高: 'orderHeight',
  厚度: 'orderHeight',
  订单重量: 'orderWeight',
  重量: 'orderWeight',
  订单面积: 'orderArea',
  面积: 'orderArea',
  店铺: 'store',
  面料: 'fabric',
  产品类型: 'productType',
  配置项值: 'optionValue'
}

const FORMULA_VARIABLES = new Set(['orderLength', 'orderWidth', 'orderHeight', 'orderWeight', 'orderArea'])
const CONDITION_VARIABLES = new Set(['orderLength', 'orderWidth', 'orderHeight', 'orderWeight', 'orderArea', 'store', 'fabric', 'productType', 'optionValue'])
const SAMPLE_CONTEXT: Record<string, number | string> = {
  orderLength: 18,
  orderWidth: 12,
  orderHeight: 20,
  orderWeight: 3,
  orderArea: 240,
  store: 'SHOP_A',
  fabric: 'XLF241801',
  productType: 'CUSTOM_CURTAIN',
  optionValue: 'MOTOR'
}

export const formulaVariables: FormulaVariable[] = [
  { label: '订单长', name: 'orderLength', sample: 18 },
  { label: '订单宽', name: 'orderWidth', sample: 12 },
  { label: '订单厚', name: 'orderHeight', sample: 20 },
  { label: '订单重量', name: 'orderWeight', sample: 3 },
  { label: '订单面积', name: 'orderArea', sample: 240 },
  { label: '面料', name: 'fabric', sample: 'XLF241801' },
  { label: '产品类型', name: 'productType', sample: 'CUSTOM_CURTAIN' },
  { label: '配置项值', name: 'optionValue', sample: 'MOTOR' }
]

export function normalizeFormulaExpression(input?: string) {
  return normalizeBase(input)
}

export function normalizeConditionExpression(input?: string) {
  return normalizeBase(input)
    .replace(/并且|且/g, '&&')
    .replace(/或者|或/g, '||')
    .replace(/(?<![!<>=])=(?![=])/g, '==')
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

export function conditionExpressionForOption(optionCode?: string, valueCode?: string) {
  if (!optionCode || !valueCode) return ''
  const variableName = optionCode === 'FABRIC' ? 'fabric' : `option_${optionCode}`
  return `${variableName} == "${valueCode}"`
}

export function conditionKeyForOption(optionCode?: string, valueCode?: string) {
  if (!optionCode || !valueCode) return ''
  return `OPTION:${optionCode}:${valueCode}`
}

export function defaultConditionKey() {
  return 'DEFAULT'
}

export function formatUsageNumber(value?: number | string | null) {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue.toFixed(2) : '-'
}

function normalizeBase(input?: string) {
  let expression = String(input || '').trim()
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
  return expression
}

function validateFormulaNode(node: ExpressionNode) {
  if (node.type === 'Literal') {
    if (typeof node.value !== 'number') throw new Error('formula literal must be number')
    return
  }
  if (node.type === 'Identifier') {
    if (!node.name || !FORMULA_VARIABLES.has(node.name)) throw new Error(`unknown variable: ${node.name || ''}`)
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
  throw new Error(`unsupported formula node: ${node.type}`)
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
  throw new Error('unsupported expression')
}

function sampleDynamicVariable(name?: string) {
  if (!name) return 0
  if (name.startsWith('option_')) return 'MOTOR'
  if (name.startsWith('material_') && name.endsWith('_materialType')) return 'MOTOR'
  if (name.startsWith('material_') && name.endsWith('_materialCode')) return 'XLF241801'
  if (name.startsWith('material_') && name.endsWith('_materialName')) return 'XLF241801 Cream'
  if (name.startsWith('material_') && name.endsWith('_attributeGroup')) return 'FABRIC'
  return 0
}

function requiredNode(node?: ExpressionNode) {
  if (!node) throw new Error('missing expression node')
  return node
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
