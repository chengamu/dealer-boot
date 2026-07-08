<template>
  <el-dialog :model-value="modelValue" :title="t('productCenter.pricing.batchSetPrice')" width="760px" destroy-on-close @close="close">
    <el-form label-width="110px">
      <el-form-item :label="t('productCenter.pricing.batchFabrics')">
        <el-select v-model="form.materialCodes" multiple filterable collapse-tags collapse-tags-tooltip class="full-width">
          <el-option
            v-for="item in fabrics"
            :key="String(item.materialCode || '')"
            :label="`${item.materialCode || ''} ${item.materialNameCn || ''}`"
            :value="String(item.materialCode || '')"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('productCenter.pricing.optionCombination')">
        <el-select v-model="form.combinationKeys" multiple collapse-tags collapse-tags-tooltip class="full-width">
          <el-option
            v-for="item in columns"
            :key="item.optionCombinationKey || 'DEFAULT'"
            :label="item.optionCombinationName || t('productCenter.pricing.defaultCombination')"
            :value="item.optionCombinationKey || 'DEFAULT'"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('productCenter.pricing.unitPrice')">
        <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :controls="false" class="full-width" />
      </el-form-item>
      <el-form-item :label="t('productCenter.pricing.fabricFormula')">
        <PriceFormulaEditor v-model="form.formula" :label="t('productCenter.pricing.fabricFormula')" :rows="2" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.blankOnly">{{ t('productCenter.pricing.applyToBlankOnly') }}</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="save">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { PriceFabricCandidate, PriceOptionCombination } from '@/api/product-pricing/types'
import { DEFAULT_FABRIC_PRICE_FORMULA } from '../utils/pricingDisplay'
import PriceFormulaEditor from './PriceFormulaEditor.vue'

const props = defineProps<{
  modelValue: boolean
  fabrics: PriceFabricCandidate[]
  columns: PriceOptionCombination[]
  formula?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: { materialCodes: string[], combinationKeys: string[], unitPrice: number, formula: string, blankOnly: boolean }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const form = reactive({
  materialCodes: [] as string[],
  combinationKeys: [] as string[],
  unitPrice: 0,
  formula: DEFAULT_FABRIC_PRICE_FORMULA,
  blankOnly: false
})

watch(() => props.modelValue, (open) => {
  if (!open) return
  form.materialCodes = props.fabrics.map(item => String(item.materialCode || '')).filter(Boolean)
  form.combinationKeys = props.columns.map(item => item.optionCombinationKey || 'DEFAULT')
  form.unitPrice = 0
  form.formula = props.formula || DEFAULT_FABRIC_PRICE_FORMULA
  form.blankOnly = false
})

function save() {
  emit('save', {
    materialCodes: form.materialCodes,
    combinationKeys: form.combinationKeys,
    unitPrice: Number(form.unitPrice || 0),
    formula: form.formula,
    blankOnly: form.blankOnly
  })
  close()
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>
