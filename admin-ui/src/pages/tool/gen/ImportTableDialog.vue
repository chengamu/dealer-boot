<template>
  <AdminDialog v-model="visible" :title="t('gen.importTitle')" width="860px" top="5vh" variant="picker" class="admin-selector-dialog" append-to-body>
    <div class="admin-dialog__toolbar">
      <el-form ref="queryRef" :model="queryParams" :inline="true" class="admin-selector-dialog__form">
      <el-form-item :label="t('gen.tableName')" prop="tableName">
        <el-input v-model="queryParams.tableName" :placeholder="t('gen.tableNamePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('gen.tableComment')" prop="tableComment">
        <el-input v-model="queryParams.tableComment" :placeholder="t('gen.tableCommentPlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
      </el-form>
    </div>
    <div class="admin-dialog__table admin-selector-dialog__table">
      <el-table ref="tableRef" :data="dbTableList" height="100%" border @row-click="clickRow" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="tableName" :label="t('gen.tableName')" :show-overflow-tooltip="true" />
        <el-table-column prop="tableComment" :label="t('gen.tableComment')" :show-overflow-tooltip="true" />
        <el-table-column prop="createTime" :label="t('common.createTime')">
          <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="t('common.updateTime')">
          <template #default="{ row }">{{ formatUtc(row.updateTime) }}</template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total > 0" class="admin-selector-dialog__pagination" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <template #footer>
      <AdminDialogFooter :status="selectedCountText">
        <el-button type="primary" @click="handleImportTable">{{ t('common.confirm') }}</el-button>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { importTable, listDbTable, type GenTable, type GenTableQuery } from '@/api/tool/gen'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { runUiAction } from '@/utils/action'
import { formatUtc } from '@/utils/datetime'

const emit = defineEmits<{
  ok: []
}>()

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const total = ref(0)
const visible = ref(false)
const tables = ref<string[]>([])
const dbTableList = ref<GenTable[]>([])
const queryRef = ref<FormInstance>()
const tableRef = ref()
const queryParams = reactive<GenTableQuery>({
  pageNum: 1,
  pageSize: 10
})
const selectedCountText = computed(() => t('common.selectedCount', { count: tables.value.length }))

async function show() {
  await runUiAction(async () => {
    await getList()
    visible.value = true
  })
}

function clickRow(row: GenTable) {
  tableRef.value?.toggleRowSelection(row)
}

function handleSelectionChange(selection: GenTable[]) {
  tables.value = selection.map((item) => item.tableName || '').filter(Boolean)
}

async function getList() {
  const response = await listDbTable(queryParams)
  dbTableList.value = response.rows || []
  total.value = response.total || 0
}

function handleQuery() {
  queryParams.pageNum = 1
  runUiAction(getList)
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleImportTable() {
  const tableNames = tables.value.join(',')
  if (!tableNames) {
    ElMessage.error(t('gen.selectTableToImport'))
    return
  }
  try {
    const response = await importTable({ tables: tableNames })
    ElMessage.success(response.msg || t('common.addSuccess'))
    if (response.code === 200) {
      visible.value = false
      emit('ok')
    }
  } catch {
    // Request interceptor already displays the backend error.
  }
}

defineExpose({ show })
</script>
