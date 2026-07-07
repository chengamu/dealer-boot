<template>
  <div class="app-container merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('merchantLevel.code')" prop="levelCode">
        <el-input v-model="queryParams.levelCode" :placeholder="t('merchantLevel.codePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('merchantLevel.name')" prop="levelName">
        <el-input v-model="queryParams.levelName" :placeholder="t('merchantLevel.namePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('common.status')" clearable style="width: 150px">
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
      <el-button type="primary" plain icon="Plus" @click="openAdd" v-hasPermi="['merchant:level:add']">{{ t('common.add') }}</el-button>
      <el-button type="success" plain icon="Edit" :disabled="single" @click="openEdit()" v-hasPermi="['merchant:level:edit']">{{ t('common.edit') }}</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['merchant:level:remove']">{{ t('common.delete') }}</el-button>
      <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['merchant:level:export']">{{ t('common.export') }}</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" :selectable="isSelectable" />
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('merchantLevel.code')" prop="levelCode" min-width="130" />
      <el-table-column :label="t('merchantLevel.name')" prop="levelName" min-width="150" />
      <el-table-column :label="t('merchantLevel.discount')" width="130" align="right">
        <template #default="{ row }">{{ formatDiscountRate(row.defaultDiscountRate) }}</template>
      </el-table-column>
      <el-table-column :label="t('merchantLevel.credit')" width="150" align="right">
        <template #default="{ row }">{{ formatCreditLimit(row.defaultCreditLimit) }}</template>
      </el-table-column>
      <el-table-column :label="t('merchantLevel.defaultFlag')" width="100" align="center">
        <template #default="{ row }">{{ row.defaultFlag ? t('common.yes') : t('common.no') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ merchantStatusText(row.status, t) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'merchant:level:edit', hidden: row.status === 'ENABLED', onClick: () => openEdit(row) },
            { label: row.status === 'ENABLED' ? t('common.disable') : t('common.enable'), icon: 'Switch', permission: 'merchant:level:edit', onClick: () => toggleStatus(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'merchant:level:remove', hidden: row.status === 'ENABLED', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="open" :title="title" size="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('merchantLevel.code')" prop="levelCode"><el-input v-model="form.levelCode" /></el-form-item>
        <el-form-item :label="t('merchantLevel.name')" prop="levelName"><el-input v-model="form.levelName" /></el-form-item>
        <el-form-item :label="t('merchantLevel.discount')" prop="defaultDiscountRate"><el-input-number v-model="form.defaultDiscountRate" :precision="2" :min="0" :max="1" controls-position="right" /></el-form-item>
        <el-form-item :label="t('merchantLevel.credit')" prop="defaultCreditLimit"><el-input-number v-model="form.defaultCreditLimit" :precision="2" :min="0" controls-position="right" /></el-form-item>
        <el-form-item :label="t('merchantLevel.defaultFlag')"><el-switch v-model="form.defaultFlag" /></el-form-item>
        <el-form-item :label="t('common.sort')" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" controls-position="right" /></el-form-item>
        <el-form-item :label="t('common.remark')"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submit">{{ t('common.confirm') }}</el-button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { addMerchantLevel, changeMerchantLevelStatus, deleteMerchantLevel, getMerchantLevel, listMerchantLevels, updateMerchantLevel, type MerchantLevel, type MerchantLevelQuery } from '@/api/merchant/level'
import { formatCreditLimit, formatDiscountRate, merchantStatusText } from '@/api/merchant/format'
import { download } from '@/utils/request'
import { formatUtc } from '@/utils/datetime'

const { t } = useI18n()
const rows = ref<MerchantLevel[]>([])
const ids = ref<Array<number | string>>([])
const selectedRows = ref<MerchantLevel[]>([])
const loading = ref(false)
const open = ref(false)
const showSearch = ref(true)
const total = ref(0)
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<MerchantLevelQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<MerchantLevel>({})
const single = computed(() => ids.value.length !== 1 || selectedRows.value.some((item) => item.status === 'ENABLED'))
const multiple = computed(() => ids.value.length === 0 || selectedRows.value.some((item) => item.status === 'ENABLED'))
const title = computed(() => (form.value.levelId ? t('merchantLevel.editTitle') : t('merchantLevel.addTitle')))
const rules = computed<FormRules<MerchantLevel>>(() => ({
  levelCode: [{ required: true, message: t('merchantLevel.codeRequired'), trigger: 'blur' }],
  levelName: [{ required: true, message: t('merchantLevel.nameRequired'), trigger: 'blur' }],
  defaultDiscountRate: [{ required: true, message: t('merchantLevel.discountRequired'), trigger: 'blur' }],
  defaultCreditLimit: [{ required: true, message: t('merchantLevel.creditRequired'), trigger: 'blur' }]
}))

async function getList() {
  loading.value = true
  try {
    const res = await listMerchantLevels(queryParams)
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
function handleSelectionChange(selection: MerchantLevel[]) {
  selectedRows.value = selection
  ids.value = selection.map((item) => item.levelId).filter(Boolean) as number[]
}
function isSelectable(row: MerchantLevel) {
  return row.status !== 'ENABLED'
}
function openAdd() {
  form.value = { defaultDiscountRate: 1, defaultCreditLimit: 0, defaultFlag: false, sortOrder: 0, status: 'DISABLED' }
  open.value = true
}
async function openEdit(row?: MerchantLevel) {
  const id = row?.levelId || ids.value[0]
  if (!id) return
  const res = await getMerchantLevel(id)
  if (res.data?.status === 'ENABLED') {
    ElMessage.warning(t('merchantLevel.enabledEditDenied'))
    return
  }
  form.value = { ...res.data }
  open.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.levelId) await updateMerchantLevel(form.value)
  else await addMerchantLevel(form.value)
  ElMessage.success(form.value.levelId ? t('common.editSuccess') : t('common.addSuccess'))
  open.value = false
  await getList()
}
async function toggleStatus(row: MerchantLevel) {
  if (!row.levelId) return
  await changeMerchantLevelStatus(row.levelId, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success(t('common.operationSuccess'))
  await getList()
}
async function handleDelete(row?: MerchantLevel) {
  const target = row?.levelId || ids.value
  if (!target || (Array.isArray(target) && !target.length)) return
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await deleteMerchantLevel(target)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}
function handleExport() {
  download('merchant/levels/export', { ...queryParams }, `merchant_level_${Date.now()}.xlsx`)
}
getList()
</script>
