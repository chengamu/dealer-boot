<template>
  <section class="sales-document-facts-card">
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.customer') }}</span>
      <strong>{{ document.customerName || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.merchant') }}</span>
      <strong>{{ document.merchantName || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.address') }}</span>
      <strong>{{ document.shippingAddress || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.customerPo') }}</span>
      <strong>{{ document.customerPoNo || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.project') }}</span>
      <strong>{{ document.projectName || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.owner') }}</span>
      <strong>{{ document.ownerName || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.recipient') }}</span>
      <strong>{{ document.recipientName || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.phone') }}</span>
      <strong>{{ document.recipientPhone || '-' }}</strong>
    </article>
    <article class="sales-document-facts-card__item">
      <span>{{ t('dealer.sales.source') }}</span>
      <strong>{{ sourceText }}</strong>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'
import { sourceTypeText } from '../salesPresentation'

const props = defineProps<{ document: SalesDocument }>()
const { t } = useI18n()
const sourceText = computed(() => {
  const label = sourceTypeText(t, props.document.sourceType)
  return `${label} · ${props.document.sourceNo || props.document.quoteNo || '-'}`
})
</script>

<style scoped>
.sales-document-facts-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 20px;
  padding: 14px 16px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #fff;
}
.sales-document-facts-card__item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}
.sales-document-facts-card__item span { color: #667085; font-size: 12px; font-weight: 600; }
.sales-document-facts-card__item strong { color: #1d2939; line-height: 22px; }
@media (max-width: 1200px) {
  .sales-document-facts-card { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
