import { computed, ref, type ComputedRef } from 'vue'
import { ElMessage } from 'element-plus'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import {
  conditionExpressionForOption,
  conditionKeyForOption,
  defaultConditionKey,
  formatUsageNumber,
  validateConditionExpression,
  validateFormulaExpression
} from '../utils/formulaExpression'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import type { ExpressionTarget, FormulaField } from './formulaUsageTypes'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaVariableVO,
  ProductFormulaUsageRuleVO
} from '@/api/product-capability/types'

type TFunction = (key: string) => string

type FormulaUsageRulesProps = {
  usageRow: ProductFormulaMaterialVO | null
  materials?: ProductFormulaMaterialVO[]
  variables?: ProductFormulaVariableVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
}

export function useFormulaUsageRules(
  props: FormulaUsageRulesProps,
  t: TFunction,
  currentUsageRows: ComputedRef<ProductFormulaMaterialVO[]>
) {
  const selectedRule = ref<ProductFormulaUsageRuleVO | null>(null)
  const expressionEditorOpen = ref(false)
  const expressionEditorText = ref('')
  const expressionEditorRow = ref<ProductFormulaUsageRuleVO | null>(null)
  const expressionEditorTarget = ref<ExpressionTarget>('usage')

  const formulaFields: FormulaField[] = [
    { target: 'length', labelKey: 'productCenter.formulaSetup.lengthFormula', valueKey: 'lengthFormula', textKey: 'lengthFormulaText' },
    { target: 'width', labelKey: 'productCenter.formulaSetup.widthFormula', valueKey: 'widthFormula', textKey: 'widthFormulaText' },
    { target: 'height', labelKey: 'productCenter.formulaSetup.heightFormula', valueKey: 'heightFormula', textKey: 'heightFormulaText' },
    { target: 'weight', labelKey: 'productCenter.formulaSetup.weightFormula', valueKey: 'weightFormula', textKey: 'weightFormulaText' },
    { target: 'usage', labelKey: 'productCenter.formulaSetup.quantityFormula', valueKey: 'usageFormula', textKey: 'usageFormulaText' }
  ]

  const currentRules = computed(() => props.usageRules.filter((rule) => rule.materialCode === props.usageRow?.materialCode))

  function cloneValue<T>(value: T): T {
    return JSON.parse(JSON.stringify(value)) as T
  }

  function ensureInitialRule() {
    if (!props.usageRow) return
    if (currentRules.value.length) return
    if (props.usageRow.usageMode === 'FORMULA') {
      addFormulaRule(true)
    } else {
      ensureFixedRule()
    }
  }

  function handleUsageModeChange(value: string | number | boolean | undefined) {
    if (!props.usageRow) return
    if (value === 'FORMULA') {
      ensureFormulaMode()
      ensureDefaultRule()
    } else {
      ensureFixedRule()
    }
  }

  function ensureFixedRule() {
    if (!props.usageRow?.materialCode) return
    removeRulesForCurrent()
    const fixedUsageQty = props.usageRow.fixedUsageQty ?? 1
    props.usageRow.usageMode = 'FIXED'
    props.usageRow.fixedUsageQty = fixedUsageQty
    props.usageRules.push({
      materialCode: props.usageRow.materialCode,
      materialNameCn: props.usageRow.materialNameCn,
      materialId: props.usageRow.materialId,
      formulaMaterialId: props.usageRow.formulaMaterialId,
      ruleName: t('productCenter.formulaSetup.fixedUsageRule'),
      conditionType: 'DEFAULT',
      conditionExpression: 'DEFAULT',
      conditionText: t('productCenter.formulaSetup.defaultUsageRule'),
      conditionKey: defaultConditionKey(),
      usageMode: 'FIXED',
      fixedUsageQty,
      usageFormula: String(fixedUsageQty),
      usageFormulaText: formatUsageNumber(fixedUsageQty),
      calculationUnitCode: props.usageRow.calculationUnitCode || props.usageRow.unitCode,
      defaultRuleFlag: true,
      productionRemark: props.usageRow.productionRemark,
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: 10
    })
  }

  function syncFixedRuleFromRow() {
    if (!props.usageRow) return
    props.usageRow.usageMode = 'FIXED'
    const rule = currentRules.value.find((item) => item.usageMode === 'FIXED') || currentRules.value[0]
    if (!rule) {
      ensureFixedRule()
      return
    }
    const fixedUsageQty = props.usageRow.fixedUsageQty ?? 0
    rule.usageMode = 'FIXED'
    rule.fixedUsageQty = fixedUsageQty
    rule.usageFormula = String(fixedUsageQty)
    rule.usageFormulaText = formatUsageNumber(fixedUsageQty)
    rule.defaultRuleFlag = true
    handleDefaultRuleChange(rule)
  }

  function ensureFormulaMode() {
    if (!props.usageRow) return
    props.usageRow.usageMode = 'FORMULA'
    currentRules.value.forEach((rule) => {
      if (rule.usageMode === 'FIXED') {
        const fixedUsageQty = rule.fixedUsageQty ?? props.usageRow?.fixedUsageQty ?? 1
        rule.usageFormula = String(fixedUsageQty)
        rule.usageFormulaText = formatUsageNumber(fixedUsageQty)
        rule.fixedUsageQty = undefined
      }
      rule.usageMode = 'FORMULA'
    })
    props.usageRow.fixedUsageQty = undefined
  }

  function addFormulaRule(defaultRule: boolean, value?: ProductFormulaOptionValueVO) {
    if (!props.usageRow?.materialCode) return
    props.usageRow.usageMode = 'FORMULA'
    const next = currentRules.value.length + 1
    const rule: ProductFormulaUsageRuleVO = {
      materialCode: props.usageRow.materialCode,
      materialNameCn: props.usageRow.materialNameCn,
      materialId: props.usageRow.materialId,
      formulaMaterialId: props.usageRow.formulaMaterialId,
      ruleName: defaultRule ? t('productCenter.formulaSetup.defaultUsageRule') : (value?.valueNameCn || t('productCenter.formulaSetup.conditionalUsageRule')),
      conditionType: defaultRule ? 'DEFAULT' : 'EXPRESSION',
      conditionOptionCode: defaultRule ? undefined : value?.optionCode || 'FABRIC',
      conditionOptionNameCn: defaultRule ? undefined : optionName(value?.optionCode || 'FABRIC'),
      conditionValueCode: defaultRule ? undefined : value?.valueCode,
      conditionValueNameCn: defaultRule ? undefined : value?.valueNameCn,
      conditionExpression: defaultRule ? 'DEFAULT' : conditionExpressionForOption(value?.optionCode || 'FABRIC', value?.valueCode),
      conditionText: defaultRule ? t('productCenter.formulaSetup.defaultUsageRule') : conditionText(value?.optionCode || 'FABRIC', value?.valueCode),
      conditionKey: defaultRule ? defaultConditionKey() : conditionKeyForOption(value?.optionCode || 'FABRIC', value?.valueCode),
      usageMode: 'FORMULA',
      fixedUsageQty: undefined,
      usageFormula: defaultRule ? '1' : undefined,
      usageFormulaText: defaultRule ? '1' : undefined,
      calculationUnitCode: props.usageRow.calculationUnitCode || props.usageRow.unitCode,
      defaultRuleFlag: defaultRule,
      productionRemark: props.usageRow.productionRemark,
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: next * 10
    }
    props.usageRules.push(rule)
    if (defaultRule) handleDefaultRuleChange(rule)
    selectedRule.value = rule
  }

  function addConditionalUsageRule() {
    ensureFormulaMode()
    addFormulaRule(false)
  }

  function generateFabricRules() {
    ensureFormulaMode()
    ensureDefaultRule()
    const optionCode = fabricOptionCode()
    const fabricValues = optionValuesOf(optionCode)
    if (!fabricValues.length) {
      ElMessage.warning(t('productCenter.formulaSetup.fabricOptionMissing'))
      return
    }
    fabricValues.forEach((value) => {
      const key = conditionKeyForOption(optionCode, value.valueCode)
      const exists = currentRules.value.some((rule) => rule.conditionKey === key)
      if (!exists) addFormulaRule(false, { ...value, optionCode })
    })
  }

  function ensureDefaultRule() {
    if (!currentRules.value.some((rule) => rule.defaultRuleFlag)) {
      addFormulaRule(true)
    }
  }

  function copySelectedRule() {
    if (!selectedRule.value) return
    ensureFormulaMode()
    const copy = { ...selectedRule.value, usageRuleId: undefined, defaultRuleFlag: false, sortOrder: props.usageRules.length * 10 + 10 }
    copy.usageMode = 'FORMULA'
    copy.fixedUsageQty = undefined
    copy.ruleName = `${copy.ruleName || t('productCenter.formulaSetup.conditionalUsageRule')} Copy`
    props.usageRules.push(copy)
    selectedRule.value = copy
  }

  function removeSelectedRule() {
    if (!selectedRule.value) return
    const index = props.usageRules.indexOf(selectedRule.value)
    if (index >= 0) props.usageRules.splice(index, 1)
    selectedRule.value = null
  }

  function handleDefaultRuleChange(row: ProductFormulaUsageRuleVO) {
    if (row.defaultRuleFlag) {
      ;(row as Record<string, unknown>)._conditionMode = 'DEFAULT'
      currentRules.value.forEach((rule) => {
        if (rule !== row) rule.defaultRuleFlag = false
      })
      row.conditionType = 'DEFAULT'
      row.conditionOptionCode = undefined
      row.conditionOptionNameCn = undefined
      row.conditionValueCode = undefined
      row.conditionValueNameCn = undefined
      row.conditionExpression = 'DEFAULT'
      row.conditionText = t('productCenter.formulaSetup.defaultUsageRule')
      row.conditionKey = defaultConditionKey()
    } else if (row.conditionType === 'DEFAULT') {
      row.conditionType = 'EXPRESSION'
      row.conditionExpression = undefined
      row.conditionText = undefined
      row.conditionKey = undefined
    }
  }

  function syncExpressionCondition(row: ProductFormulaUsageRuleVO) {
    if (row.conditionType !== 'EXPRESSION') return
    ;(row as Record<string, unknown>)._conditionMode = 'EXPRESSION'
    const expressionText = normalizeDisplayExpression(row.conditionText || row.conditionExpression, props.options, props.optionValues, props.materials || [])
    const result = validateConditionExpression(expressionText)
    row.conditionExpression = result.expression
    row.conditionKey = result.valid ? `EXPR:${result.expression}` : undefined
  }

  function syncFormula(row: ProductFormulaUsageRuleVO, field: FormulaField = formulaFields[4]) {
    const expressionText = normalizeDisplayExpression(ruleFormulaText(row, field), props.options, props.optionValues, props.materials || [], props.variables || [])
    const result = validateFormulaExpression(expressionText)
    row[field.valueKey] = result.expression as never
    if (row.usageMode === 'FIXED' && result.valid && typeof result.sampleValue === 'number') {
      row.fixedUsageQty = result.sampleValue
    }
  }

  function syncCurrentRules() {
    let valid = true
    currentRules.value.forEach((row) => {
      if (row.conditionType === 'EXPRESSION') {
        syncExpressionCondition(row)
        if (!validateConditionExpression(row.conditionExpression).valid) valid = false
      }
      formulaFields.forEach((field) => {
        if (!ruleFormulaText(row, field)) return
        syncFormula(row, field)
        if (!validateFormulaExpression(String(row[field.valueKey] || '')).valid) valid = false
      })
    })
    if (!valid) ElMessage.warning(t('product.formula.usageConditionInvalid'))
    return valid
  }

  function ruleFormulaText(row: ProductFormulaUsageRuleVO, field: FormulaField = formulaFields[4]) {
    const textValue = row[field.textKey]
    const formulaValue = row[field.valueKey]
    if (typeof textValue === 'string' || typeof formulaValue === 'string') return (textValue || formulaValue) as string
    if (field.target === 'usage' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) return String(row.fixedUsageQty)
    return undefined
  }

  function openExpressionEditor(row: ProductFormulaUsageRuleVO, target: ExpressionTarget) {
    expressionEditorRow.value = row
    expressionEditorTarget.value = target
    const field = formulaFieldByTarget(target)
    expressionEditorText.value = field
      ? ruleFormulaText(row, field) || ''
      : row.conditionText || row.conditionExpression || ''
    expressionEditorOpen.value = true
  }

  function confirmExpressionEditor() {
    const row = expressionEditorRow.value
    if (!row) return
    const field = formulaFieldByTarget(expressionEditorTarget.value)
    if (field) {
      row[field.textKey] = expressionEditorText.value as never
      syncFormula(row, field)
    } else {
      row.conditionText = expressionEditorText.value
      syncExpressionCondition(row)
    }
    expressionEditorOpen.value = false
  }

  function formulaFieldByTarget(target: ExpressionTarget) {
    return formulaFields.find((field) => field.target === target)
  }

  function optionValuesOf(optionCode?: string) {
    return props.optionValues.filter((value) => value.optionCode === optionCode)
  }

  function fabricOptionCode() {
    const materialPoolOptions = props.options.filter((option) => option.sourceType === 'MATERIAL_POOL')
    return props.options.find((option) => option.optionCode === 'FABRIC')?.optionCode
      || materialPoolOptions.find((option) => option.optionNameCn?.includes('面料'))?.optionCode
      || materialPoolOptions[0]?.optionCode
      || 'FABRIC'
  }

  function optionName(optionCode?: string) {
    return props.options.find((option) => option.optionCode === optionCode)?.optionNameCn
  }

  function conditionText(optionCode?: string, valueCode?: string) {
    const option = optionName(optionCode)
    const value = optionValuesOf(optionCode).find((item) => item.valueCode === valueCode)
    return option && value ? `${option} = ${value.valueNameCn || value.valueCode}` : undefined
  }

  function applyCurrentUsageToBatchRows() {
    if (!props.usageRow || currentUsageRows.value.length <= 1) return
    const sourceRow = props.usageRow
    const sourceRules = cloneValue(currentRules.value)
    currentUsageRows.value.forEach((targetRow) => {
      if (!targetRow.materialCode || targetRow.materialCode === sourceRow.materialCode) return
      targetRow.usageMode = sourceRow.usageMode
      targetRow.fixedUsageQty = sourceRow.fixedUsageQty
      targetRow.calculationUnitCode = sourceRow.calculationUnitCode || sourceRow.unitCode
      removeRulesForMaterial(targetRow.materialCode)
      sourceRules.forEach((rule, index) => {
        props.usageRules.push(copyUsageRuleForMaterial(rule, targetRow, index))
      })
    })
  }

  function copyUsageRuleForMaterial(rule: ProductFormulaUsageRuleVO, row: ProductFormulaMaterialVO, index: number) {
    const copy = cloneValue(rule)
    delete (copy as Record<string, unknown>).usageRuleId
    delete (copy as Record<string, unknown>).lossRate
    copy.materialCode = row.materialCode
    copy.materialNameCn = row.materialNameCn
    copy.materialId = row.materialId
    copy.formulaMaterialId = row.formulaMaterialId
    copy.calculationUnitCode = row.calculationUnitCode || row.unitCode || rule.calculationUnitCode
    copy.productionRemark = row.productionRemark
    copy.sortOrder = rule.sortOrder ?? (index + 1) * 10
    return copy
  }

  function removeRulesForMaterial(materialCode?: string) {
    if (!materialCode) return
    for (let index = props.usageRules.length - 1; index >= 0; index--) {
      if (props.usageRules[index].materialCode === materialCode) {
        props.usageRules.splice(index, 1)
      }
    }
  }

  function removeRulesForCurrent() {
    removeRulesForMaterial(props.usageRow?.materialCode)
  }

  return {
    selectedRule,
    expressionEditorOpen,
    expressionEditorText,
    expressionEditorTarget,
    formulaFields,
    currentRules,
    ensureInitialRule,
    handleUsageModeChange,
    syncFixedRuleFromRow,
    generateFabricRules,
    addConditionalUsageRule,
    copySelectedRule,
    removeSelectedRule,
    handleDefaultRuleChange,
    syncFormula,
    syncCurrentRules,
    openExpressionEditor,
    confirmExpressionEditor,
    applyCurrentUsageToBatchRows
  }
}
