<template>
  <div class="app-container shipping-template-page">
    <section class="shipping-template-page__search">
      <el-form :inline="true" :model="query">
        <el-form-item :label="t('productCenter.shippingTemplate.code')"><el-input v-model="query.templateCode" clearable /></el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.name')"><el-input v-model="query.templateName" clearable /></el-form-item>
        <el-form-item :label="t('productCenter.common.status')">
          <el-select v-model="query.status" clearable>
            <el-option :label="t('common.enabled')" value="ENABLED" />
            <el-option :label="t('common.disabled')" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="load">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="shipping-template-page__table">
      <div class="shipping-template-page__toolbar">
        <el-button v-hasPermi="['product:shipping-template:add']" type="primary" icon="Plus" @click="openAdd">{{ t('common.add') }}</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border height="calc(100vh - 286px)">
        <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
        <el-table-column prop="templateCode" :label="t('productCenter.shippingTemplate.code')" min-width="160" />
        <el-table-column prop="templateName" :label="t('productCenter.shippingTemplate.name')" min-width="220" />
        <el-table-column prop="currencyCode" :label="t('productCenter.shippingTemplate.currency')" width="110" align="center" />
        <el-table-column :label="t('productCenter.shippingTemplate.defaultFlag')" width="110" align="center">
          <template #default="{ row }"><el-tag :type="row.defaultFlag ? 'success' : 'info'" effect="plain">{{ row.defaultFlag ? t('common.yes') : t('common.no') }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('productCenter.shippingTemplate.ruleCount')" width="110" align="center">
          <template #default="{ row }">{{ row.ruleCount || 0 }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.common.status')" width="110" align="center">
          <template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? t('common.enabled') : t('common.disabled') }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formula.updateTime')" width="170">
          <template #default="{ row }">{{ formatMinute(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['product:shipping-template:edit']" link type="primary" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
            <el-button v-hasPermi="['product:shipping-template:edit']" link type="primary" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? t('productCenter.shippingTemplate.disable') : t('productCenter.shippingTemplate.enable') }}
            </el-button>
            <el-button v-hasPermi="['product:shipping-template:remove']" link type="danger" @click="remove(row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        class="shipping-template-page__pager"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="load"
        @current-change="load"
      />
    </section>

    <ShippingTemplateDrawer v-model="drawerOpen" :template="activeTemplate" @save="save" />
  </div>
</template>

<script setup lang="ts" name="ShippingTemplatePage">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import { shippingTemplateApi } from '@/api/product-pricing/pricing'
import type { ShippingTemplateQuery, ShippingTemplateVO } from '@/api/product-pricing/types'
import ShippingTemplateDrawer from './components/ShippingTemplateDrawer.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const query = reactive<ShippingTemplateQuery>({ pageNum: 1, pageSize: 20 })
const rows = ref<ShippingTemplateVO[]>([])
const total = ref(0)
const loading = ref(false)
const drawerOpen = ref(false)
const activeTemplate = ref<ShippingTemplateVO>()

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

function reset() {
  query.templateCode = ''
  query.templateName = ''
  query.status = ''
  query.pageNum = 1
  void load()
}

function openAdd() {
  activeTemplate.value = undefined
  drawerOpen.value = true
}

async function openEdit(row: ShippingTemplateVO) {
  const response = await shippingTemplateApi.get(row.shippingTemplateId || '')
  activeTemplate.value = response.data || row
  drawerOpen.value = true
}

async function save(payload: ShippingTemplateVO) {
  if (payload.shippingTemplateId) await shippingTemplateApi.update(payload)
  else await shippingTemplateApi.add(payload)
  ElMessage.success(t('common.success'))
  drawerOpen.value = false
  await load()
}

async function toggleStatus(row: ShippingTemplateVO) {
  const next = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await shippingTemplateApi.changeStatus(row.shippingTemplateId || '', next)
  ElMessage.success(t('common.success'))
  await load()
}

async function remove(row: ShippingTemplateVO) {
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await shippingTemplateApi.remove(row.shippingTemplateId || '')
  ElMessage.success(t('common.success'))
  await load()
}

function formatMinute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.shipping-template-page {
  display: grid;
  gap: 8px;
  background: #f3f6fb;
}

.shipping-template-page__search,
.shipping-template-page__table {
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.shipping-template-page__toolbar {
  margin-bottom: 10px;
}

.shipping-template-page__pager {
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
