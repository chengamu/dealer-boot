<template>
  <el-dialog v-model="visible" :title="t('customer.quote.order.previewTitle')" width="720px" destroy-on-close>
    <div v-loading="loading" class="convert-dialog">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('customer.quote.no')">{{ preview.quoteNo }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.customer')">{{ preview.customerName }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.project')">{{ preview.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.order.merchantLevel')">{{ preview.merchantLevelName || preview.merchantLevelCode || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="convert-dialog__amounts">
        <span>{{ t('customer.quote.order.customerAmount') }} <b>{{ money(preview.customerQuoteAmount) }}</b></span>
        <span>{{ t('customer.quote.order.listAmount') }} <b>{{ money(preview.listAmount) }}</b></span>
        <span>{{ t('customer.quote.order.discountAmount') }} <b>-{{ money(preview.discountAmount) }}</b></span>
        <span>{{ t('customer.quote.amount.shipping') }} <b>{{ money(preview.shippingAmount) }}</b></span>
        <span class="total">{{ t('customer.quote.order.payableAmount') }} <b>{{ money(preview.totalAmount) }}</b></span>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('customer.quote.order.recipient')" prop="recipientName"><el-input v-model="form.recipientName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('customer.quote.order.phone')" prop="recipientPhone"><el-input v-model="form.recipientPhone" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item :label="t('customer.quote.order.address')" prop="shippingAddress"><el-input v-model="form.shippingAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('customer.quote.order.customerPo')"><el-input v-model="form.customerPoNo" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item :label="t('customer.quote.order.remark')"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <el-alert :title="t('customer.quote.order.confirmHint')" type="warning" :closable="false" show-icon />
    </div>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" :disabled="loading" @click="submit">{{ t('customer.quote.action.convert') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { customerQuoteApi, type CustomerQuote, type QuoteOrderPreview, type QuoteOrderResult } from '@/api/customer/quote'

const emit = defineEmits<{ converted: [result: QuoteOrderResult] }>()
const { t } = useI18n()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const quoteId = ref<string>()
const formRef = ref<FormInstance>()
const preview = reactive<QuoteOrderPreview>({})
const form = reactive({ recipientName: '', recipientPhone: '', shippingAddress: '', customerPoNo: '', remark: '' })
const rules: FormRules = {
  recipientName: [{ required: true, message: t('customer.quote.order.recipient.required'), trigger: 'blur' }],
  recipientPhone: [{ required: true, message: t('customer.quote.order.phone.required'), trigger: 'blur' }],
  shippingAddress: [{ required: true, message: t('customer.quote.order.address.required'), trigger: 'blur' }]
}

async function open(quote: CustomerQuote) {
  if (!quote.quoteId) return
  visible.value = true
  loading.value = true
  quoteId.value = quote.quoteId
  Object.assign(form, {
    recipientName: quote.recipientName || quote.customerName || '', recipientPhone: quote.recipientPhone || quote.customerPhone || '',
    shippingAddress: quote.shippingAddress || '', customerPoNo: quote.customerPoNo || '', remark: ''
  })
  try { Object.assign(preview, (await customerQuoteApi.orderPreview(quote.quoteId)).data) } finally { loading.value = false }
}

async function submit() {
  if (!quoteId.value || !(await formRef.value?.validate()) || preview.totalAmount == null) return
  await ElMessageBox.confirm(t('customer.quote.order.confirmHint'), t('common.prompt'), { type: 'warning' })
  submitting.value = true
  try {
    const response = await customerQuoteApi.convertOrder(quoteId.value, { ...form, expectedTotalAmount: preview.totalAmount })
    visible.value = false
    ElMessage.success(t('common.operationSuccess'))
    emit('converted', response.data)
  } finally { submitting.value = false }
}

function money(value?: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: preview.currencyCode || 'USD' }).format(value || 0)
}
defineExpose({ open })
</script>

<style scoped>
.convert-dialog { display: flex; flex-direction: column; gap: 16px; }
.convert-dialog__amounts { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 8px; }
.convert-dialog__amounts span { display: flex; flex-direction: column; gap: 5px; padding: 10px; border: 1px solid #e4e9f0; background: #f8fafc; color: #667085; font-size: 12px; }
.convert-dialog__amounts b { color: #1d2939; font-size: 15px; }
.convert-dialog__amounts .total { border-color: #91caff; background: #eaf4ff; }
</style>
