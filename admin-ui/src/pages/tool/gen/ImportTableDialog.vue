<template>
  <el-dialog v-model="visible" :title="t('gen.importTitle')" width="800px" top="5vh" append-to-body>
    <el-form ref="queryRef" :model="queryParams" :inline="true">
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
    <el-row>
      <el-table ref="tableRef" :data="dbTableList" height="260px" @row-click="clickRow" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="tableName" :label="t('gen.tableName')" :show-overflow-tooltip="true" />
        <el-table-column prop="tableComment" :label="t('gen.tableComment')" :show-overflow-tooltip="true" />
        <el-table-column prop="createTime" :label="t('common.createTime')" />
        <el-table-column prop="updateTime" :label="t('common.updateTime')" />
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleImportTable">{{ t('common.confirm') }}</el-button>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { importTable, listDbTable, type GenTable, type GenTableQuery } from '@/api/tool/gen'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { runUiAction } from '@/utils/action'

const emit = defineEmits<{
  ok: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
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
