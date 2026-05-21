<template>
  <el-dialog :title="t('gen.importTitle')" v-model="visible" width="800px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryRef" :inline="true">
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
      <el-table @row-click="clickRow" ref="table" :data="dbTableList" @selection-change="handleSelectionChange" height="260px">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="tableName" :label="t('gen.tableName')" :show-overflow-tooltip="true" />
        <el-table-column prop="tableComment" :label="t('gen.tableComment')" :show-overflow-tooltip="true" />
        <el-table-column prop="createTime" :label="t('common.createTime')" />
        <el-table-column prop="updateTime" :label="t('common.updateTime')" />
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleImportTable">{{ t('common.confirm') }}</el-button>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { listDbTable, importTable } from "@/api/tool/gen";
import { getMessage } from '@/locales'
import useLocaleStore from '@/store/modules/locale'

const total = ref(0);
const visible = ref(false);
const tables = ref([]);
const dbTableList = ref([]);
const { proxy } = getCurrentInstance();
const localeStore = useLocaleStore();
const t = (key) => getMessage(key, localeStore.language);

const queryParams = reactive({ pageNum: 1, pageSize: 10, tableName: undefined, tableComment: undefined });
const emit = defineEmits(["ok"]);

function show() {
  getList();
  visible.value = true;
}
function clickRow(row) {
  proxy.$refs.table.toggleRowSelection(row);
}
function handleSelectionChange(selection) {
  tables.value = selection.map(item => item.tableName);
}
function getList() {
  listDbTable(queryParams).then(res => {
    dbTableList.value = res.rows;
    total.value = res.total;
  });
}
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
function handleImportTable() {
  const tableNames = tables.value.join(",");
  if (tableNames == "") {
    proxy.$modal.msgError(t('gen.selectTableToImport'));
    return;
  }
  importTable({ tables: tableNames }).then(res => {
    proxy.$modal.msgSuccess(res.msg);
    if (res.code === 200) {
      visible.value = false;
      emit("ok");
    }
  });
}

defineExpose({ show });
</script>
