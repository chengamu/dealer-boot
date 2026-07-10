<template>
  <div class="app-container shipping-template-page">
    <el-form v-show="showSearch" ref="queryRef" :inline="true" :model="query" class="shipping-template-page__search">
      <el-form-item :label="t('productCenter.shippingTemplate.code')" prop="templateCode">
        <el-input v-model="query.templateCode" :placeholder="t('productCenter.shippingTemplate.codePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('productCenter.shippingTemplate.name')" prop="templateName">
        <el-input v-model="query.templateName" :placeholder="t('productCenter.shippingTemplate.namePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('productCenter.common.status')" prop="status">
        <el-select v-model="query.status" :placeholder="t('productCenter.common.status')" clearable>
          <el-option :label="t('common.enabled')" value="ENABLED" />
          <el-option :label="t('common.disabled')" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 shipping-template-page__toolbar">
      <el-button v-hasPermi="['product:shipping-template:add']" type="primary" icon="Plus" @click="openAdd">{{ t('common.add') }}</el-button>
      <el-button v-hasPermi="['product:shipping-template:edit']" plain icon="Edit" :disabled="selectedRows.length !== 1 || selectedRows[0]?.status === 'ENABLED'" @click="openEdit()">{{ t('common.edit') }}</el-button>
      <el-button v-hasPermi="['product:shipping-template:remove']" type="danger" plain icon="Delete" :disabled="!selectedRows.length || selectedRows.some(row => row.status === 'ENABLED')" @click="remove()">{{ t('common.delete') }}</el-button>
      <el-button v-hasPermi="['product:shipping-template:export']" plain icon="Download" @click="handleExport">{{ t('common.export') }}</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="rows"
      border
      class="shipping-template-page__table"
      :empty-text="t('productCenter.common.empty')"
      @selection-change="selectedRows = $event"
      @row-dblclick="openEdit"
    >
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column type="index" :index="rowIndex" :label="t('common.index')" width="70" align="center" />
      <el-table-column prop="templateCode" :label="t('productCenter.shippingTemplate.code')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="templateName" :label="t('productCenter.shippingTemplate.name')" min-width="220" show-overflow-tooltip />
      <el-table-column prop="currencyCode" :label="t('productCenter.shippingTemplate.currency')" width="96" align="center" />
      <el-table-column :label="t('productCenter.shippingTemplate.ruleCount')" width="90" align="right">
        <template #default="{ row }">{{ row.ruleCount || 0 }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.common.status')" width="96" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 'ENABLED'"
            :disabled="!canEdit"
            :before-change="() => toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formula.updateTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'product:shipping-template:edit', primary: true, hidden: row.status === 'ENABLED', onClick: () => openEdit(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'product:shipping-template:remove', hidden: row.status === 'ENABLED', onClick: () => remove(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      class="shipping-template-page__pagination"
      @pagination="load"
    />

    <ShippingTemplateDrawer v-model="drawerOpen" :template="activeTemplate" @save="save" />
  </div>
</template>

<script setup lang="ts" name="ShippingTemplatePage">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import { download } from '@/utils/request'
import { checkPermi } from '@/utils/permission'
import { shippingTemplateApi } from '@/api/product-pricing/pricing'
import type { ShippingTemplateQuery, ShippingTemplateVO } from '@/api/product-pricing/types'
import ShippingTemplateDrawer from './components/ShippingTemplateDrawer.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const query = reactive<ShippingTemplateQuery>({ pageNum: 1, pageSize: 10 })
const queryRef = ref<FormInstance>()
const rows = ref<ShippingTemplateVO[]>([])
const selectedRows = ref<ShippingTemplateVO[]>([])
const total = ref(0)
const loading = ref(false)
const showSearch = ref(true)
const drawerOpen = ref(false)
const activeTemplate = ref<ShippingTemplateVO>()
const canEdit = checkPermi(['product:shipping-template:edit'])

onMounted(load)

async function load() {
  loading.value = true
  try {
    const response = await shippingTemplateApi.list(query)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  void load()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function openAdd() {
  activeTemplate.value = undefined
  drawerOpen.value = true
}

async function openEdit(row?: ShippingTemplateVO) {
  const target = row || selectedRows.value[0]
  if (!target?.shippingTemplateId || target.status === 'ENABLED') return
  const response = await shippingTemplateApi.get(target.shippingTemplateId)
  activeTemplate.value = response.data || target
  drawerOpen.value = true
}

async function save(payload: ShippingTemplateVO) {
  if (payload.shippingTemplateId) await shippingTemplateApi.update(payload)
  else await shippingTemplateApi.add(payload)
  ElMessage.success(payload.shippingTemplateId ? t('common.editSuccess') : t('common.addSuccess'))
  drawerOpen.value = false
  await load()
}

async function toggleStatus(row: ShippingTemplateVO) {
  if (!row.shippingTemplateId) return false
  try {
    const next = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    const messageKey = next === 'ENABLED'
      ? 'productCenter.shippingTemplate.enableConfirm'
      : 'productCenter.shippingTemplate.disableConfirm'
    await ElMessageBox.confirm(t(messageKey), t('common.prompt'), { type: 'warning' })
    await shippingTemplateApi.changeStatus(row.shippingTemplateId, next)
    await load()
    ElMessage.success(t('common.operationSuccess'))
    return true
  } catch {
    return false
  }
}

async function remove(row?: ShippingTemplateVO) {
  const ids = row?.shippingTemplateId
    ? [row.shippingTemplateId]
    : selectedRows.value.map(item => item.shippingTemplateId).filter(Boolean) as Array<string | number>
  if (!ids.length) return
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await shippingTemplateApi.remove(ids)
  ElMessage.success(t('common.deleteSuccess'))
  await load()
}

function handleExport() {
  download('product-pricing/shipping-templates/export', { ...query }, `shipping_templates_${Date.now()}.xlsx`)
}

function rowIndex(index: number) {
  return ((query.pageNum || 1) - 1) * (query.pageSize || 10) + index + 1
}

function formatMinute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.shipping-template-page {
  background: #f3f6fa;
}

.shipping-template-page__search {
  padding: 8px 12px 0;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 8px;
}

.shipping-template-page__search :deep(.el-input),
.shipping-template-page__search :deep(.el-select) {
  width: 210px;
}

.shipping-template-page__toolbar {
  align-items: center;
}

.shipping-template-page__table {
  width: 100%;
}

.shipping-template-page__table :deep(.el-table__empty-block) {
  min-height: 220px;
}

.shipping-template-page__pagination {
  margin-top: 8px;
}
</style>
