<template>
  <aside class="operations-detail">
    <template v-if="merchant">
      <header class="operations-detail__header"><div><h2>{{ merchant.companyName || merchant.merchantName }}</h2><el-tag size="small" :type="auditType(merchant.auditStatus)">{{ auditLabel(merchant.auditStatus) }}</el-tag></div><el-button text circle :icon="Close" :aria-label="t('common.close')" @click="emit('close')" /></header>
      <section><h3>{{ t('dashboard.operations.detail.basic') }}</h3><dl><div><dt>{{ t('dashboard.operations.detail.merchantName') }}</dt><dd>{{ merchant.merchantName || '-' }}</dd></div><div><dt>{{ t('dashboard.operations.detail.contact') }}</dt><dd>{{ merchant.contactName || '-' }}</dd></div><div><dt>{{ t('dashboard.operations.detail.email') }}</dt><dd>{{ merchant.primaryEmail || '-' }}</dd></div><div><dt>{{ t('dashboard.operations.detail.address') }}</dt><dd>{{ address }}</dd></div></dl></section>
      <section><h3>{{ t('dashboard.operations.detail.profile') }}</h3><dl><div><dt>{{ t('dashboard.operations.detail.status') }}</dt><dd>{{ merchant.status ? merchantStatusText(merchant.status, t) : '-' }}</dd></div><div><dt>{{ t('dashboard.operations.detail.level') }}</dt><dd>{{ merchant.levelName || '-' }}</dd></div><div><dt>{{ t('dashboard.operations.detail.discount') }}</dt><dd>{{ formatDiscountRate(merchant.discountRate) }}</dd></div><div><dt>{{ t('dashboard.operations.detail.credit') }}</dt><dd>{{ formatCreditLimit(merchant.creditLimit) }}</dd></div></dl></section>
      <section><h3>{{ t('dashboard.operations.detail.audit') }}</h3><dl><div><dt>{{ t('dashboard.operations.detail.auditStatus') }}</dt><dd>{{ auditLabel(merchant.auditStatus) }}</dd></div><div><dt>{{ t('dashboard.operations.detail.auditTime') }}</dt><dd>{{ formatUtc(merchant.auditTime || merchant.createTime) }}</dd></div><div v-if="merchant.rejectReason" class="detail-full"><dt>{{ t('dashboard.operations.detail.rejectReason') }}</dt><dd>{{ merchant.rejectReason }}</dd></div></dl></section>
      <footer v-if="merchant.source === 'application' && merchant.auditStatus === 'PENDING'"><el-button type="success" :icon="CircleCheck" v-hasPermi="['system:tenant:application:approve']" @click="emit('approve', merchant)">{{ t('tenant.approve') }}</el-button><el-button type="danger" plain :icon="CircleClose" v-hasPermi="['system:tenant:application:reject']" @click="emit('reject', merchant)">{{ t('tenant.reject') }}</el-button></footer>
    </template>
    <el-empty v-else :description="t('dashboard.operations.selectMerchant')" :image-size="72" />
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CircleCheck, CircleClose, Close } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { OperationsMerchant } from '@/api/platform-operations'
import { formatCreditLimit, formatDiscountRate, merchantStatusText } from '@/api/merchant/format'
import { formatUtc } from '@/utils/datetime'

const props = defineProps<{ merchant?: OperationsMerchant }>()
const emit = defineEmits<{ close: []; approve: [merchant: OperationsMerchant]; reject: [merchant: OperationsMerchant] }>()
const { t } = useI18n()
const address = computed(() => [props.merchant?.country, props.merchant?.state, props.merchant?.city, props.merchant?.addressLine1, props.merchant?.addressLine2, props.merchant?.postalCode].filter(Boolean).join(' · ') || '-')
function auditType(status?: string) { return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning' }
function auditLabel(status?: string) { return status === 'APPROVED' ? t('tenant.statusApproved') : status === 'REJECTED' ? t('tenant.statusRejected') : t('tenant.statusPending') }
</script>

<style scoped>
.operations-detail { min-height: 100%; border: 1px solid #e7ecf4; border-radius: 12px; background: #fff; }
.operations-detail__header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; padding: 18px 18px 14px; border-bottom: 1px solid #edf0f5; }
.operations-detail__header div { display: flex; min-width: 0; align-items: center; gap: 8px; }
.operations-detail__header h2 { overflow: hidden; margin: 0; color: #182230; font-size: 17px; text-overflow: ellipsis; white-space: nowrap; }
.operations-detail section { padding: 14px 18px; border-bottom: 1px solid #edf0f5; }
.operations-detail h3 { margin: 0 0 10px; color: #344054; font-size: 13px; }
.operations-detail dl { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 14px; margin: 0; }
.operations-detail dl div { min-width: 0; }.operations-detail dt { color: #98a2b3; font-size: 12px; }.operations-detail dd { overflow: hidden; margin: 4px 0 0; color: #344054; font-size: 13px; line-height: 19px; text-overflow: ellipsis; white-space: nowrap; }.operations-detail .detail-full { grid-column: 1 / -1; }.operations-detail .detail-full dd { white-space: normal; }
.operations-detail footer { display: flex; gap: 10px; padding: 14px 18px; }.operations-detail :deep(.el-empty) { padding: 94px 16px; }
</style>
