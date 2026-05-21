<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item :label="t('gen.dataName')" prop="dataName">
        <el-input v-model="queryParams.dataName" :placeholder="t('gen.dataNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('gen.tableName')" prop="tableName">
        <el-input v-model="queryParams.tableName" :placeholder="t('gen.tableNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('gen.tableComment')" prop="tableComment">
        <el-input v-model="queryParams.tableComment" :placeholder="t('gen.tableCommentPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.createTime')" style="width: 308px">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" :start-placeholder="t('common.startDate')" :end-placeholder="t('common.endDate')" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Download" @click="handleGenTable" v-hasPermi="['tool:gen:code']">{{ t('gen.generate') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="openImportTable" v-hasPermi="['tool:gen:import']">{{ t('common.import') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleEditTable" v-hasPermi="['tool:gen:edit']">{{ t('common.edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['tool:gen:remove']">{{ t('common.delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" align="center" width="55" />
      <el-table-column :label="t('common.index')" type="index" width="50" align="center">
        <template #default="scope"><span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span></template>
      </el-table-column>
      <el-table-column :label="t('gen.tableName')" align="center" prop="tableName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('gen.tableComment')" align="center" prop="tableComment" :show-overflow-tooltip="true" />
      <el-table-column :label="t('gen.className')" align="center" prop="className" :show-overflow-tooltip="true" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="160" />
      <el-table-column :label="t('common.updateTime')" align="center" prop="updateTime" width="160" />
      <el-table-column :label="t('common.operate')" align="center" width="330" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip :content="t('common.preview')" placement="top"><el-button link type="primary" icon="View" :aria-label="t('common.preview')" :title="t('common.preview')" @click="handlePreview(scope.row)" v-hasPermi="['tool:gen:preview']" /></el-tooltip>
          <el-tooltip :content="t('common.edit')" placement="top"><el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" :title="t('common.edit')" @click="handleEditTable(scope.row)" v-hasPermi="['tool:gen:edit']" /></el-tooltip>
          <el-tooltip :content="t('common.delete')" placement="top"><el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" :title="t('common.delete')" @click="handleDelete(scope.row)" v-hasPermi="['tool:gen:remove']" /></el-tooltip>
          <el-tooltip :content="t('common.sync')" placement="top"><el-button link type="primary" icon="Refresh" :aria-label="t('common.sync')" :title="t('common.sync')" @click="handleSynchDb(scope.row)" v-hasPermi="['tool:gen:edit']" /></el-tooltip>
          <el-tooltip :content="t('gen.generateCode')" placement="top"><el-button link type="primary" icon="Download" :aria-label="t('gen.generateCode')" :title="t('gen.generateCode')" @click="handleGenTable(scope.row)" v-hasPermi="['tool:gen:code']" /></el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="preview.title" v-model="preview.open" width="80%" top="5vh" append-to-body class="scrollbar">
      <el-tabs v-model="preview.activeName">
        <el-tab-pane v-for="(value, key) in preview.data" :label="key.substring(key.lastIndexOf('/') + 1, key.indexOf('.vm'))" :name="key.substring(key.lastIndexOf('/') + 1, key.indexOf('.vm'))" :key="value">
          <el-link :underline="false" icon="DocumentCopy" v-copyText="value" v-copyText:callback="copyTextSuccess" style="float:right">&nbsp;{{ t('common.copy') }}</el-link>
          <pre>{{ value }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    <import-table ref="importRef" @ok="handleQuery" />
  </div>
</template>

<script setup name="Gen">
import { listTable, previewTable, delTable, genCode, synchDb } from "@/api/tool/gen";
import router from "@/router";
import importTable from "./importTable";
import { getMessage } from '@/locales'
import useLocaleStore from '@/store/modules/locale'

const route = useRoute();
const { proxy } = getCurrentInstance();
const localeStore = useLocaleStore();
const t = (key) => getMessage(key, localeStore.language);

const tableList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const tableNames = ref([]);
const dateRange = ref([]);
const uniqueId = ref("");

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tableName: undefined,
    tableComment: undefined,
    dataName: "master"
  },
  preview: {
    open: false,
    title: t('gen.previewTitle'),
    data: {},
    activeName: "domain.java"
  }
});

const { queryParams, preview } = toRefs(data);

localStorage.setItem("dataName", queryParams.value.dataName);

onActivated(() => {
  const time = route.query.t;
  if (time != null && time != uniqueId.value) {
    uniqueId.value = time;
    queryParams.value.pageNum = Number(route.query.pageNum);
    dateRange.value = [];
    proxy.resetForm("queryRef");
    getList();
  }
})

function getList() {
  loading.value = true;
  listTable(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    tableList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleQuery() {
  localStorage.setItem("dataName", queryParams.value.dataName);
  queryParams.value.pageNum = 1;
  getList();
}

function handleGenTable(row = {}) {
  const tbNames = row.tableName || tableNames.value;
  if (tbNames == "") {
    proxy.$modal.msgError(t('gen.selectDataToGenerate'));
    return;
  }
  if (row.genType === "1") {
    genCode(row.tableName).then(() => {
      proxy.$modal.msgSuccess(t('gen.generateSuccess') + row.genPath);
    });
  } else {
    proxy.$download.zip("/tool/gen/batchGenCode?tables=" + tbNames, "bm.zip");
  }
}

function handleSynchDb(row) {
  const tableName = row.tableName;
  proxy.$modal.confirm(t('gen.syncConfirm').replace('{name}', tableName)).then(function () {
    return synchDb(tableName);
  }).then(() => {
    proxy.$modal.msgSuccess(t('common.syncSuccess'));
  }).catch(() => {});
}

function openImportTable() {
  proxy.$refs["importRef"].show();
}

function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

function handlePreview(row) {
  previewTable(row.tableId).then(response => {
    preview.value.data = response.data;
    preview.value.open = true;
    preview.value.activeName = "domain.java";
  });
}

function copyTextSuccess() {
  proxy.$modal.msgSuccess(t('common.copySuccess'));
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.tableId);
  tableNames.value = selection.map(item => item.tableName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleEditTable(row = {}) {
  const tableId = row.tableId || ids.value[0];
  router.push({ path: "/tool/gen-edit/index/" + tableId, query: { pageNum: queryParams.value.pageNum } });
}

function handleDelete(row = {}) {
  const tableIds = row.tableId || ids.value;
  proxy.$modal.confirm(t('gen.deleteConfirm').replace('{ids}', tableIds)).then(function () {
    return delTable(tableIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(t('common.deleteSuccess'));
  }).catch(() => {});
}

getList();
</script>
