<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="104px" class="bank-form">
    <el-alert v-if="!loadingAccounts && !accounts.length" :title="t('pay.bank.accountUnavailable')" type="warning" :closable="false" show-icon />
    <el-form-item v-else :label="t('pay.bank.collectionAccount')">
      <el-select v-model="selectedAccountId" :loading="loadingAccounts">
        <el-option v-for="account in accounts" :key="account.bankAccountId" :value="account.bankAccountId" :label="accountLabel(account)" />
      </el-select>
    </el-form-item>
    <el-descriptions v-if="selectedAccount" :column="2" border size="small">
      <el-descriptions-item :label="t('pay.bank.bankName')">{{ selectedAccount.bankName }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.bank.accountName')">{{ selectedAccount.accountName }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.bank.accountNumber')">{{ selectedAccount.accountNumber || selectedAccount.accountNumberMasked }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.bank.swiftCode')">{{ selectedAccount.swiftCode || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-form-item :label="t('pay.bank.payerName')" prop="payerName"><el-input v-model="form.payerName" maxlength="100" /></el-form-item>
    <el-form-item :label="t('pay.reference')" prop="bankReference"><el-input v-model="form.bankReference" maxlength="128" /></el-form-item>
    <el-form-item :label="t('pay.bank.transferredTime')" prop="transferredTime"><el-date-picker v-model="form.transferredTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
    <el-form-item :label="t('pay.amount')"><el-input :model-value="money(amount, currency)" disabled /></el-form-item>
    <el-form-item :label="t('pay.bank.proof')" prop="proofMediaId">
      <ProofMediaUpload v-model="form.proofMediaId" />
    </el-form-item>
    <el-form-item :label="t('pay.remark')"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="300" show-word-limit /></el-form-item>
    <el-button v-hasPermi="['pay:bank:submit']" type="primary" :loading="submitting" class="bank-form__submit" @click="submit">{{ t('pay.bank.submit') }}</el-button>
  </el-form>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { payApi, type BankCollectionAccount } from '@/api/pay'
import { money } from '../payPresentation'
import ProofMediaUpload from './ProofMediaUpload.vue'
import type { DecimalValue } from '@/types/api'
import { decimalToMinorUnits } from '@/utils/businessNumber'

const props = defineProps<{ salesDocumentId: string; payOrderId?: string; amount: DecimalValue; currency: string }>()
const emit = defineEmits<{ refresh: [poll?: boolean] }>()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loadingAccounts = ref(false)
const accounts = ref<BankCollectionAccount[]>([])
const selectedAccountId = ref<string>()
const selectedAccount = computed(() => accounts.value.find(item => item.bankAccountId === selectedAccountId.value))
const form = reactive({ payerName: '', bankReference: '', transferredTime: '', proofMediaId: '', remark: '' })
const required = { required: true, message: t('common.required'), trigger: 'blur' }
const rules: FormRules = { payerName: [required], bankReference: [required], transferredTime: [required], proofMediaId: [required] }

async function submit() {
  if (!selectedAccount.value || !(await formRef.value?.validate().catch(() => false))) return
  submitting.value = true
  try {
    const declaredPrice = decimalToMinorUnits(props.amount, 2)
    if (declaredPrice === null) return
    await payApi.submitBank(props.salesDocumentId, {
      payerName: form.payerName, bankReference: form.bankReference, transferredTime: form.transferredTime,
      declaredPrice, currency: props.currency, proofMediaId: form.proofMediaId, remark: form.remark
    })
    ElMessage.success(t('pay.bank.submitted'))
    emit('refresh', true)
  } finally { submitting.value = false }
}

function accountLabel(account: BankCollectionAccount) {
  return [account.bankName, account.accountName, account.accountNumberMasked || account.accountNumber].filter(Boolean).join(' · ')
}

onMounted(async () => {
  loadingAccounts.value = true
  try {
    accounts.value = await payApi.bankAccounts({ payOrderId: props.payOrderId, currency: props.currency })
    selectedAccountId.value = accounts.value[0]?.bankAccountId
  } finally { loadingAccounts.value = false }
})
</script>

<style scoped>
.bank-form { padding-top: 4px; }
.bank-form :deep(.el-descriptions) { margin-bottom: 14px; }
.bank-form__submit { width: 100%; }
</style>
