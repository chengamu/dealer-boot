<template>
  <div class="quick-quote-panel">
    <strong>{{ t('productCenter.sales.quickQuote') }}</strong>
    <span>{{ t('productCenter.sales.quickQuoteHint') }}</span>
    <el-button type="primary" icon="Right" @click="$emit('quote')" v-hasPermi="['product:price:test']">{{ t('productCenter.sales.quickQuote') }}</el-button>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineEmits<{
  quote: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.quick-quote-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border-radius: 8px;
  background: #eff6ff;

  strong {
    font-size: 16px;
  }

  span {
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
  }
}
</style>
