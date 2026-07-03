import { computed, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED, formulaStatusTagType, formulaStatusText, formulaValidationStatusText, formulaValidationTagType } from '@/constants/productStatus'
import { formatUsageNumber } from '../utils/formulaExpression'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVO,
  ProductMaterialVO,
  ProductRecord,
  ProductUnitVO
} from '@/api/product-capability/types'
import type { SetupState } from './useFormulaSetupCore'

type SetupOperationContext = {
  setup: SetupState
  formula: Ref<ProductFormulaVO>
  selectedOptionCode: Ref<string>
  usageRow: Ref<ProductFormulaMaterialVO | null>
  usageRows: Ref<ProductFormulaMaterialVO[]>
  usageDrawerOpen: Ref<boolean>
  materialPickerOpen: Ref<boolean>
  groupRows: Ref<Array<{ groupCode?: string }>>
  groupSortMap: Ref<Record<string, number>>
  unitRows: Ref<ProductUnitVO[]>
  language: () => string
  t: (key: string) => string
}

export function useFormulaSetupOperations(ctx: SetupOperationContext) {
  const selectedValues = computed(() => ctx.setup.optionValues.filter((row) => row.optionCode === ctx.selectedOptionCode.value))
  const selectedOptionMaterials = computed(() => ctx.setup.optionMaterials.filter((row) => row.optionCode === ctx.selectedOptionCode.value))
  const unsetUsageCount = computed(() => ctx.setup.materials.filter((row) => isUsageUnset(row)).length)
  function appendSelectedMaterials(selectedMaterials: ProductMaterialVO[]) {
    const existing = new Set(ctx.setup.materials.map((row) => row.materialCode))
    selectedMaterials.forEach((material) => {
      if (!material.materialCode || existing.has(material.materialCode)) return
      ctx.setup.materials.push(materialToFormulaMaterial(material))
      existing.add(material.materialCode)
    })
    ctx.materialPickerOpen.value = false
  }
  function materialToFormulaMaterial(material: ProductMaterialVO): ProductFormulaMaterialVO {
    return {
      lineNo: ctx.setup.materials.length + 1,
      materialId: material.materialId,
      materialCode: material.materialCode,
      materialNameCn: material.materialNameCn,
      specModelText: material.specModelText,
      attributeGroupId: material.attributeGroupId,
      attributeGroupCode: material.attributeGroupCode,
      attributeGroupNameCn: material.attributeGroupNameCn,
      materialTypeId: material.materialTypeId,
      materialTypeCode: material.materialTypeCode,
      materialTypeNameCn: material.materialTypeNameCn,
      unitCode: material.unitCode,
      calculationUnitCode: material.unitCode,
      usageMode: 'FIXED',
      fixedUsageQty: 1,
      lossRate: 0,
      defaultFlag: false,
      requiredFlag: true,
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: nextMaterialSortOrder(material)
    }
  }
  function nextMaterialSortOrder(material: ProductMaterialVO) {
    const groupRank = Math.max(1, ctx.groupSortMap.value[material.attributeGroupCode || ''] || ctx.groupRows.value.length + 1)
    const base = groupRank * 100
    const used = new Set(ctx.setup.materials
      .filter((row) => row.attributeGroupCode === material.attributeGroupCode)
      .map((row) => Number(row.sortOrder || 0))
      .filter((value) => value >= base && value < base + 100))
    let next = base + used.size + 1
    while (used.has(next)) next += 1
    return next
  }
  function addOptionRow(parent?: { optionCode?: string; optionNameCn?: string; valueCode?: string; valueNameCn?: string }) {
    const next = ctx.setup.options.length + 1
    const code = `OPTION_${next}`
    ctx.setup.options.push({
      optionCode: code,
      optionNameCn: parent ? `${parent.valueNameCn || parent.valueCode || ''}${ctx.t('productCenter.formulaSetup.childOption')}` : `${ctx.t('productCenter.formulaSetup.optionName')} ${next}`,
      sourceType: 'MANUAL',
      selectionMode: 'SINGLE',
      requiredFlag: true,
      businessVisibleFlag: true,
      visibilityMode: parent ? 'CONDITIONAL' : 'ALWAYS',
      visibleConditionOptionCode: parent?.optionCode || '',
      visibleConditionOptionNameCn: parent?.optionNameCn || '',
      visibleConditionValueCode: parent?.valueCode || '',
      visibleConditionValueNameCn: parent?.valueNameCn || '',
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: next * 10
    })
    ctx.selectedOptionCode.value = code
  }
  function addOptionValueRow() {
    if (!ctx.selectedOptionCode.value) return
    const next = selectedValues.value.length + 1
    ctx.setup.optionValues.push({
      optionCode: ctx.selectedOptionCode.value,
      valueCode: `VALUE_${next}`,
      valueNameCn: '',
      defaultFlag: false,
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: ctx.setup.optionValues.length * 10 + 10
    })
  }
  function addOptionMaterialRow(valueCode?: string) {
    if (!ctx.selectedOptionCode.value) return
    ctx.setup.optionMaterials.push({
      optionCode: ctx.selectedOptionCode.value,
      valueCode: valueCode || selectedValues.value[0]?.valueCode,
      requiredFlag: true,
      defaultFlag: true,
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: ctx.setup.optionMaterials.length * 10 + 10
    })
  }
  function addRestrictionRow() {
    ctx.setup.restrictions.push({
      restrictionName: '',
      targetOptionCode: ctx.selectedOptionCode.value || ctx.setup.options[0]?.optionCode,
      conditionType: 'WIDTH',
      conditionOperator: 'GT',
      actionType: 'DISABLE',
      status: PRODUCT_STATUS_ENABLED,
      sortOrder: ctx.setup.restrictions.length * 10 + 10
    })
  }
  function removeOption(index: number) {
    const optionCode = ctx.setup.options[index]?.optionCode
    removeRow(ctx.setup.options, index)
    ctx.setup.optionValues = ctx.setup.optionValues.filter((row) => row.optionCode !== optionCode)
    ctx.setup.optionMaterials = ctx.setup.optionMaterials.filter((row) => row.optionCode !== optionCode)
    ctx.setup.options.forEach((row) => {
      if (row.visibleConditionOptionCode === optionCode) clearVisibilityCondition(row)
    })
    if (ctx.selectedOptionCode.value === optionCode) ctx.selectedOptionCode.value = ctx.setup.options[0]?.optionCode || ''
  }
  function removeSelectedValue(index: number) {
    const row = selectedValues.value[index]
    if (!row) return
    removeRow(ctx.setup.optionValues, ctx.setup.optionValues.indexOf(row))
    ctx.setup.optionMaterials = ctx.setup.optionMaterials.filter((item) => !(item.optionCode === row.optionCode && item.valueCode === row.valueCode))
    ctx.setup.options.forEach((option) => {
      if (option.visibleConditionOptionCode === row.optionCode && option.visibleConditionValueCode === row.valueCode) clearVisibilityCondition(option)
    })
  }
  function removeSelectedOptionMaterial(index: number) {
    const row = selectedOptionMaterials.value[index]
    if (row) removeRow(ctx.setup.optionMaterials, ctx.setup.optionMaterials.indexOf(row))
  }
  function removeMaterial(material: ProductFormulaMaterialVO) {
    if (isMaterialReferenced(material?.materialCode)) {
      ElMessage.warning(ctx.t('productCenter.formulaSetup.materialReferencedRemoveDenied'))
      return
    }
    const index = ctx.setup.materials.indexOf(material)
    if (index < 0) return
    removeRow(ctx.setup.materials, index)
  }
  function removeMaterials(rows: ProductFormulaMaterialVO[]) {
    const codes = new Set(rows.map((row) => row.materialCode).filter(Boolean))
    if ([...codes].some((code) => isMaterialReferenced(code))) {
      ElMessage.warning(ctx.t('productCenter.formulaSetup.materialReferencedRemoveDenied'))
      return
    }
    ctx.setup.materials = ctx.setup.materials.filter((row) => !codes.has(row.materialCode))
  }
  function isMaterialReferenced(materialCode?: string) {
    if (!materialCode) return false
    return ctx.setup.usageRules.some((row) => row.materialCode === materialCode)
      || ctx.setup.optionMaterials.some((row) => row.materialCode === materialCode)
  }
  function handleOptionChange(row?: ProductFormulaOptionVO) {
    if (row?.optionCode) ctx.selectedOptionCode.value = row.optionCode
  }
  function moveOption(optionCode: string, direction: 'UP' | 'DOWN') {
    const currentIndex = ctx.setup.options.findIndex((row) => row.optionCode === optionCode)
    const targetIndex = direction === 'UP' ? currentIndex - 1 : currentIndex + 1
    if (currentIndex < 0 || targetIndex < 0 || targetIndex >= ctx.setup.options.length) return
    const [row] = ctx.setup.options.splice(currentIndex, 1)
    ctx.setup.options.splice(targetIndex, 0, row)
    ctx.setup.options.forEach((item, index) => { item.sortOrder = (index + 1) * 10 })
  }
  function openUsage(row: ProductFormulaMaterialVO) {
    ctx.usageRow.value = row
    ctx.usageRows.value = [row]
    ctx.usageDrawerOpen.value = true
  }
  function openBatchUsage(rows: ProductFormulaMaterialVO[]) {
    ctx.usageRows.value = rows
    ctx.usageRow.value = rows[0] || null
    ctx.usageDrawerOpen.value = true
  }
  function selectUsageRow(row: ProductFormulaMaterialVO) {
    ctx.usageRow.value = row
  }
  function syncOptionMaterial(row: ProductFormulaOptionMaterialVO) {
    const material = ctx.setup.materials.find((item) => item.materialCode === row.materialCode)
    row.formulaMaterialId = material?.formulaMaterialId
    row.materialId = material?.materialId
    row.materialNameCn = material?.materialNameCn
  }
  function usageSummary(row: ProductFormulaMaterialVO): string {
    const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
    if (activeRules.length > 0) {
      const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
      const fixedRule = activeRules.find((rule) => rule.usageMode === 'FIXED')
      if (formulaRules.length > 0) return formulaRules.slice().sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0)).map(usageRuleSummary).join('\n')
      if (fixedRule) return `${textOf(ctx.t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(fixedRule.fixedUsageQty)}`
    }
    if (row.usageMode === 'FIXED' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) return `${textOf(ctx.t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(row.fixedUsageQty)}`
    if (row.usageMode === 'FORMULA' && hasAnyUsageFormula(row)) return usageRuleSummary(row)
    return textOf(ctx.t('productCenter.formulaSetup.usageNotSet'))
  }
  function isUsageUnset(row: ProductFormulaMaterialVO) {
    const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
    if (activeRules.length > 0) {
      const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
      if (formulaRules.length > 0) return !formulaRules.some((rule) => rule.defaultRuleFlag)
      return activeRules.some((rule) => rule.usageMode === 'FIXED' && (rule.fixedUsageQty === undefined || rule.fixedUsageQty === null))
    }
    if (row.usageMode === 'FIXED') return row.fixedUsageQty === undefined || row.fixedUsageQty === null
    if (row.usageMode === 'FORMULA') return !hasAnyUsageFormula(row)
    return true
  }
  function usageRuleSummary(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string {
    const condition = usageConditionText(rule)
    const formulas = usageFormulaParts(rule)
    return formulas.length ? `${condition}：\n${formulas.join('\n')}` : condition
  }
  function usageConditionText(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string {
    if ('defaultRuleFlag' in rule && rule.defaultRuleFlag) return textOf(ctx.t('productCenter.formulaSetup.defaultUsageRule'))
    if ('conditionText' in rule && rule.conditionText) return textOf(rule.conditionText)
    if ('conditionOptionNameCn' in rule && (rule.conditionOptionNameCn || rule.conditionOptionCode)) {
      return `${rule.conditionOptionNameCn || rule.conditionOptionCode} = ${rule.conditionValueNameCn || rule.conditionValueCode || '-'}`
    }
    if ('conditionExpression' in rule && rule.conditionExpression) return textOf(rule.conditionExpression)
    return textOf(ctx.t('productCenter.formulaSetup.defaultUsageRule'))
  }
  function usageFormulaParts(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string[] {
    const parts: string[] = []
    appendUsageFormulaPart(parts, textOf(ctx.t('productCenter.formulaSetup.lengthFormula')), summaryFormulaValue(rule.lengthFormulaText || rule.lengthFormula))
    appendUsageFormulaPart(parts, textOf(ctx.t('productCenter.formulaSetup.widthFormula')), summaryFormulaValue(rule.widthFormulaText || rule.widthFormula))
    appendUsageFormulaPart(parts, textOf(ctx.t('productCenter.formulaSetup.heightFormula')), summaryFormulaValue(rule.heightFormulaText || rule.heightFormula))
    appendUsageFormulaPart(parts, textOf(ctx.t('productCenter.formulaSetup.weightFormula')), summaryFormulaValue(rule.weightFormulaText || rule.weightFormula))
    const quantityFormula = summaryFormulaValue(rule.usageFormulaText || rule.usageFormula)
    if (quantityFormula && !(parts.length > 0 && isDefaultQuantityFormula(quantityFormula))) appendUsageFormulaPart(parts, textOf(ctx.t('productCenter.formulaSetup.quantityFormula')), quantityFormula)
    if (!parts.length && rule.fixedUsageQty !== undefined && rule.fixedUsageQty !== null) parts.push(`${textOf(ctx.t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(rule.fixedUsageQty)}`)
    return parts
  }
  function unitLabel(unitCode?: string) {
    if (!unitCode) return '-'
    const unit = ctx.unitRows.value.find((row) => row.unitCode === unitCode)
    if (!unit) return unitCode
    return ctx.language() === 'zh_CN' ? unit.unitNameCn || unit.unitCode || unitCode : unit.unitNameEn || unit.unitNameCn || unit.unitCode || unitCode
  }
  function statusText(status?: string) { return formulaStatusText(status, ctx.t) }
  function validationText(status?: string) { return formulaValidationStatusText(status, ctx.t) }
  function statusTagType(status?: string) { return formulaStatusTagType(status) }
  function validationTagType(status?: string) { return formulaValidationTagType(status) }
  function materialLabel(row: ProductMaterialVO | ProductFormulaMaterialVO) { return `${row.materialCode || ''} ${row.materialNameCn || ''}`.trim() }
  function formatNumber(value?: number | string) { const numericValue = Number(value); return Number.isFinite(numericValue) ? numericValue.toFixed(2) : '-' }
  function formatMinute(value?: string) { return value ? value.replace('T', ' ').slice(0, 16) : '-' }
  function removeRow<T>(rows: T[], index: number) { if (index >= 0) rows.splice(index, 1) }
  function clearVisibilityCondition(row: ProductFormulaOptionVO) { row.visibilityMode = 'ALWAYS'; row.visibleConditionOptionCode = ''; row.visibleConditionOptionNameCn = ''; row.visibleConditionValueCode = ''; row.visibleConditionValueNameCn = '' }
  function usageRulesFor(row: ProductFormulaMaterialVO) { return ctx.setup.usageRules.filter((rule) => rule.materialCode === row.materialCode) }
  function hasAnyUsageFormula(row: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO) { return Boolean(row.lengthFormula || row.widthFormula || row.heightFormula || row.weightFormula || row.usageFormula) }
  function appendUsageFormulaPart(parts: string[], label: string, value?: string) { if (value) parts.push(`${label} ${value}`) }
  function summaryFormulaValue(value: unknown): string { const text = textOf(value).trim(); const numeric = Number(text); return text && Number.isFinite(numeric) && /^-?\d+(\.\d+)?$/.test(text) ? numeric.toFixed(2) : text }
  function isDefaultQuantityFormula(value: string) { return Number(value) === 1 && /^1(\.0+)?$/.test(value) }
  function textOf(value: unknown): string { return value === undefined || value === null ? '' : String(value) }
  return {
    selectedValues, selectedOptionMaterials, unsetUsageCount, appendSelectedMaterials, addOptionRow, addOptionValueRow,
    addOptionMaterialRow, addRestrictionRow, removeOption, removeSelectedValue, removeSelectedOptionMaterial, removeMaterial,
    removeMaterials, removeRow, handleOptionChange, moveOption, openUsage, openBatchUsage, selectUsageRow, syncOptionMaterial,
    usageSummary, isUsageUnset, statusText, validationText, statusTagType, validationTagType, materialLabel, unitLabel, formatNumber, formatMinute
  }
}
