import { computed, ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import {
  materialOwnerClientKey,
  materialValueClientKey,
  optionClientKey,
  valueClientKey,
  valueOwnerClientKey,
  type DraftOptionMaterial,
  type DraftOptionValue
} from '../utils/formulaOptionDraftIdentity'
import type { ProductFormulaMaterialVO, ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

type ValueDialogProps = {
  optionValues: ProductFormulaOptionValueVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  allOptionMaterials: ProductFormulaOptionMaterialVO[]
  materials: ProductFormulaMaterialVO[]
}

type MaterialGroupOption = {
  value: string
  label: string
}

type UseValueDialogsOptions = {
  t: (key: string, params?: Record<string, string | number>) => string
  selectedOption: Ref<ProductFormulaOptionVO | undefined>
  selectedValue: Ref<ProductFormulaOptionValueVO | undefined>
  selectedValueCode: Ref<string>
  materialGroupOptions: Ref<MaterialGroupOption[]>
  sourceGroupCode: (option?: ProductFormulaOptionVO) => string
  selectOptionValue: (row: ProductFormulaOptionValueVO) => void
  materialsForValue: (row?: ProductFormulaOptionValueVO) => ProductFormulaOptionMaterialVO[]
}

export function useFormulaOptionValueDialogs(props: ValueDialogProps, options: UseValueDialogsOptions) {
  const valueImportOpen = ref(false)
  const valueImportKeyword = ref('')
  const importMaterialRows = ref<ProductFormulaMaterialVO[]>([])
  const valueMaterialOpen = ref(false)
  const valueMaterialCodes = ref<string[]>([])
  const selectedValueCodes = computed(() => new Set(props.optionValues
    .filter((row) => valueOwnerClientKey(row) === optionClientKey(options.selectedOption.value) || (!valueOwnerClientKey(row) && row.optionCode === options.selectedOption.value?.optionCode))
    .map((row) => row.valueCode || '')
    .filter(Boolean)))
  const sourceMaterials = computed(() => {
    const option = options.selectedOption.value
    if (!option) return []
    const groupCode = options.sourceGroupCode(option)
    return props.materials.filter((row) => !groupCode || row.attributeGroupCode === groupCode)
  })
  const importableSourceMaterials = computed(() => sourceMaterials.value.filter((row) => {
    if (selectedValueCodes.value.has(row.materialCode || '')) return false
    const keyword = valueImportKeyword.value.trim().toLowerCase()
    if (!keyword) return true
    return containsText(row.materialCode, keyword)
      || containsText(row.materialNameCn, keyword)
      || containsText(row.materialNameEn, keyword)
      || containsText(row.specModelText, keyword)
      || containsText(row.materialTypeNameCn, keyword)
  }))
  const selectedValueName = computed(() => options.selectedValue.value?.valueNameCn || options.selectedValue.value?.valueCode || options.t('productCenter.formulaSetup.selectOptionHint'))
  const valueMaterialDialogTitle = computed(() => `${options.t('productCenter.formulaSetup.valueMaterial')}：${selectedValueName.value}`)

  function openValueImportDialog() {
    if (!options.selectedOption.value || options.selectedOption.value.sourceType !== 'MATERIAL_POOL') return
    valueImportKeyword.value = ''
    importMaterialRows.value = []
    valueImportOpen.value = true
  }

  function appendImportedMaterialValues() {
    const option = options.selectedOption.value
    if (!option) return
    const optionKey = optionClientKey(option)
    const existingValues = new Set(props.allOptionValues
      .filter((row) => valueOwnerClientKey(row) === optionKey || (!valueOwnerClientKey(row) && row.optionCode === option.optionCode))
      .map((row) => row.valueCode))
    importMaterialRows.value.forEach((material) => {
      if (!material?.materialCode || existingValues.has(material.materialCode)) return
      const valueKey = `material-value-${optionKey}-${material.materialCode}`
      props.allOptionValues.push({
        clientKey: valueKey,
        optionClientKey: optionKey,
        optionCode: option.optionCode,
        valueCode: material.materialCode,
        valueNameCn: material.materialNameCn,
        valueNameEn: material.materialNameEn || '',
        defaultFlag: false,
        status: PRODUCT_STATUS_ENABLED,
        sortOrder: props.allOptionValues.length * 10 + 10
      } as DraftOptionValue)
      props.allOptionMaterials.push({
        optionClientKey: optionKey,
        valueClientKey: valueKey,
        optionCode: option.optionCode,
        valueCode: material.materialCode,
        formulaMaterialId: material.formulaMaterialId,
        materialId: material.materialId,
        materialCode: material.materialCode,
        materialNameCn: material.materialNameCn,
        requiredFlag: true,
        defaultFlag: true,
        status: PRODUCT_STATUS_ENABLED,
        sortOrder: props.allOptionMaterials.length * 10 + 10
      } as DraftOptionMaterial)
      options.selectedValueCode.value = valueKey
    })
    importMaterialRows.value = []
    valueImportOpen.value = false
  }

  function openValueMaterialDialog(row: ProductFormulaOptionValueVO) {
    if (!row.valueCode) {
      ElMessage.warning(options.t('productCenter.formulaSetup.selectValueBeforeAddMaterial'))
      return
    }
    options.selectOptionValue(row)
    valueMaterialCodes.value = options.materialsForValue(row)
      .map((item) => item.materialCode || '')
      .filter(Boolean)
    valueMaterialOpen.value = true
  }

  function saveValueMaterials() {
    const option = options.selectedOption.value
    const value = options.selectedValue.value
    if (!option?.optionCode || !value?.valueCode) return
    const optionKey = optionClientKey(option)
    const valueKey = valueClientKey(value)
    for (let index = props.allOptionMaterials.length - 1; index >= 0; index -= 1) {
      const row = props.allOptionMaterials[index]
      const matched = materialValueClientKey(row)
        ? materialValueClientKey(row) === valueKey
        : row.optionCode === option.optionCode && row.valueCode === value.valueCode
      if (matched) {
        props.allOptionMaterials.splice(index, 1)
      }
    }
    valueMaterialCodes.value.forEach((code) => {
      const material = props.materials.find((row) => row.materialCode === code)
      if (!material?.materialCode) return
      props.allOptionMaterials.push({
        optionClientKey: optionKey,
        valueClientKey: valueKey,
        optionCode: option.optionCode,
        valueCode: value.valueCode,
        formulaMaterialId: material.formulaMaterialId,
        materialId: material.materialId,
        materialCode: material.materialCode,
        materialNameCn: material.materialNameCn,
        requiredFlag: true,
        defaultFlag: props.allOptionMaterials.filter((row) => {
          if (materialValueClientKey(row)) return materialValueClientKey(row) === valueKey
          if (materialOwnerClientKey(row)) return materialOwnerClientKey(row) === optionKey && row.valueCode === value.valueCode
          return row.optionCode === option.optionCode && row.valueCode === value.valueCode
        }).length === 0,
        status: PRODUCT_STATUS_ENABLED,
        sortOrder: props.allOptionMaterials.length * 10 + 10
      } as DraftOptionMaterial)
    })
    valueMaterialOpen.value = false
  }

  function containsText(value: unknown, keyword: string) {
    if (!keyword) return true
    return String(value || '').toLowerCase().includes(keyword)
  }

  return {
    valueImportOpen,
    valueImportKeyword,
    importMaterialRows,
    importableSourceMaterials,
    valueMaterialOpen,
    valueMaterialCodes,
    selectedValueName,
    valueMaterialDialogTitle,
    openValueImportDialog,
    appendImportedMaterialValues,
    openValueMaterialDialog,
    saveValueMaterials
  }
}
