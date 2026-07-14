<template>
  <section class="delivery-card">
    <header class="delivery-card__header">
      <h3>{{ t('pay.delivery') }}</h3>
    </header>
    <div class="delivery-card__grid">
      <div>
        <dt>{{ t('dealer.sales.address') }}</dt>
        <dd>{{ order.shippingAddress || '-' }}</dd>
      </div>
      <div>
        <dt>{{ t('dealer.sales.recipient') }}</dt>
        <dd>{{ order.recipientName || '-' }}</dd>
      </div>
      <div>
        <dt>{{ t('dealer.sales.phone') }}</dt>
        <dd>{{ order.recipientPhone || '-' }}</dd>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'

defineProps<{ order: SalesDocument }>()
const { t } = useI18n()
</script>

<style scoped>
.delivery-card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.delivery-card__header {
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.delivery-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.delivery-card__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(180px, 0.8fr) minmax(160px, 0.8fr);
  gap: 16px;
  padding: 16px;
}

dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

@media (max-width: 900px) {
  .delivery-card__grid {
    grid-template-columns: 1fr;
  }
}
</style>
