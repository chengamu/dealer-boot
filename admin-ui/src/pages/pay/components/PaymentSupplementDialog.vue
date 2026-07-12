<template>
  <AdminDialog v-model="visible" :title="t('pay.supplement.title')" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item :label="t('pay.methodLabel')" prop="method">
        <el-select v-model="form.method"><el-option :label="t('pay.method.bank')" value="BANK_TRANSFER" /><el-option :label="t('pay.method.cash')" value="CASH" /></el-select>
      </el-form-item>
      <el-form-item :label="t('pay.amount')" prop="amount"><el-input-number v-model="form.amount" :precision="2" :min="0.01" controls-position="right" /></el-form-item>
      <el-form-item :label="t('pay.currency')" prop="currency"><el-input v-model="form.currency" maxlength="3" /></el-form-item>
      <el-form-item :label="t('pay.reference')" prop="reference"><el-input v-model="form.reference" /></el-form-item>
      <el-form-item :label="t('pay.paidTime')" prop="paidTime"><el-date-picker v-model="form.paidTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item :label="t('pay.bank.proof')" prop="proofMediaId"><ProofMediaUpload v-model="form.proofMediaId" /></el-form-item>
      <el-form-item :label="t('pay.reason')" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" maxlength="300" show-word-limit /></el-form-item>
    </el-form>
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import { payApi, type PayOrder } from '@/api/pay'
import ProofMediaUpload from './ProofMediaUpload.vue'

const emit = defineEmits<{ saved: [] }>()
const { t } = useI18n()
const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const order = ref<PayOrder>()
const form = reactive({ method: 'BANK_TRANSFER', amount: 0, currency: 'USD', reference: '', paidTime: '', proofMediaId: '', reason: '' })
const required = { required: true, message: t('common.required'), trigger: 'blur' }
const rules: FormRules = { method: [required], amount: [required], currency: [required], reference: [required], paidTime: [required], proofMediaId: [required], reason: [required] }

function open(row: PayOrder) {
  order.value = row
  Object.assign(form, { method: 'BANK_TRANSFER', amount: (row.price || 0) / 100, currency: row.currency || 'USD', reference: '', paidTime: '', proofMediaId: '', reason: '' })
  visible.value = true
}

async function submit() {
  if (!order.value?.id || !(await formRef.value?.validate().catch(() => false))) return
  submitting.value = true
  try {
    await payApi.supplement(order.value.id, { method: form.method, price: Math.round(form.amount * 100), currency: form.currency, reference: form.reference, paidTime: form.paidTime, proofMediaId: form.proofMediaId, reason: form.reason })
    ElMessage.success(t('pay.supplement.success'))
    visible.value = false
    emit('saved')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
