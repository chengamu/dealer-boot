<template>
  <section class="sales-document-facts-card">
    <el-descriptions :column="3" border>
      <el-descriptions-item :label="t('dealer.sales.customer')">{{ document.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.merchant')">{{ document.merchantName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.source')">{{ sourceText }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.project')">{{ document.projectName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.customerPo')">{{ document.customerPoNo || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.owner')">{{ document.ownerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.recipient')">{{ document.recipientName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.phone')">{{ document.recipientPhone || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.address')" :span="3">{{ document.shippingAddress || '-' }}</el-descriptions-item>
    </el-descriptions>
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
.sales-document-facts-card { padding: 12px; border: 1px solid #e4eaf2; border-radius: 8px; background: #fff; }
</style>
