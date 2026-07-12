<template>
  <section class="simulation-panel simulation-result-panel">
    <div class="simulation-result-strip">
      <div>
        <span>{{ t('productCenter.formulaSimulation.status') }}</span>
        <strong>{{ validationText(status) }}</strong>
      </div>
      <div>
        <span>{{ t('productCenter.formulaSimulation.bomRows') }}</span>
        <strong>{{ result.items?.length || 0 }}</strong>
      </div>
      <div>
        <span>{{ t('productCenter.formulaSimulation.quantity') }}</span>
        <strong>{{ displayQuantity }}</strong>
      </div>
      <div>
        <span>{{ t('productCenter.formulaSimulation.singleItemAmount') }}</span>
        <strong>{{ moneyFormat(result.singleAmount) }}</strong>
      </div>
      <div>
        <span>{{ t('productCenter.formulaSimulation.totalAmount') }}</span>
        <strong>{{ moneyFormat(result.totalAmount) }}</strong>
      </div>
      <div class="simulation-result-strip__message">{{ messageText(result.message) }}</div>
    </div>

    <div class="simulation-selected-summary">
      <span v-for="option in visibleOptions" :key="option.optionCode">
        {{ option.optionNameCn || option.optionNameEn || option.optionCode }}：
        <strong>{{ selectedLabel(option.optionCode) }}</strong>
        <small v-if="selectedMaterials(option.optionCode).length > 0">
          {{ t('productCenter.formulaSetup.linkedMaterial') }}：{{ linkedMaterialSummary(option.optionCode) }}
        </small>
      </span>
    </div>

    <el-collapse v-model="activePanels" class="simulation-bom-collapse">
      <el-collapse-item name="bom">
        <template #title>
          <span class="simulation-bom-collapse__title">{{ t('productCenter.formulaSimulation.bomPreview') }}</span>
        </template>
        <el-table :data="result.items || []" border class="simulation-table">
          <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
          <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="110" show-overflow-tooltip />
          <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="130" show-overflow-tooltip />
          <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="130" show-overflow-tooltip />
          <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="260" show-overflow-tooltip />
          <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="260" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.formulaSetup.unit')" width="90">
            <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.formulaSimulation.usageQty')" width="110" align="right">
            <template #default="{ row }">{{ quantityFormat(row.usageQty) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="96" align="right">
            <template #default="{ row }">{{ percentFormat(row.lossRate) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.formulaSimulation.unitPrice')" width="110" align="right">
            <template #default="{ row }">{{ moneyFormat(row.unitPrice) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.formulaSimulation.salesPrice')" width="110" align="right">
            <template #default="{ row }">{{ moneyFormat(row.salesPrice) }}</template>
          </el-table-column>
          <el-table-column :label="t('productCenter.formulaSimulation.amount')" width="110" align="right">
            <template #default="{ row }">{{ moneyFormat(row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="productionRemark" :label="t('productCenter.formulaSetup.productionRemark')" min-width="160" show-overflow-tooltip />
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaSimulationVO, ProductFormulaVO } from '@/api/product-capability/types'
import { FORMULA_VALIDATION_STATUS } from '@/constants/productStatus'
import { formatRate } from '@/utils/businessNumber'
import type { DecimalValue } from '@/types/api'

const props = defineProps<{
  result: ProductFormulaSimulationVO
  formula: ProductFormulaVO
  quantity: number
  selectedOptionValues: Record<string, string>
  visibleOptions: ProductFormulaOptionVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  unitLabel: (unitCode?: string) => string
  valueLabel: (optionCode?: string, valueCode?: string) => string
  quantityFormat: (value?: DecimalValue) => string
  moneyFormat: (value?: DecimalValue) => string
  validationText: (status?: string) => string
  messageText: (message?: string) => string
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const activePanels = ref<string[]>([])

const status = computed(() => props.result.status || props.formula.simulationValidationStatus || FORMULA_VALIDATION_STATUS.NOT_VALIDATED)
const displayQuantity = computed(() => props.result.orderQuantity || props.quantity)

function selectedLabel(optionCode?: string) {
  if (!optionCode) return '-'
  const valueCode = props.selectedOptionValues[optionCode]
  return valueCode ? props.valueLabel(optionCode, valueCode) : '-'
}

function selectedMaterials(optionCode?: string) {
  const valueCodes = splitCodes(optionCode ? props.selectedOptionValues[optionCode] : '')
  return props.optionMaterials.filter((row) => row.optionCode === optionCode && valueCodes.includes(String(row.valueCode || '')))
}

function linkedMaterialSummary(optionCode?: string) {
  const materials = selectedMaterials(optionCode)
  return materials.map((row) => row.materialNameCn || row.materialCode).filter(Boolean).slice(0, 3).join('、')
    + (materials.length > 3 ? ` +${materials.length - 3}` : '')
}

function percentFormat(value?: string | number | null) {
  const formatted = formatRate(value)
  return formatted === '-' ? formatted : `${formatted}%`
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}
</script>
