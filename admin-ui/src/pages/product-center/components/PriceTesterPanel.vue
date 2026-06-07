<template>
  <div class="price-tester-panel">
    <div class="price-tester-panel__head">
      <h2>{{ t('productCenter.price.tester') }}</h2>
      <el-tag :type="resultTag">{{ result.resultStatus || 'PRICED' }}</el-tag>
    </div>
    <p>{{ t('productCenter.price.testerHint') }}</p>
    <el-form label-position="top">
      <el-form-item :label="t('productCenter.price.planVersionId')">
        <el-input-number :model-value="form.pricePlanVersionId" :min="0" controls-position="right" style="width: 100%" @update:model-value="updateForm('pricePlanVersionId', $event)" />
      </el-form-item>
      <el-form-item :label="t('productCenter.price.currencyCode')">
        <el-input :model-value="form.currencyCode" @update:model-value="updateForm('currencyCode', $event)" />
      </el-form-item>
      <el-form-item :label="t('productCenter.price.quantity')">
        <el-input-number :model-value="form.quantity" :min="1" controls-position="right" style="width: 100%" @update:model-value="updateForm('quantity', $event)" />
      </el-form-item>
      <el-form-item :label="t('productCenter.price.inputValues')">
        <el-input :model-value="inputValuesText" type="textarea" :rows="4" @update:model-value="$emit('update:inputValuesText', $event)" />
      </el-form-item>
      <el-form-item :label="t('productCenter.template.selectedOptions')">
        <el-input :model-value="selectedOptionsText" type="textarea" :rows="4" @update:model-value="$emit('update:selectedOptionsText', $event)" />
      </el-form-item>
    </el-form>
    <el-button class="price-tester-panel__button" type="primary" icon="Refresh" @click="$emit('calculate')" v-hasPermi="['product:price:test']">
      {{ t('productCenter.price.calculate') }}
    </el-button>
    <el-button class="price-tester-panel__button" icon="Document" @click="$emit('openQuotePreview')" v-hasPermi="['product:price:test']">
      {{ t('productCenter.menu.quotePreview') }}
    </el-button>
    <price-hit-details :result="result" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'
import PriceHitDetails from '@/pages/product-center/components/PriceHitDetails.vue'

const props = defineProps<{
  form: { pricePlanVersionId?: number; currencyCode?: string; quantity?: number }
  inputValuesText: string
  selectedOptionsText: string
  result: ProductRecord
}>()

const emit = defineEmits<{
  calculate: []
  openQuotePreview: []
  'update:form': [form: { pricePlanVersionId?: number; currencyCode?: string; quantity?: number }]
  'update:inputValuesText': [value: string]
  'update:selectedOptionsText': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const resultTag = computed(() => {
  if (props.result.resultStatus === 'BLOCKER') return 'danger'
  if (props.result.resultStatus === 'WARNING') return 'warning'
  return 'success'
})

function updateForm(key: 'pricePlanVersionId' | 'currencyCode' | 'quantity', value: number | string | undefined) {
  emit('update:form', { ...props.form, [key]: value })
}
</script>

<style scoped lang="scss">
.price-tester-panel {
  min-height: 580px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);

  p {
    margin: 0 0 12px;
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
  }
}

.price-tester-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.price-tester-panel__button {
  width: 100%;
  margin-left: 0;
  margin-bottom: 8px;
}
</style>
