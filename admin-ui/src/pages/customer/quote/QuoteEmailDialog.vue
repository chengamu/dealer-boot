<template>
  <el-dialog v-model="visible" :title="t('customer.quote.email.title')" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item :label="t('customer.quote.email.recipient')" prop="recipient"><el-input v-model="form.recipient" /></el-form-item>
      <el-form-item :label="t('customer.quote.email.message')"><el-input v-model="form.message" type="textarea" :rows="4" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="sending" @click="send">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { customerQuoteApi, type CustomerQuote } from '@/api/customer/quote'

const { t } = useI18n()
const visible = ref(false)
const sending = ref(false)
const quoteId = ref<string>()
const formRef = ref<FormInstance>()
const form = reactive({ recipient: '', message: '' })
const rules: FormRules = { recipient: [
  { required: true, message: t('customer.quote.email.recipient.required'), trigger: 'blur' },
  { type: 'email', message: t('customer.quote.email.recipient.invalid'), trigger: 'blur' }
] }

function open(quote: CustomerQuote) {
  if (!quote.quoteId) return
  quoteId.value = quote.quoteId
  Object.assign(form, { recipient: quote.customerEmail || '', message: '' })
  visible.value = true
}
async function send() {
  if (!quoteId.value || !(await formRef.value?.validate())) return
  sending.value = true
  try {
    await customerQuoteApi.email(quoteId.value, form)
    visible.value = false
    ElMessage.success(t('common.operationSuccess'))
  } finally { sending.value = false }
}
defineExpose({ open })
</script>
