<template>
  <div class="price-hit-details">
    <div><span>{{ t('productCenter.price.baseAmount') }}</span><strong>{{ result.baseAmount ?? 0 }}</strong></div>
    <div><span>{{ t('productCenter.price.optionAmount') }}</span><strong>{{ result.optionAmount ?? 0 }}</strong></div>
    <div class="is-total"><span>{{ t('productCenter.price.totalAmount') }}</span><strong>{{ result.totalAmount ?? 0 }}</strong></div>
    <div><span>{{ t('productCenter.price.matchedItems') }}</span><strong>{{ matchedCount }}</strong></div>
    <div><span>{{ t('productCenter.template.blockers') }}</span><strong>{{ blockerCount }}</strong></div>
    <el-empty v-if="!result.resultStatus" :description="t('productCenter.price.previewEmpty')" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

const props = defineProps<{
  result: ProductRecord
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const matchedCount = computed(() => (props.result.matchedItems as unknown[] | undefined)?.length || 0)
const blockerCount = computed(() => (props.result.blockers as unknown[] | undefined)?.length || 0)
</script>

<style scoped lang="scss">
.price-hit-details {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;

  > div {
    padding: 10px;
    border-radius: 8px;
    background: #f8fafc;
  }

  span,
  strong {
    display: block;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    margin-top: 4px;
    color: #111827;
    font-size: 20px;
  }

  .is-total {
    grid-column: 1 / -1;
    background: #eff6ff;

    strong {
      color: #2563eb;
      font-size: 26px;
    }
  }

  :deep(.el-empty) {
    grid-column: 1 / -1;
  }
}
</style>
