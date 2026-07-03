import type { ProductFormulaMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO, ProductFormulaVariableVO } from '@/api/product-capability/types'

type AliasPair = { from: string; to: string }

export function optionVariableName(optionCode?: string) {
  return optionCode === 'FABRIC' ? 'fabric' : `option_${optionCode || 'VALUE'}`
}

export function materialAttributeVariableName(roleCode?: string, fieldName?: string) {
  return `material_${roleCode || 'MATERIAL'}_${fieldName || 'value'}`
}

export function normalizeDisplayExpression(
  input?: string,
  options: ProductFormulaOptionVO[] = [],
  optionValues: ProductFormulaOptionValueVO[] = [],
  materials: ProductFormulaMaterialVO[] = [],
  variables: ProductFormulaVariableVO[] = []
) {
  let expression = String(input || '')
  buildAliasPairs(options, optionValues, materials, variables)
    .sort((left, right) => right.from.length - left.from.length)
    .forEach(({ from, to }) => {
      expression = expression.replace(new RegExp(escapeRegExp(from), 'g'), to)
    })
  return expression
}

function buildAliasPairs(options: ProductFormulaOptionVO[], optionValues: ProductFormulaOptionValueVO[], materials: ProductFormulaMaterialVO[], variables: ProductFormulaVariableVO[]) {
  const aliases: AliasPair[] = []
  options.forEach((option) => {
    const variableName = optionVariableName(option.optionCode)
    pushAlias(aliases, option.optionNameCn, variableName)
    pushAlias(aliases, option.optionCode, variableName)
  })
  optionValues.forEach((value) => {
    pushAlias(aliases, value.valueNameCn, value.valueCode)
  })
  buildMaterialAliasPairs(aliases, materials)
  variables.forEach((variable) => {
    pushAlias(aliases, variable.variableName, `var_${variable.variableCode}`)
    pushAlias(aliases, variable.variableCode, `var_${variable.variableCode}`)
  })
  pushAlias(aliases, '四舍五入', 'round')
  pushAlias(aliases, '向上取整', 'ceil')
  pushAlias(aliases, '向下取整', 'floor')
  return aliases
}

function buildMaterialAliasPairs(aliases: AliasPair[], materials: ProductFormulaMaterialVO[]) {
  const roleCodes = new Set<string>()
  materials.forEach((material) => {
    if (!material.attributeGroupCode || roleCodes.has(material.attributeGroupCode)) return
    roleCodes.add(material.attributeGroupCode)
    const roleName = material.attributeGroupNameCn || material.attributeGroupCode
    pushAlias(aliases, `${roleName}.类型`, materialAttributeVariableName(material.attributeGroupCode, 'materialType'))
    pushAlias(aliases, `${roleName}.编码`, materialAttributeVariableName(material.attributeGroupCode, 'materialCode'))
    pushAlias(aliases, `${roleName}.名称`, materialAttributeVariableName(material.attributeGroupCode, 'materialName'))
    pushAlias(aliases, `${roleName}.名字`, materialAttributeVariableName(material.attributeGroupCode, 'materialName'))
    pushAlias(aliases, `${roleName}.物料类型`, materialAttributeVariableName(material.attributeGroupCode, 'materialType'))
    pushAlias(aliases, `${roleName}.物料编码`, materialAttributeVariableName(material.attributeGroupCode, 'materialCode'))
    pushAlias(aliases, `${roleName}.物料名称`, materialAttributeVariableName(material.attributeGroupCode, 'materialName'))
    pushAlias(aliases, `${roleName}.属性分组`, materialAttributeVariableName(material.attributeGroupCode, 'attributeGroup'))
  })
  materials.forEach((material) => {
    pushAlias(aliases, material.materialTypeNameCn, material.materialTypeCode)
    pushAlias(aliases, material.materialNameCn, material.materialCode)
    pushAlias(aliases, material.attributeGroupNameCn, material.attributeGroupCode)
  })
}

function pushAlias(aliases: AliasPair[], from?: string, to?: string) {
  if (!from || !to || from === to) return
  if (aliases.some((item) => item.from === from)) return
  aliases.push({ from, to })
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
