<template>
  <div class="fixed-usage-card">
    <label>
      <span>{{ t('productCenter.formulaSetup.fixedUsageQty') }}</span>
      <el-input-number v-model="usageRow.fixedUsageQty" :min="0" :precision="2" controls-position="right" @change="$emit('change')" />
    </label>
  </div>
</template>

<script setup lang="ts" name="FormulaUsageFixedPanel">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaMaterialVO } from '@/api/product-capability/types'

defineProps<{
  usageRow: ProductFormulaMaterialVO
}>()

defineEmits<{
  (event: 'change'): void
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.fixed-usage-card {
  display: grid;
  grid-template-columns: minmax(220px, 360px);
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.fixed-usage-card label {
  display: grid;
  gap: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
}

.fixed-usage-card :deep(.el-input-number) {
  width: 100%;
}
</style>
