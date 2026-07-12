<template>
  <el-dialog v-model="visible" :title="t('customer.quote.batchPaste.title')" width="680px">
    <el-alert :title="t('customer.quote.batchPaste.hint')" type="info" :closable="false" show-icon />
    <el-input v-model="content" class="paste-input" type="textarea" :rows="12" :placeholder="t('customer.quote.batchPaste.placeholder')" />
    <div class="paste-summary">{{ t('customer.quote.batchPaste.rows', { count: parsed.length }) }}</div>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :disabled="!parsed.length" @click="confirm">{{ t('customer.quote.batchPaste.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { canonicalDecimal, compareDecimals, isExactInch } from '@/utils/businessNumber'

import type { QuotePastedRow } from './quoteWorkbenchTypes'

const emit = defineEmits<{ confirm: [rows: QuotePastedRow[]] }>()
const { t } = useI18n()
const visible = ref(false)
const content = ref('')
const parsed = computed(() => content.value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean).map((line) => {
  const [roomLocation, width, height, quantity] = line.split('\t').map((value) => value.trim())
  return {
    roomLocation,
    orderWidthInch: canonicalDecimal(width) || '',
    orderHeightInch: canonicalDecimal(height) || '',
    quantity: Number(quantity)
  }
}).filter((row) => isExactInch(row.orderWidthInch) && isExactInch(row.orderHeightInch)
  && compareDecimals(row.orderWidthInch, '0') === 1 && compareDecimals(row.orderHeightInch, '0') === 1
  && Number.isInteger(row.quantity) && row.quantity > 0))

function open() { content.value = ''; visible.value = true }
function confirm() {
  if (!parsed.value.length) { ElMessage.warning(t('customer.quote.batchPaste.invalid')); return }
  emit('confirm', parsed.value)
  visible.value = false
}
defineExpose({ open })
</script>

<style scoped>
.paste-input { margin-top: 14px; }
.paste-summary { margin-top: 8px; color: #667085; font-size: 13px; }
</style>
