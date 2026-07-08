<template>
  <el-dialog :model-value="modelValue" :title="t('productCenter.pricing.priceSetting')" width="720px" destroy-on-close @close="close">
    <div class="rule-summary">
      <span>{{ row?.materialCode || '-' }}</span>
      <strong>{{ row?.materialNameCn || '-' }}</strong>
      <el-tag size="small">{{ row?.optionCombinationName || t('productCenter.pricing.defaultCombination') }}</el-tag>
    </div>
    <el-form label-width="120px">
      <el-form-item :label="t('productCenter.pricing.unitPrice')" required>
        <el-input-number v-model="form.basePrice" :min="0" :precision="2" :controls="false" class="full-width" />
      </el-form-item>
      <el-form-item :label="t('productCenter.pricing.fabricFormula')" required>
        <PriceFormulaEditor v-model="form.areaFormula" :label="t('productCenter.pricing.fabricFormula')" :rows="3" />
      </el-form-item>
      <el-form-item :label="t('common.remark')">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
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
import type { FabricPriceRule } from '@/api/product-pricing/types'
import { DEFAULT_FABRIC_PRICE_FORMULA } from '../utils/pricingDisplay'
import PriceFormulaEditor from './PriceFormulaEditor.vue'

const props = defineProps<{
  modelValue: boolean
  row?: FabricPriceRule
  formula?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [row: FabricPriceRule]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const form = reactive({
  basePrice: 0,
  areaFormula: DEFAULT_FABRIC_PRICE_FORMULA,
  remark: ''
})

watch(() => props.modelValue, (open) => {
  if (!open) return
  form.basePrice = Number(props.row?.basePrice || 0)
  form.areaFormula = props.row?.areaFormula || props.formula || DEFAULT_FABRIC_PRICE_FORMULA
  form.remark = props.row?.remark || ''
})

function save() {
  if (!props.row) return
  emit('save', {
    ...props.row,
    basePrice: Number(form.basePrice || 0),
    areaFormula: form.areaFormula,
    remark: form.remark,
    priceMode: 'FORMULA',
    status: 'ENABLED'
  })
  close()
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.rule-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 10px 12px;
  color: #344054;
  background: #f7f9fc;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.rule-summary strong {
  color: #1f2937;
}

.full-width {
  width: 100%;
}
</style>
