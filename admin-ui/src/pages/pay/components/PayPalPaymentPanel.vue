<template>
  <div class="method-panel">
    <el-alert v-if="checkout" :title="t('pay.paypal.approvalHint')" type="info" :closable="false" show-icon />
    <div class="method-panel__brand">PayPal</div>
    <el-button v-if="!checkout" v-hasPermi="['pay:order:submit']" type="primary" :loading="loading" class="method-panel__primary" @click="create">
      {{ t('pay.paypal.create') }}
    </el-button>
    <template v-else>
      <el-button v-if="checkout.approvalUrl" type="warning" class="method-panel__primary" @click="approve">{{ t('pay.paypal.open') }}</el-button>
      <el-button v-hasPermi="['pay:order:submit']" type="primary" :loading="loading" class="method-panel__primary" @click="capture">{{ t('pay.paypal.capture') }}</el-button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { payApi, type PayPalCheckout } from '@/api/pay'

const props = defineProps<{ salesDocumentId: string }>()
const emit = defineEmits<{ refresh: [poll?: boolean] }>()
const { t } = useI18n()
const checkout = ref<PayPalCheckout>()
const loading = ref(false)

async function create() {
  loading.value = true
  try {
    checkout.value = await payApi.createPayPal(props.salesDocumentId)
    emit('refresh', true)
    if (checkout.value.approvalUrl) window.open(checkout.value.approvalUrl, '_blank', 'noopener,noreferrer')
  } finally { loading.value = false }
}

function approve() {
  if (checkout.value?.approvalUrl) window.open(checkout.value.approvalUrl, '_blank', 'noopener,noreferrer')
}

async function capture() {
  if (!checkout.value?.paypalOrderId) return
  loading.value = true
  try {
    checkout.value = await payApi.capturePayPal(props.salesDocumentId, checkout.value.paypalOrderId)
    ElMessage.success(t('pay.paypal.captureSubmitted'))
    emit('refresh', true)
  } finally { loading.value = false }
}
</script>

<style scoped>
.method-panel { display: grid; gap: 12px; }
.method-panel__brand { padding: 12px; color: #003087; font-size: 22px; font-weight: 700; text-align: center; border: 1px solid #e9edf5; }
.method-panel__primary { width: 100%; }
</style>
