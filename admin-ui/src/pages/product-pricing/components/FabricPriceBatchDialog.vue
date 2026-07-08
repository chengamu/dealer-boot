<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.pricing.batchSetPrice')"
    width="min(1180px, calc(100vw - 48px))"
    class="fabric-price-batch-dialog"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="batch-price-editor">
      <section class="batch-price-editor__scope">
        <label>{{ t('productCenter.pricing.batchFabrics') }}</label>
        <el-select v-model="form.priceFabricIds" multiple filterable collapse-tags collapse-tags-tooltip>
          <el-option
            v-for="item in fabrics"
            :key="item.priceFabricId"
            :label="`${item.materialCode || ''} ${item.materialNameCn || ''}`"
            :value="Number(item.priceFabricId)"
          />
        </el-select>
      </section>

      <section class="batch-price-editor__grid">
        <div class="batch-price-editor__field">
          <label>{{ t('productCenter.pricing.condition') }}</label>
          <div class="batch-price-editor__inline">
            <el-input v-model="form.conditionExpression" :placeholder="t('productCenter.pricing.conditionPlaceholder')" />
            <el-button plain @click="openExpression('condition')">
              {{ t('productCenter.formulaSetup.conditionExpressionEditor') }}
            </el-button>
          </div>
        </div>
        <div class="batch-price-editor__field batch-price-editor__field--price">
          <label>{{ t('productCenter.pricing.unitPrice') }}</label>
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :controls="false" />
        </div>
        <div class="batch-price-editor__field batch-price-editor__field--formula">
          <label>{{ t('productCenter.pricing.fabricFormula') }}</label>
          <div class="batch-price-editor__inline">
            <el-input v-model="form.priceFormula" :placeholder="t('productCenter.pricing.formulaPlaceholder')" />
            <el-button plain @click="openExpression('formula')">
              {{ t('productCenter.formulaSetup.formulaSelectorShort') }}
            </el-button>
          </div>
          <p>{{ t('productCenter.pricing.unitPriceFormulaHint') }}</p>
        </div>
      </section>

      <el-checkbox v-model="form.blankOnly">{{ t('productCenter.pricing.applyToBlankOnly') }}</el-checkbox>
    </div>

    <PriceExpressionEditorDialog
      v-model="expressionOpen"
      v-model:text="expressionText"
      :target="expressionTarget"
      :options="options"
      :option-values="optionValues"
      @confirm="confirmExpression"
    />

    <template #footer>
      <AdminDialogFooter>
        <el-button @click="close">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { PriceFabricVO } from '@/api/product-pricing/types'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { fabricPriceFormulaForUnitPrice } from '../utils/pricingDisplay'
import PriceExpressionEditorDialog from './PriceExpressionEditorDialog.vue'

type ExpressionTarget = 'formula' | 'condition'

const props = defineProps<{
  modelValue: boolean
  fabrics: PriceFabricVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: { priceFabricIds: number[], conditionExpression: string, unitPrice: number, priceFormula: string, blankOnly: boolean }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const form = reactive({
  priceFabricIds: [] as number[],
  conditionExpression: '',
  unitPrice: 0,
  priceFormula: fabricPriceFormulaForUnitPrice(0),
  blankOnly: false
})
const expressionOpen = ref(false)
const expressionTarget = ref<ExpressionTarget>('formula')
const expressionText = ref('')

watch(() => props.modelValue, (open) => {
  if (!open) return
  form.priceFabricIds = props.fabrics.map(item => Number(item.priceFabricId)).filter(Boolean)
  form.conditionExpression = ''
  form.unitPrice = 0
  form.priceFormula = fabricPriceFormulaForUnitPrice(0)
  form.blankOnly = false
})

watch(() => form.unitPrice, () => {
  if (!form.priceFormula || /^\s*\d+(\.\d+)?\s*\*\s*MAX\(/.test(form.priceFormula)) {
    form.priceFormula = fabricPriceFormulaForUnitPrice(form.unitPrice)
  }
})

function openExpression(target: ExpressionTarget) {
  expressionTarget.value = target
  expressionText.value = target === 'formula' ? form.priceFormula : form.conditionExpression
  expressionOpen.value = true
}

function confirmExpression() {
  if (expressionTarget.value === 'formula') form.priceFormula = expressionText.value
  else form.conditionExpression = expressionText.value
  expressionOpen.value = false
}

function save() {
  emit('save', {
    priceFabricIds: form.priceFabricIds,
    conditionExpression: form.conditionExpression,
    unitPrice: Number(form.unitPrice || 0),
    priceFormula: form.priceFormula,
    blankOnly: form.blankOnly
  })
  close()
}

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.batch-price-editor {
  display: grid;
  gap: 16px;
}

.batch-price-editor label {
  color: #344054;
  font-weight: 700;
}

.batch-price-editor__scope,
.batch-price-editor__field {
  display: grid;
  gap: 8px;
}

.batch-price-editor__scope :deep(.el-select) {
  width: 100%;
}

.batch-price-editor__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 14px 16px;
  padding: 14px;
  background: #f8fbff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.batch-price-editor__field--formula {
  grid-column: 1 / -1;
}

.batch-price-editor__field--price :deep(.el-input-number) {
  width: 100%;
}

.batch-price-editor__inline {
  display: flex;
  gap: 8px;
}

.batch-price-editor__inline :deep(.el-input) {
  flex: 1;
}

.batch-price-editor__field p {
  margin: 0;
  color: #667085;
  font-size: 12px;
}
</style>
