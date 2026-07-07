import { computed, type Ref } from 'vue'
import type { ProductFormulaMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import {
  createDraftClientKey,
  optionClientKey,
  type DraftOptionValue
} from '../utils/formulaOptionDraftIdentity'

type OptionSourceProps = {
  options: ProductFormulaOptionVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  materials: ProductFormulaMaterialVO[]
}

type UseOptionSourceOptions = {
  t: (key: string, params?: Record<string, string | number>) => string
  selectedOption: Ref<ProductFormulaOptionVO | undefined>
}

export function useFormulaOptionSource(props: OptionSourceProps, options: UseOptionSourceOptions) {
  const materialGroupOptions = computed(() => {
    const groups = new Map<string, string>()
    props.materials.forEach((row) => {
      if (!row.attributeGroupCode) return
      groups.set(row.attributeGroupCode, row.attributeGroupNameCn || row.attributeGroupCode)
    })
    return Array.from(groups.entries()).map(([value, label]) => ({ value, label }))
  })

  function sourceText(option: ProductFormulaOptionVO) {
    if (option.sourceType === 'MATERIAL_POOL') {
      const group = materialGroupOptions.value.find((item) => item.value === sourceGroupCode(option))
      return `${options.t('productCenter.formulaSetup.sourceMaterialPool')} ${group?.label || ''}`.trim()
    }
    const map: Record<string, string> = {
      PRODUCT_DICT: options.t('productCenter.formulaSetup.sourceProductDict'),
      BOOLEAN: options.t('productCenter.formulaSetup.sourceBoolean'),
      MANUAL: options.t('productCenter.formulaSetup.sourceManual')
    }
    return map[option.sourceType || ''] || options.t('productCenter.formulaSetup.sourceManual')
  }

  function visibilityText(option: ProductFormulaOptionVO) {
    if (option.visibilityMode !== 'CONDITIONAL') {
      return options.t('productCenter.formulaSetup.visibilityAlways')
    }
    const conditionOption = props.options.find((row) => row.optionCode === option.visibleConditionOptionCode)
    const conditionValue = props.allOptionValues.find((row) => row.optionCode === option.visibleConditionOptionCode && row.valueCode === option.visibleConditionValueCode)
    const optionName = option.visibleConditionOptionNameCn || conditionOption?.optionNameCn || option.visibleConditionOptionCode || '-'
    const valueName = option.visibleConditionValueNameCn || conditionValue?.valueNameCn || option.visibleConditionValueCode || '-'
    return options.t('productCenter.formulaSetup.visibleWhenSummary')
      .replace('{option}', optionName)
      .replace('{value}', valueName)
  }

  function sourceGroupCode(option?: ProductFormulaOptionVO) {
    return String(option?.sourceScope || '').replace('attributeGroupCode=', '')
  }

  function changeSourceGroup(value: string) {
    if (!options.selectedOption.value) return
    options.selectedOption.value.sourceScope = value ? `attributeGroupCode=${value}` : ''
  }

  function handleSourceTypeChange() {
    if (!options.selectedOption.value) return
    if (options.selectedOption.value.sourceType === 'BOOLEAN') {
      normalizeBooleanOption(options.selectedOption.value)
      return
    }
    if (options.selectedOption.value.sourceType === 'MATERIAL_POOL' && !options.selectedOption.value.sourceScope) {
      const firstGroup = materialGroupOptions.value[0]?.value || ''
      options.selectedOption.value.sourceScope = firstGroup ? `attributeGroupCode=${firstGroup}` : ''
    }
    if (options.selectedOption.value.sourceType !== 'MATERIAL_POOL') {
      options.selectedOption.value.displayMode = 'SELECT'
      return
    }
    options.selectedOption.value.displayMode ||= 'SELECT'
  }

  function normalizeBooleanOption(option: ProductFormulaOptionVO) {
    option.sourceScope = ''
    option.selectionMode = 'SWITCH'
    option.displayMode = 'SELECT'
    option.defaultValueCode = String(option.defaultValueCode || '').toUpperCase() === 'TRUE' ? 'TRUE' : 'FALSE'
    option.defaultValueNameCn = option.defaultValueCode.toLowerCase()
    const optionKey = optionClientKey(option)
    const booleanRows = [
      booleanValueRow(option, optionKey, 'FALSE', 10, option.defaultValueCode !== 'TRUE'),
      booleanValueRow(option, optionKey, 'TRUE', 20, option.defaultValueCode === 'TRUE')
    ]
    props.allOptionValues.splice(
      0,
      props.allOptionValues.length,
      ...props.allOptionValues.filter((row) => !valueBelongsToOption(row, option, optionKey)),
      ...booleanRows
    )
  }

  function booleanValueRow(option: ProductFormulaOptionVO, optionKey: string, valueCode: 'TRUE' | 'FALSE', sortOrder: number, defaultFlag: boolean) {
    const existing = props.allOptionValues.find((row) => valueBelongsToOption(row, option, optionKey) && row.valueCode === valueCode) as DraftOptionValue | undefined
    const row = existing || ({ clientKey: createDraftClientKey('value') } as DraftOptionValue)
    row.optionClientKey = optionKey
    row.optionCode = option.optionCode
    row.valueCode = valueCode
    row.valueNameCn = valueCode.toLowerCase()
    row.valueNameEn = valueCode.toLowerCase()
    row.defaultFlag = defaultFlag
    row.status ||= 'ENABLED'
    row.sortOrder = sortOrder
    return row as ProductFormulaOptionValueVO
  }

  function valueBelongsToOption(row: ProductFormulaOptionValueVO, option: ProductFormulaOptionVO, optionKey: string) {
    return (row as DraftOptionValue).optionClientKey
      ? (row as DraftOptionValue).optionClientKey === optionKey
      : row.optionCode === option.optionCode
  }

  return {
    materialGroupOptions,
    sourceText,
    visibilityText,
    sourceGroupCode,
    changeSourceGroup,
    handleSourceTypeChange
  }
}
