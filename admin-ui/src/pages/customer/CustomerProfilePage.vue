<template>
  <div class="app-container merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('customer.name')" prop="customerName"><el-input v-model="queryParams.customerName" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('customer.email')" prop="email"><el-input v-model="queryParams.email" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('customer.owner')" prop="ownerUserId">
        <el-select v-model="queryParams.ownerUserId" clearable filterable style="width: 170px">
          <el-option v-for="item in ownerOptions" :key="item.userId" :label="item.nickName || item.userName" :value="item.userId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 140px">
          <el-option :label="t('common.enabled')" value="ENABLED" />
          <el-option :label="t('common.disabled')" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <el-button type="primary" icon="Plus" @click="openAdd" v-hasPermi="['customer:profile:add']">{{ t('common.add') }}</el-button>
      <el-button plain icon="Edit" :disabled="single" @click="openEdit()" v-hasPermi="['customer:profile:edit']">{{ t('common.edit') }}</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['customer:profile:remove']">{{ t('common.delete') }}</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('customer.name')" prop="customerName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.company')" prop="companyName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.email')" prop="email" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.phone')" prop="phone" min-width="140" />
      <el-table-column :label="t('customer.owner')" prop="ownerName" min-width="130" />
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? t('common.enabled') : t('common.disabled') }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'customer:profile:edit', hidden: row.status === 'ENABLED', onClick: () => openEdit(row) },
            { label: row.status === 'ENABLED' ? t('common.disable') : t('common.enable'), icon: 'Switch', permission: 'customer:profile:edit', onClick: () => toggleStatus(row) },
            { label: t('dealer.sales.history'), icon: 'List', permission: 'dealer:sales:query', onClick: () => openHistory(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'customer:profile:remove', hidden: row.status === 'ENABLED', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="open" :title="title" size="680px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('customer.name')" prop="customerName"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item :label="t('customer.company')" prop="companyName"><el-input v-model="form.companyName" /></el-form-item>
        <el-form-item :label="t('customer.email')" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item :label="t('customer.phone')" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item :label="t('customer.owner')" prop="ownerUserId">
          <el-select v-model="form.ownerUserId" clearable filterable>
            <el-option v-for="item in ownerOptions" :key="item.userId" :label="item.nickName || item.userName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('customer.country')"><el-input v-model="form.country" /></el-form-item>
        <el-form-item :label="t('customer.state')"><el-input v-model="form.state" /></el-form-item>
        <el-form-item :label="t('customer.city')"><el-input v-model="form.city" /></el-form-item>
        <el-form-item :label="t('customer.address1')"><el-input v-model="form.addressLine1" /></el-form-item>
        <el-form-item :label="t('customer.address2')"><el-input v-model="form.addressLine2" /></el-form-item>
        <el-form-item :label="t('customer.postalCode')"><el-input v-model="form.postalCode" /></el-form-item>
        <el-form-item :label="t('common.status')">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">{{ t('common.enabled') }}</el-radio>
            <el-radio value="DISABLED">{{ t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('common.remark')"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submit">{{ t('common.confirm') }}</el-button>
      </template>
    </AdminDrawer>
    <AdminDrawer v-model="historyOpen" :title="t('dealer.sales.history')" size="860px">
      <el-table :data="historyRows" border>
        <el-table-column prop="quoteNo" :label="t('dealer.sales.quoteNo')" min-width="160" />
        <el-table-column prop="orderNo" :label="t('dealer.sales.orderNo')" min-width="160" />
        <el-table-column prop="projectName" :label="t('dealer.sales.project')" min-width="150" />
        <el-table-column :label="t('common.status')" width="110"><template #default="{ row }">{{ documentStatusText(t, row.documentStatus) }}</template></el-table-column>
        <el-table-column :label="t('dealer.sales.totalAmount')" width="120" align="right"><template #default="{ row }">{{ money(row.totalAmount, row.currencyCode) }}</template></el-table-column>
        <el-table-column :label="t('common.operate')" width="90"><template #default="{ row }"><el-button link type="primary" @click="openSalesDocument(row)">{{ t('common.detail') }}</el-button></template></el-table-column>
      </el-table>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { addCustomer, changeCustomerStatus, deleteCustomer, getCustomer, listCustomerOwnerOptions, listCustomers, updateCustomer, type CustomerOwnerOption, type CustomerProfile, type CustomerProfileQuery } from '@/api/customer/profile'
import { formatUtc } from '@/utils/datetime'
import { salesApi, type SalesDocument } from '@/api/dealer-sales'
import { documentStatusText } from '@/pages/dealer-sales/salesPresentation'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
const rows = ref<CustomerProfile[]>([])
const ownerOptions = ref<CustomerOwnerOption[]>([])
const ids = ref<Array<number | string>>([])
const loading = ref(false)
const open = ref(false)
const historyOpen = ref(false)
const historyRows = ref<SalesDocument[]>([])
const showSearch = ref(true)
const total = ref(0)
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<CustomerProfileQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<CustomerProfile>({})
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.customerId ? t('customer.editTitle') : t('customer.addTitle')))
const rules = computed<FormRules<CustomerProfile>>(() => ({
  customerName: [{ required: true, message: t('customer.nameRequired'), trigger: 'blur' }],
  email: [{ required: true, message: t('customer.emailRequired'), trigger: 'blur' }]
}))

async function loadOwners() {
  const res = await listCustomerOwnerOptions()
  ownerOptions.value = res.data || []
}
async function getList() {
  loading.value = true
  try {
    const res = await listCustomers(queryParams)
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}
function handleSelectionChange(selection: CustomerProfile[]) {
  ids.value = selection.map((item) => item.customerId).filter(Boolean) as number[]
}
function openAdd() {
  form.value = { status: 'ENABLED', country: 'US' }
  open.value = true
}
async function openEdit(row?: CustomerProfile) {
  const id = row?.customerId || ids.value[0]
  if (!id) return
  const res = await getCustomer(id)
  form.value = { ...res.data }
  open.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.customerId) await updateCustomer(form.value)
  else await addCustomer(form.value)
  ElMessage.success(form.value.customerId ? t('common.editSuccess') : t('common.addSuccess'))
  open.value = false
  await getList()
}
async function toggleStatus(row: CustomerProfile) {
  if (!row.customerId) return
  await changeCustomerStatus(row.customerId, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success(t('common.operationSuccess'))
  await getList()
}
async function handleDelete(row?: CustomerProfile) {
  const target = row?.customerId || ids.value
  if (!target || (Array.isArray(target) && !target.length)) return
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await deleteCustomer(target)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}
async function openHistory(row: CustomerProfile) {
  if (!row.customerId) return
  historyRows.value = (await salesApi.history(String(row.customerId))).data || []
  historyOpen.value = true
}
function openSalesDocument(row: SalesDocument) {
  if (!row.salesDocumentId) return
  void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
}
function money(value?: number, currency = 'USD') {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(value || 0)
}
loadOwners()
getList()
</script>
