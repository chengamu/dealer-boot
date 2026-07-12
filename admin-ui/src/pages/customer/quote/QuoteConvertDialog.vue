<template>
  <el-dialog
    v-model="visible"
    :title="t('customer.quote.order.previewTitle')"
    width="1080px"
    destroy-on-close
    @closed="resetState"
  >
    <div v-loading="loading" class="quote-convert-dialog">
      <el-descriptions :column="4" border>
        <el-descriptions-item :label="t('customer.quote.no')">{{ preview.quoteNo || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.customer')">{{ preview.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.project')">{{ preview.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.order.merchantLevel')">{{ preview.merchantLevelName || preview.merchantLevelCode || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="preview.items || []" border class="quote-convert-dialog__table">
        <el-table-column prop="lineNo" :label="t('common.index')" width="58" align="center" />
        <el-table-column prop="roomLocation" :label="t('customer.quote.room')" min-width="130" />
        <el-table-column prop="saleProductName" :label="t('customer.quote.product')" min-width="180" />
        <el-table-column prop="quantity" :label="t('customer.quote.quantity')" width="90" align="center" />
        <el-table-column :label="t('customer.quote.order.listAmount')" width="130" align="right">
          <template #default="{ row }">{{ money(row.listAmount) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.order.discountAmount')" width="130" align="right">
          <template #default="{ row }">{{ money(row.discountAmount) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.amount.shipping')" width="130" align="right">
          <template #default="{ row }">{{ money(row.shippingAmount) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.amount.line')" width="140" align="right">
          <template #default="{ row }"><strong>{{ money(row.lineAmount) }}</strong></template>
        </el-table-column>
      </el-table>

      <div class="quote-convert-dialog__amounts">
        <div><span>{{ t('customer.quote.order.customerAmount') }}</span><b>{{ money(preview.customerQuoteAmount) }}</b></div>
        <div><span>{{ t('customer.quote.order.listAmount') }}</span><b>{{ money(preview.listAmount) }}</b></div>
        <div><span>{{ t('customer.quote.order.discountAmount') }}</span><b>-{{ money(preview.discountAmount) }}</b></div>
        <div><span>{{ t('customer.quote.amount.shipping') }}</span><b>{{ money(preview.shippingAmount) }}</b></div>
        <div class="is-total"><span>{{ t('customer.quote.order.payableAmount') }}</span><b>{{ money(preview.totalAmount) }}</b></div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="quote-convert-dialog__form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('customer.quote.order.recipient')" prop="recipientName"><el-input v-model="form.recipientName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.quote.order.phone')" prop="recipientPhone"><el-input v-model="form.recipientPhone" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('customer.quote.order.address')" prop="shippingAddress"><el-input v-model="form.shippingAddress" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.quote.order.customerPo')"><el-input v-model="form.customerPoNo" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.quote.order.remark')"><el-input v-model="form.remark" /></el-form-item>
          </el-col>
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
import { customerQuoteApi, type CustomerQuote, type QuoteConvertOrderRequest, type QuoteOrderPreview, type QuoteOrderResult } from '@/api/customer/quote'

const emit = defineEmits<{ converted: [result: QuoteOrderResult] }>()
const { t } = useI18n()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const quoteId = ref<string>()
const formRef = ref<FormInstance>()
const preview = reactive<QuoteOrderPreview>({})
const form = reactive<QuoteConvertOrderRequest>({
  recipientName: '',
  recipientPhone: '',
  shippingAddress: '',
  customerPoNo: '',
  remark: '',
  expectedTotalAmount: 0
})
const rules: FormRules = {
  recipientName: [{ required: true, message: t('customer.quote.order.recipient.required'), trigger: 'blur' }],
  recipientPhone: [{ required: true, message: t('customer.quote.order.phone.required'), trigger: 'blur' }],
  shippingAddress: [{ required: true, message: t('customer.quote.order.address.required'), trigger: 'blur' }]
}

async function open(quote: CustomerQuote) {
  if (!quote.quoteId) return
  resetState()
  visible.value = true
  loading.value = true
  quoteId.value = quote.quoteId
  Object.assign(form, {
    recipientName: quote.recipientName || quote.customerName || '',
    recipientPhone: quote.recipientPhone || quote.customerPhone || '',
    shippingAddress: quote.shippingAddress || '',
    customerPoNo: quote.customerPoNo || '',
    remark: '',
    expectedTotalAmount: 0
  })
  try {
    Object.assign(preview, (await customerQuoteApi.orderPreview(quote.quoteId)).data || {})
    form.expectedTotalAmount = preview.totalAmount || 0
  } finally {
    loading.value = false
  }
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
  } finally {
    submitting.value = false
  }
}

function resetState() {
  quoteId.value = undefined
  Object.assign(preview, {})
  Object.assign(form, { recipientName: '', recipientPhone: '', shippingAddress: '', customerPoNo: '', remark: '', expectedTotalAmount: 0 })
}

function money(value?: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: preview.currencyCode || 'USD' }).format(value || 0)
}

defineExpose({ open })
</script>

<style scoped>
.quote-convert-dialog { display: flex; flex-direction: column; gap: 16px; }
.quote-convert-dialog__table { background: #fff; }
.quote-convert-dialog__amounts { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 10px; }
.quote-convert-dialog__amounts div { display: flex; flex-direction: column; gap: 6px; padding: 12px; border: 1px solid #e4e9f0; border-radius: 8px; background: #f8fafc; }
.quote-convert-dialog__amounts span { color: #667085; font-size: 12px; }
.quote-convert-dialog__amounts b { color: #1d2939; font-size: 18px; }
.quote-convert-dialog__amounts .is-total { border-color: #91caff; background: #eaf4ff; }
.quote-convert-dialog__form { padding: 4px 0; }
</style>
