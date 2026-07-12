<template>
  <section v-if="actions.length" class="sales-quick-actions">
    <button v-for="action in actions" :key="action.key" type="button" @click="emit('select', action.key)">
      <el-icon><component :is="action.icon" /></el-icon>
      <span><strong>{{ action.title }}</strong><small>{{ action.description }}</small></span>
      <el-icon><ArrowRight /></el-icon>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight, DocumentAdd, ShoppingCart, UserFilled } from '@element-plus/icons-vue'
import type { DashboardCapabilities } from '@/api/sales-dashboard'

const props = defineProps<{ capabilities: DashboardCapabilities }>()
const emit = defineEmits<{ select: [target: string] }>()
const { t } = useI18n()

const actions = computed(() => {
  const result: Array<{ key: string; title: string; description: string; icon: Component }> = []
  if (props.capabilities.createQuote) result.push({ key: 'quote', icon: DocumentAdd,
    title: t('dashboard.sales.actions.quote.title'), description: t('dashboard.sales.actions.quote.description') })
  if (props.capabilities.createCustomer) result.push({ key: 'customer', icon: UserFilled,
    title: t('dashboard.sales.actions.customer.title'), description: t('dashboard.sales.actions.customer.description') })
  if (props.capabilities.quickOrder) result.push({ key: 'quickOrder', icon: ShoppingCart,
    title: t('dashboard.sales.actions.quickOrder.title'), description: t('dashboard.sales.actions.quickOrder.description') })
  return result
})
</script>

<style scoped>
.sales-quick-actions { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.sales-quick-actions button { display: grid; grid-template-columns: 34px minmax(0, 1fr) 18px; align-items: center; gap: 10px; min-height: 68px; padding: 11px 13px; border: 1px solid #e9edf5; border-radius: 7px; background: #fff; color: #1677ff; text-align: left; cursor: pointer; }
.sales-quick-actions button:hover { border-color: #b8d2ff; background: #f8fbff; }
.sales-quick-actions strong, .sales-quick-actions small { display: block; }
.sales-quick-actions strong { color: #1d2129; font-size: 14px; }
.sales-quick-actions small { margin-top: 4px; color: #667085; font-size: 12px; line-height: 1.4; }
@media (max-width: 900px) { .sales-quick-actions { grid-template-columns: 1fr; } }
</style>
