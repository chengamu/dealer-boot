<template>
  <AdminDialog v-model="visible" :title="t('pay.receivable.repayTitle')" width="560px" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item :label="t('pay.amount')" prop="amount">
        <BusinessNumberInput v-model="form.amount" mode="MONEY" :currency-digits="2" :min="0.01" :allow-zero="false" />
      </el-form-item>
      <el-form-item :label="t('pay.paidTime')" prop="paidTime">
        <el-date-picker v-model="form.paidTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item :label="t('pay.methodLabel')" prop="method">
        <el-select v-model="form.method">
          <el-option :label="t('pay.method.bank')" value="BANK_TRANSFER" />
          <el-option :label="t('pay.method.cash')" value="CASH" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('pay.reference')" prop="reference">
        <el-input v-model="form.reference" />
      </el-form-item>
      <el-form-item :label="t('pay.bank.proof')" prop="proofMediaId">
        <ProofMediaUpload ref="proofUploadRef" v-model="form.proofMediaId" />
      </el-form-item>
      <el-form-item :label="t('pay.reason')" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="300" show-word-limit />
      </el-form-item>
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
import { platformReceivableApi, type Receivable } from '@/api/pay'
import ProofMediaUpload from './ProofMediaUpload.vue'
import { createIdempotencyKey } from '../payGridSupport'

const emit = defineEmits<{ saved: [] }>()
const { t } = useI18n()
const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const proofUploadRef = ref<InstanceType<typeof ProofMediaUpload>>()
const receivable = ref<Receivable>()
const form = reactive({ amount: '', paidTime: '', method: 'BANK_TRANSFER', reference: '', proofMediaId: '', reason: '' })
const required = { required: true, message: t('common.required'), trigger: 'blur' }
const rules: FormRules = {
  amount: [required],
  paidTime: [required],
  method: [required],
  reference: [required],
  proofMediaId: [required],
  reason: [required]
}

function open(row: Receivable) {
  receivable.value = row
  Object.assign(form, {
    amount: row.outstandingAmount || row.receivableAmount || '',
    paidTime: '',
    method: 'BANK_TRANSFER',
    reference: '',
    proofMediaId: '',
    reason: ''
  })
  visible.value = true
}

async function submit() {
  if (!receivable.value?.receivableId || !(await formRef.value?.validate().catch(() => false))) return
  submitting.value = true
  try {
    await platformReceivableApi.repay(receivable.value.receivableId, {
      amount: form.amount,
      paidTime: form.paidTime,
      method: form.method,
      reference: form.reference,
      proofMediaId: form.proofMediaId || undefined,
      reason: form.reason,
      idempotencyKey: createIdempotencyKey('receivable_repay')
    })
    proofUploadRef.value?.commit()
    ElMessage.success(t('pay.credit.repaid'))
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
