<template>
  <el-card>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="t('gen.basicInfo')" name="basic">
        <basic-info-form ref="basicInfo" :info="info" />
      </el-tab-pane>
      <el-tab-pane :label="t('gen.columnInfo')" name="columnInfo">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight">
          <el-table-column :label="t('common.index')" type="index" min-width="5%" />
          <el-table-column :label="t('gen.columnName')" prop="columnName" min-width="10%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('gen.columnComment')" min-width="10%">
            <template #default="{ row }"><el-input v-model="row.columnComment" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.columnType')" prop="columnType" min-width="10%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('gen.javaType')" min-width="11%">
            <template #default="{ row }">
              <el-select v-model="row.javaType">
                <el-option v-for="item in javaTypes" :key="item" :label="item" :value="item" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('gen.javaField')" min-width="10%">
            <template #default="{ row }"><el-input v-model="row.javaField" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.insert')" min-width="5%">
            <template #default="{ row }"><el-checkbox v-model="row.isInsert" true-label="1" false-label="0" /></template>
          </el-table-column>
          <el-table-column :label="t('common.edit')" min-width="5%">
            <template #default="{ row }"><el-checkbox v-model="row.isEdit" true-label="1" false-label="0" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.list')" min-width="5%">
            <template #default="{ row }"><el-checkbox v-model="row.isList" true-label="1" false-label="0" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.query')" min-width="5%">
            <template #default="{ row }"><el-checkbox v-model="row.isQuery" true-label="1" false-label="0" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.queryType')" min-width="10%">
            <template #default="{ row }">
              <el-select v-model="row.queryType">
                <el-option v-for="item in queryTypes" :key="item" :label="item" :value="item" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.required')" min-width="5%">
            <template #default="{ row }"><el-checkbox v-model="row.isRequired" true-label="1" false-label="0" /></template>
          </el-table-column>
          <el-table-column :label="t('gen.htmlType')" min-width="12%">
            <template #default="{ row }">
              <el-select v-model="row.htmlType">
                <el-option :label="t('gen.textInput')" value="input" />
                <el-option :label="t('gen.textarea')" value="textarea" />
                <el-option :label="t('gen.select')" value="select" />
                <el-option :label="t('gen.radio')" value="radio" />
                <el-option :label="t('gen.checkbox')" value="checkbox" />
                <el-option :label="t('gen.datetime')" value="datetime" />
                <el-option :label="t('gen.imageUpload')" value="imageUpload" />
                <el-option :label="t('gen.fileUpload')" value="fileUpload" />
                <el-option :label="t('gen.editor')" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('gen.dictType')" min-width="12%">
            <template #default="{ row }">
              <el-select v-model="row.dictType" clearable filterable :placeholder="t('common.selectPlaceholder')">
                <el-option v-for="dict in dictOptions" :key="dict.dictType" :label="dict.dictName" :value="dict.dictType">
                  <span style="float: left">{{ dict.dictName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="t('gen.genInfo')" name="genInfo">
        <gen-info-form ref="genInfo" :info="info" :tables="tables" />
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="100px">
      <div style="text-align: center; margin-left: -100px; margin-top: 10px">
        <el-button type="primary" @click="submitForm">{{ t('common.submit') }}</el-button>
        <el-button @click="close">{{ t('common.back') }}</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script setup lang="ts" name="GenEditPage">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { optionselect as getDictOptionselect } from '@/api/system/dict/type'
import { getGenTable, updateGenTable, type GenColumn, type GenTable } from '@/api/tool/gen'
import BasicInfoForm from './BasicInfoForm.vue'
import GenInfoForm from './GenInfoForm.vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useTagsViewStore, type TagView } from '@/stores/tagsView'

interface DictTypeOption {
  dictName?: string
  dictType?: string
}

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const activeName = ref('columnInfo')
const tableHeight = ref(`${document.documentElement.scrollHeight - 245}px`)
const tables = ref<GenTable[]>([])
const columns = ref<GenColumn[]>([])
const dictOptions = ref<DictTypeOption[]>([])
const info = ref<GenTable>({})
const basicInfo = ref<InstanceType<typeof BasicInfoForm>>()
const genInfo = ref<InstanceType<typeof GenInfoForm>>()
const javaTypes = ['Long', 'String', 'Integer', 'Double', 'BigDecimal', 'Date', 'Boolean']
const queryTypes = ['EQ', 'NE', 'GT', 'GE', 'LT', 'LE', 'LIKE', 'BETWEEN']

async function submitForm() {
  const validateResult = await Promise.all([basicInfo.value?.validate(), genInfo.value?.validate()])
  if (!validateResult.every(Boolean)) {
    ElMessage.error(t('gen.formValidateFailed'))
    return
  }
  const genTable: GenTable = {
    ...info.value,
    columns: columns.value,
    params: {
      treeCode: info.value.treeCode,
      treeName: info.value.treeName,
      treeParentCode: info.value.treeParentCode,
      parentMenuId: info.value.parentMenuId
    }
  }
  const response = await updateGenTable(genTable)
  ElMessage.success(response.msg || t('common.editSuccess'))
  if (response.code === 200) close()
}

function close() {
  tagsViewStore.delView(route as unknown as TagView)
  router.push({ path: '/tool/gen', query: { t: Date.now(), pageNum: route.query.pageNum } })
}

async function loadDetail() {
  const tableId = String(route.params.tableId || '')
  if (!tableId) return
  const response = await getGenTable(tableId)
  columns.value = response.data.rows || []
  info.value = response.data.info || {}
  info.value.columns = columns.value
  tables.value = response.data.tables || []
  const dictResponse = await getDictOptionselect()
  dictOptions.value = dictResponse.data || []
}

loadDetail()
</script>
