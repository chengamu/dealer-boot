<template>
  <div class="credit-panel">
    <el-alert v-if="disabledReason" :title="disabledReason" type="warning" :closable="false" show-icon />
    <el-descriptions :column="1" border>
      <el-descriptions-item :label="t('pay.credit.limit')">{{ money(account?.creditLimit, currency) }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.credit.used')">{{ money(account?.usedCredit, currency) }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.credit.available')">{{ money(available, currency) }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.credit.current')">{{ money(amount, currency) }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.credit.after')"><strong>{{ money(available - amount, currency) }}</strong></el-descriptions-item>
    </el-descriptions>
    <el-checkbox v-model="confirmed" :disabled="Boolean(disabledReason)">{{ t('pay.credit.receivableConfirm') }}</el-checkbox>
    <el-button v-hasPermi="['pay:credit:use']" type="primary" :disabled="!confirmed || Boolean(disabledReason)" :loading="loading" @click="submit">{{ t('pay.credit.submit') }}</el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { payApi, type CreditAccount } from '@/api/pay'
import { money } from '../payPresentation'

const props = defineProps<{ salesDocumentId: string; amount: number; currency: string; account?: CreditAccount }>()
const emit = defineEmits<{ refresh: [poll?: boolean] }>()
const { t } = useI18n()
const loading = ref(false)
const confirmed = ref(false)
const available = computed(() => (props.account?.creditLimit || 0) - (props.account?.usedCredit || 0))
const disabledReason = computed(() => {
  if (!props.account) return t('pay.credit.unavailable')
  if (props.account.status !== 'NORMAL') return t('pay.credit.frozen')
  if (available.value < props.amount) return t('pay.credit.insufficient')
  return ''
})

async function submit() {
  await ElMessageBox.confirm(t('pay.credit.confirmMessage', { amount: money(props.amount, props.currency) }), t('common.prompt'), { type: 'warning' })
  loading.value = true
  try {
    await payApi.useCredit(props.salesDocumentId)
    emit('refresh', true)
  } finally { loading.value = false }
}
</script>

<style scoped>
.credit-panel { display: grid; gap: 12px; }
</style>
