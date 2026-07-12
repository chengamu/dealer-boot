<template>
  <PaymentCheckoutPage v-if="checkoutId" :sales-document-id="checkoutId" @back="closeCheckout" />
  <div v-else class="app-container pay-order-list">
    <el-tabs v-model="activeView" class="pay-order-list__tabs">
      <el-tab-pane :label="t('pay.order.list')" name="orders" />
      <el-tab-pane v-if="canQueryCredit" :label="t('pay.credit.management')" name="credit" />
    </el-tabs>
    <CreditManagementPanel v-if="activeView === 'credit'" />
    <template v-else>
    <el-form :model="query" inline class="pay-order-list__search">
      <el-form-item :label="t('pay.orderNo')"><el-input v-model="query.no" clearable /></el-form-item>
      <el-form-item :label="t('pay.salesOrderNo')"><el-input v-model="query.merchantOrderId" clearable /></el-form-item>
      <el-form-item :label="t('pay.methodLabel')">
        <el-select v-model="query.channelCode" clearable>
          <el-option :label="t('pay.method.paypal')" value="paypal" />
          <el-option :label="t('pay.method.bank')" value="bank_transfer" />
          <el-option :label="t('pay.method.credit')" value="credit_limit" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')">
        <el-select v-model="query.status" clearable>
          <el-option v-for="status in statuses" :key="status" :label="statusText(t, status)" :value="status" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="no" :label="t('pay.orderNo')" min-width="185" show-overflow-tooltip />
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="175"><template #default="{ row }">{{ row.salesOrderNo || row.merchantOrderId || '-' }}</template></el-table-column>
      <el-table-column prop="customerName" :label="t('pay.customer')" min-width="145" show-overflow-tooltip />
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="145" show-overflow-tooltip />
      <el-table-column :label="t('pay.methodLabel')" width="120"><template #default="{ row }">{{ methodText(t, row.channelCode) }}</template></el-table-column>
      <el-table-column :label="t('pay.orderAmount')" width="130" align="right"><template #default="{ row }"><strong>{{ minorMoney(row.price, row.currency) }}</strong></template></el-table-column>
      <el-table-column :label="t('pay.channelOrderNo')" min-width="150" show-overflow-tooltip><template #default="{ row }">{{ row.channelOrderNo || '-' }}</template></el-table-column>
      <el-table-column :label="t('common.status')" width="105" align="center"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(t, row.status) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('common.createTime')" width="160" align="center"><template #default="{ row }">{{ time(row.createTime) }}</template></el-table-column>
      <el-table-column :label="t('pay.successTime')" width="160" align="center"><template #default="{ row }">{{ time(row.successTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="205" fixed="right" align="center">
        <template #default="{ row }"><AdminTableActions :actions="rowActions(row)" /></template>
      </el-table-column>
    </el-table>
    <Pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />

    <PaymentDetailDrawer ref="detailRef" />
    <PaymentSupplementDialog ref="supplementRef" @saved="load" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { payApi, type PayOrder, type PayOrderQuery, type PayOrderStatus } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import { checkPermi } from '@/utils/permission'
import { methodText, minorMoney, statusText, statusType } from './payPresentation'
import PaymentCheckoutPage from './PaymentCheckoutPage.vue'
import PaymentDetailDrawer from './components/PaymentDetailDrawer.vue'
import PaymentSupplementDialog from './components/PaymentSupplementDialog.vue'
import CreditManagementPanel from './components/CreditManagementPanel.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const rows = ref<PayOrder[]>([])
const total = ref(0)
const activeView = ref('orders')
const canQueryCredit = checkPermi(['pay:credit:query'])
const detailRef = ref<InstanceType<typeof PaymentDetailDrawer>>()
const supplementRef = ref<InstanceType<typeof PaymentSupplementDialog>>()
const query = reactive<PayOrderQuery>({ pageNum: 1, pageSize: 20, no: '', merchantOrderId: '', channelCode: '', status: '' })
const statuses: PayOrderStatus[] = [0, 5, 10, 20]
const checkoutId = computed(() => String(route.query.salesDocumentId || ''))

function time(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
function indexMethod(index: number) { return ((query.pageNum || 1) - 1) * (query.pageSize || 20) + index + 1 }

async function load() {
  loading.value = true
  try {
    const response = await payApi.orderList(query)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally { loading.value = false }
}

function search() { query.pageNum = 1; void load() }
function reset() { Object.assign(query, { pageNum: 1, no: '', merchantOrderId: '', channelCode: '', status: '' }); void load() }
function openDetail(row: PayOrder) { detailRef.value?.open(row) }
function openCheckout(row: PayOrder) { if (row.salesDocumentId) void router.replace({ query: { ...route.query, salesDocumentId: row.salesDocumentId } }) }
function closeCheckout() { const next = { ...route.query }; delete next.salesDocumentId; void router.replace({ query: next }) }

async function review(row: PayOrder, approved: boolean) {
  if (!row.extensionId) return
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt(t('pay.bank.rejectReason'), t('pay.bank.reject'), { inputValidator: (value) => Boolean(value?.trim()) || t('common.required') })
    reason = result.value
  } else {
    await ElMessageBox.confirm(t('pay.bank.approveConfirm'), t('common.prompt'), { type: 'warning' })
  }
  await payApi.reviewBank(row.extensionId, approved, reason)
  ElMessage.success(t(approved ? 'pay.bank.approved' : 'pay.bank.rejected'))
  await load()
}

async function repair(row: PayOrder) {
  if (!row.id) return
  await ElMessageBox.confirm(t('pay.repair.confirm'), t('common.prompt'), { type: 'warning' })
  await payApi.repair(row.id)
  ElMessage.success(t('pay.repair.success'))
  await load()
}

async function reconcile(row: PayOrder) {
  if (!row.id) return
  await payApi.reconcilePayPal(row.id)
  ElMessage.success(t('pay.reconcile.success'))
  await load()
}

function rowActions(row: PayOrder): AdminTableAction[] {
  const bankPending = row.status === 5 && Boolean(row.channelCode?.toLowerCase().includes('bank'))
  return [
    { label: t('common.detail'), permission: 'pay:order:query', primary: true, onClick: () => openDetail(row) },
    { label: t('pay.checkout.action'), permission: 'pay:order:submit', hidden: row.status !== 0 || !row.salesDocumentId, onClick: () => openCheckout(row) },
    { label: t('pay.bank.approve'), permission: 'pay:bank:review', hidden: !bankPending, onClick: () => review(row, true) },
    { label: t('pay.bank.reject'), permission: 'pay:bank:review', type: 'danger', hidden: !bankPending, onClick: () => review(row, false) },
    { label: t('pay.supplement.action'), permission: 'pay:order:supplement', hidden: row.status !== 0, onClick: () => supplementRef.value?.open(row) },
    { label: t('pay.reconcile.action'), permission: 'pay:reconcile:execute', hidden: !row.channelCode?.toLowerCase().includes('paypal') || ![0, 5].includes(row.status || 0), onClick: () => reconcile(row) },
    { label: t('pay.repair.action'), permission: 'pay:order:repair', hidden: row.status !== 10, onClick: () => repair(row) }
  ]
}

onMounted(load)
</script>

<style scoped>
.pay-order-list__search { padding: 8px 10px 0; border: 1px solid #eef0f5; background: #fff; }
.pay-order-list__tabs { padding: 0 10px; background: #fff; }
.pay-order-list__search :deep(.el-input), .pay-order-list__search :deep(.el-select) { width: 170px; }
</style>
