<template>
  <AdminDialog v-model="visible" :title="t('pay.supplement.title')" width="560px" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item :label="t('pay.methodLabel')" prop="method">
        <el-select v-model="form.method"><el-option :label="t('pay.method.bank')" value="BANK_TRANSFER" /><el-option :label="t('pay.method.cash')" value="CASH" /></el-select>
      </el-form-item>
      <el-form-item :label="t('pay.amount')" prop="amount"><BusinessNumberInput v-model="form.amount" mode="MONEY" :currency-digits="2" :min="0.01" :allow-zero="false" /></el-form-item>
      <el-form-item :label="t('pay.currency')" prop="currency"><el-input v-model="form.currency" maxlength="3" /></el-form-item>
      <el-form-item :label="t('pay.reference')" prop="reference"><el-input v-model="form.reference" /></el-form-item>
      <el-form-item :label="t('pay.paidTime')" prop="paidTime"><el-date-picker v-model="form.paidTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item :label="t('pay.bank.proof')" prop="proofMediaId"><ProofMediaUpload ref="proofUploadRef" v-model="form.proofMediaId" /></el-form-item>
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
import { platformPaymentApi, type PayOrder } from '@/api/pay'
import ProofMediaUpload from './ProofMediaUpload.vue'
import { decimalToMinorUnits, minorUnitsToDecimal } from '@/utils/businessNumber'

const emit = defineEmits<{ saved: [] }>()
const { t } = useI18n()
const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const proofUploadRef = ref<InstanceType<typeof ProofMediaUpload>>()
const order = ref<PayOrder>()
const audience = ref<'platform'>('platform')
const form = reactive({ method: 'BANK_TRANSFER', amount: '', currency: 'USD', reference: '', paidTime: '', proofMediaId: '', reason: '' })
const required = { required: true, message: t('common.required'), trigger: 'blur' }
const rules: FormRules = { method: [required], amount: [required], currency: [required], reference: [required], paidTime: [required], proofMediaId: [required], reason: [required] }

function open(row: PayOrder, nextAudience: 'platform' = 'platform') {
  order.value = row
  audience.value = nextAudience
  Object.assign(form, { method: 'BANK_TRANSFER', amount: minorUnitsToDecimal(row.price || '0', 2) || '', currency: row.currency || 'USD', reference: '', paidTime: '', proofMediaId: '', reason: '' })
  visible.value = true
}

async function submit() {
  const payOrderId = order.value?.payOrderId || order.value?.id
  if (!payOrderId || !(await formRef.value?.validate().catch(() => false))) return
  submitting.value = true
  try {
    const price = decimalToMinorUnits(form.amount, 2)
    if (price === null) return
    if (audience.value === 'platform') {
      await platformPaymentApi.supplement(payOrderId, {
        method: form.method,
        price,
        currency: form.currency,
        reference: form.reference,
        paidTime: form.paidTime,
        proofMediaId: form.proofMediaId,
        reason: form.reason
      })
    }
    proofUploadRef.value?.commit()
    ElMessage.success(t('pay.supplement.success'))
    visible.value = false
    emit('saved')
  } catch (error) {
    await proofUploadRef.value?.cleanup()
    throw error
  } finally {
    submitting.value = false
  }
}

function handleClosed() {
  void proofUploadRef.value?.cleanup()
}

defineExpose({ open })
</script>
