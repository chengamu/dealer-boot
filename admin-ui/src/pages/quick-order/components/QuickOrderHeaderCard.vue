<template>
  <section class="quick-order-header-card">
    <div class="quick-order-header-card__grid">
      <el-form-item :label="t('dealer.quickOrder.customerLabel')">
        <el-select v-model="order.customerId" filterable :disabled="readonly" @change="selectCustomer">
          <el-option
            v-for="customer in customers"
            :key="customer.customerId"
            :label="customer.companyName || customer.customerName"
            :value="String(customer.customerId || '')"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dealer.quickOrder.recipient')"><el-input v-model="order.recipientName" :disabled="readonly" /></el-form-item>
      <el-form-item :label="t('dealer.quickOrder.phone')"><el-input v-model="order.recipientPhone" :disabled="readonly" /></el-form-item>
      <el-form-item class="quick-order-header-card__address" :label="t('dealer.quickOrder.address')">
        <el-input v-model="order.shippingAddress" :disabled="readonly" />
      </el-form-item>
      <el-form-item :label="t('dealer.quickOrder.customerPo')"><el-input v-model="order.customerPoNo" :disabled="readonly" /></el-form-item>
      <el-form-item class="quick-order-header-card__remark" :label="t('dealer.quickOrder.remark')">
        <el-input v-model="order.remark" :disabled="readonly" maxlength="200" show-word-limit />
      </el-form-item>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { QuickOrder } from '@/api/dealer-sales/quick-order'
import type { CustomerProfile } from '@/api/customer/profile'

const props = defineProps<{
  order: QuickOrder
  customers: CustomerProfile[]
  readonly?: boolean
}>()

const { t } = useI18n()

function selectCustomer(value: string) {
  const customer = props.customers.find((item) => String(item.customerId || '') === value)
  props.order.customerName = customer?.customerName || ''
  props.order.companyName = customer?.companyName || ''
  props.order.recipientName = props.order.recipientName || customer?.customerName || ''
  props.order.recipientPhone = props.order.recipientPhone || customer?.phone || ''
  if (!props.order.shippingAddress) {
    props.order.shippingAddress = [customer?.addressLine1, customer?.addressLine2, customer?.city, customer?.state, customer?.postalCode, customer?.country]
      .filter(Boolean)
      .join(', ')
  }
}
</script>

<style scoped>
.quick-order-header-card { padding: 14px 16px 6px; border: 1px solid #e3e9f2; border-radius: 8px; background: #fff; }
.quick-order-header-card__grid { display: grid; grid-template-columns: minmax(220px, 1.15fr) minmax(180px, 1fr) minmax(170px, 0.9fr) minmax(320px, 1.7fr); gap: 8px 12px; }
.quick-order-header-card__address { grid-column: 4; grid-row: 1 / span 2; }
.quick-order-header-card__remark { grid-column: span 3; }
.quick-order-header-card :deep(.el-form-item) { margin-bottom: 8px; }
@media (max-width: 1480px) {
  .quick-order-header-card__grid { grid-template-columns: repeat(3, minmax(180px, 1fr)); }
  .quick-order-header-card__address,
  .quick-order-header-card__remark { grid-column: span 3; grid-row: auto; }
}
@media (max-width: 1280px) {
  .quick-order-header-card__grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .quick-order-header-card__address,
  .quick-order-header-card__remark { grid-column: span 2; }
}
</style>
