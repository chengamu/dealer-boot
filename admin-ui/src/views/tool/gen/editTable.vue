<template>
  <el-card>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="t('gen.basicInfo')" name="basic"><basic-info-form ref="basicInfo" :info="info" /></el-tab-pane>
      <el-tab-pane :label="t('gen.columnInfo')" name="columnInfo">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight">
          <el-table-column :label="t('common.index')" type="index" min-width="5%" />
          <el-table-column :label="t('gen.columnName')" prop="columnName" min-width="10%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('gen.columnComment')" min-width="10%"><template #default="scope"><el-input v-model="scope.row.columnComment" /></template></el-table-column>
          <el-table-column :label="t('gen.columnType')" prop="columnType" min-width="10%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('gen.javaType')" min-width="11%">
            <template #default="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" /><el-option label="String" value="String" /><el-option label="Integer" value="Integer" /><el-option label="Double" value="Double" /><el-option label="BigDecimal" value="BigDecimal" /><el-option label="Date" value="Date" /><el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('gen.javaField')" min-width="10%"><template #default="scope"><el-input v-model="scope.row.javaField" /></template></el-table-column>
          <el-table-column :label="t('gen.insert')" min-width="5%"><template #default="scope"><el-checkbox true-label="1" false-label="0" v-model="scope.row.isInsert" /></template></el-table-column>
          <el-table-column :label="t('common.edit')" min-width="5%"><template #default="scope"><el-checkbox true-label="1" false-label="0" v-model="scope.row.isEdit" /></template></el-table-column>
          <el-table-column :label="t('gen.list')" min-width="5%"><template #default="scope"><el-checkbox true-label="1" false-label="0" v-model="scope.row.isList" /></template></el-table-column>
          <el-table-column :label="t('gen.query')" min-width="5%"><template #default="scope"><el-checkbox true-label="1" false-label="0" v-model="scope.row.isQuery" /></template></el-table-column>
          <el-table-column :label="t('gen.queryType')" min-width="10%">
            <template #default="scope">
              <el-select v-model="scope.row.queryType"><el-option label="=" value="EQ" /><el-option label="!=" value="NE" /><el-option label=">" value="GT" /><el-option label=">=" value="GE" /><el-option label="<" value="LT" /><el-option label="<=" value="LE" /><el-option label="LIKE" value="LIKE" /><el-option label="BETWEEN" value="BETWEEN" /></el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.required')" min-width="5%"><template #default="scope"><el-checkbox true-label="1" false-label="0" v-model="scope.row.isRequired" /></template></el-table-column>
          <el-table-column :label="t('gen.htmlType')" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.htmlType">
                <el-option :label="t('gen.textInput')" value="input" /><el-option :label="t('gen.textarea')" value="textarea" /><el-option :label="t('gen.select')" value="select" /><el-option :label="t('gen.radio')" value="radio" /><el-option :label="t('gen.checkbox')" value="checkbox" /><el-option :label="t('gen.datetime')" value="datetime" /><el-option :label="t('gen.imageUpload')" value="imageUpload" /><el-option :label="t('gen.fileUpload')" value="fileUpload" /><el-option :label="t('gen.editor')" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('gen.dictType')" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.dictType" clearable filterable :placeholder="t('common.selectPlaceholder')">
                <el-option v-for="dict in dictOptions" :key="dict.dictType" :label="dict.dictName" :value="dict.dictType"><span style="float: left">{{ dict.dictName }}</span><span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span></el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="t('gen.genInfo')" name="genInfo"><gen-info-form ref="genInfo" :info="info" :tables="tables" /></el-tab-pane>
    </el-tabs>
    <el-form label-width="100px"><div style="text-align: center;margin-left:-100px;margin-top:10px;"><el-button type="primary" @click="submitForm()">{{ t('common.submit') }}</el-button><el-button @click="close()">{{ t('common.back') }}</el-button></div></el-form>
  </el-card>
</template>

<script setup name="GenEdit">
import { getGenTable, updateGenTable } from "@/api/tool/gen";
import { optionselect as getDictOptionselect } from "@/api/system/dict/type";
import basicInfoForm from "./basicInfoForm";
import genInfoForm from "./genInfoForm";
import { getMessage } from '@/locales'
import useLocaleStore from '@/store/modules/locale'

const route = useRoute();
const { proxy } = getCurrentInstance();
const localeStore = useLocaleStore();
const t = (key) => getMessage(key, localeStore.language);

const activeName = ref("columnInfo");
const tableHeight = ref(document.documentElement.scrollHeight - 245 + "px");
const tables = ref([]);
const columns = ref([]);
const dictOptions = ref([]);
const info = ref({});

function submitForm() {
  const basicForm = proxy.$refs.basicInfo.$refs.basicInfoForm;
  const genForm = proxy.$refs.genInfo.$refs.genInfoForm;
  Promise.all([basicForm, genForm].map(getFormPromise)).then(res => {
    const validateResult = res.every(item => !!item);
    if (validateResult) {
      const genTable = Object.assign({}, info.value);
      genTable.columns = columns.value;
      genTable.params = { treeCode: info.value.treeCode, treeName: info.value.treeName, treeParentCode: info.value.treeParentCode, parentMenuId: info.value.parentMenuId };
      updateGenTable(genTable).then(res => {
        proxy.$modal.msgSuccess(res.msg);
        if (res.code === 200) close();
      });
    } else {
      proxy.$modal.msgError(t('gen.formValidateFailed'));
    }
  });
}
function getFormPromise(form) {
  return new Promise(resolve => { form.validate(res => { resolve(res); }); });
}
function close() {
  const obj = { path: "/gen", query: { t: Date.now(), pageNum: route.query.pageNum } };
  proxy.$tab.closeOpenPage(obj);
}

(() => {
  const tableId = route.params && route.params.tableId;
  if (tableId) {
    getGenTable(tableId).then(res => {
      columns.value = res.data.rows;
      info.value = res.data.info;
      tables.value = res.data.tables;
    });
    getDictOptionselect().then(response => { dictOptions.value = response.data; });
  }
})();
</script>
