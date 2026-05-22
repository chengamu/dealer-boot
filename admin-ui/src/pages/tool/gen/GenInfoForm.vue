<template>
  <el-form ref="genInfoForm" :model="info" :rules="rules" label-width="150px">
    <el-row>
      <el-col :span="12">
        <el-form-item prop="tplCategory">
          <template #label>{{ t('gen.tplCategory') }}</template>
          <el-select v-model="info.tplCategory" @change="tplSelectChange">
            <el-option :label="t('gen.tplCrud')" value="crud" />
            <el-option :label="t('gen.tplTree')" value="tree" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="packageName">
          <template #label>{{ t('gen.packageName') }} <el-tooltip :content="t('gen.packageNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-input v-model="info.packageName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="moduleName">
          <template #label>{{ t('gen.moduleName') }} <el-tooltip :content="t('gen.moduleNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-input v-model="info.moduleName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="businessName">
          <template #label>{{ t('gen.businessName') }} <el-tooltip :content="t('gen.businessNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-input v-model="info.businessName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="functionName">
          <template #label>{{ t('gen.functionName') }} <el-tooltip :content="t('gen.functionNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-input v-model="info.functionName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item>
          <template #label>{{ t('gen.parentMenu') }} <el-tooltip :content="t('gen.parentMenuTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <tree-select v-model:value="info.parentMenuId" :options="menuOptions" :objMap="{ value: 'menuId', label: 'menuName', children: 'children' }" :placeholder="t('gen.parentMenuPlaceholder')" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="genType">
          <template #label>{{ t('gen.genType') }} <el-tooltip :content="t('gen.genTypeTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-radio-group v-model="info.genType">
            <el-radio value="0">{{ t('gen.genZip') }}</el-radio>
            <el-radio value="1">{{ t('gen.genCustomPath') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col v-if="info.genType === '1'" :span="24">
        <el-form-item prop="genPath">
          <template #label>{{ t('gen.genPath') }} <el-tooltip :content="t('gen.genPathTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
          <el-input v-model="info.genPath">
            <template #append>
              <el-dropdown>
                <el-button type="primary">{{ t('gen.recentPath') }}</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="info.genPath = '/'">{{ t('gen.restoreDefaultPath') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-input>
        </el-form-item>
      </el-col>
    </el-row>

    <template v-if="info.tplCategory === 'tree'">
      <h4 class="form-header">{{ t('gen.otherInfo') }}</h4>
      <el-row>
        <el-col :span="12">
          <el-form-item>
            <template #label>{{ t('gen.treeCode') }} <el-tooltip :content="t('gen.treeCodeTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
            <el-select v-model="info.treeCode" :placeholder="t('common.selectPlaceholder')">
              <el-option v-for="(column, index) in info.columns" :key="index" :label="`${column.columnName} : ${column.columnComment}`" :value="column.columnName" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>{{ t('gen.treeParentCode') }} <el-tooltip :content="t('gen.treeParentCodeTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
            <el-select v-model="info.treeParentCode" :placeholder="t('common.selectPlaceholder')">
              <el-option v-for="(column, index) in info.columns" :key="index" :label="`${column.columnName} : ${column.columnComment}`" :value="column.columnName" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>{{ t('gen.treeName') }} <el-tooltip :content="t('gen.treeNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
            <el-select v-model="info.treeName" :placeholder="t('common.selectPlaceholder')">
              <el-option v-for="(column, index) in info.columns" :key="index" :label="`${column.columnName} : ${column.columnComment}`" :value="column.columnName" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <template v-if="info.tplCategory === 'sub'">
      <h4 class="form-header">{{ t('gen.relationInfo') }}</h4>
      <el-row>
        <el-col :span="12">
          <el-form-item>
            <template #label>{{ t('gen.subTableName') }} <el-tooltip :content="t('gen.subTableNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
            <el-select v-model="info.subTableName" :placeholder="t('common.selectPlaceholder')" @change="subSelectChange">
              <el-option v-for="(table, index) in tables" :key="index" :label="`${table.tableName} : ${table.tableComment}`" :value="table.tableName" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>{{ t('gen.subTableFkName') }} <el-tooltip :content="t('gen.subTableFkNameTip')" placement="top"><el-icon><question-filled /></el-icon></el-tooltip></template>
            <el-select v-model="info.subTableFkName" :placeholder="t('common.selectPlaceholder')">
              <el-option v-for="(column, index) in subColumns" :key="index" :label="`${column.columnName} : ${column.columnComment}`" :value="column.columnName" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { listMenu, type Menu } from '@/api/system/menu'
import type { GenColumn, GenTable } from '@/api/tool/gen'
import { handleTree } from '@/utils/ruoyi'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const props = defineProps<{
  info: GenTable
  tables: GenTable[]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const genInfoForm = ref<FormInstance>()
const subColumns = ref<GenColumn[]>([])
const menuOptions = ref<Menu[]>([])
const rules = computed<FormRules<GenTable>>(() => ({
  tplCategory: [{ required: true, message: t('gen.tplCategoryRequired'), trigger: 'blur' }],
  packageName: [{ required: true, message: t('gen.packageNameRequired'), trigger: 'blur' }],
  moduleName: [{ required: true, message: t('gen.moduleNameRequired'), trigger: 'blur' }],
  businessName: [{ required: true, message: t('gen.businessNameRequired'), trigger: 'blur' }],
  functionName: [{ required: true, message: t('gen.functionNameRequired'), trigger: 'blur' }]
}))

function subSelectChange() {
  props.info.subTableFkName = ''
}

function tplSelectChange(value: string) {
  if (value !== 'sub') {
    props.info.subTableName = ''
    props.info.subTableFkName = ''
  }
}

function setSubTableColumns(value?: string) {
  const table = props.tables.find((item) => item.tableName === value)
  subColumns.value = table?.columns || []
}

async function getMenuTreeselect() {
  const response = await listMenu()
  menuOptions.value = handleTree(response.data, 'menuId')
}

async function validate() {
  return await genInfoForm.value?.validate().catch(() => false)
}

watch(() => props.info.subTableName, setSubTableColumns)
getMenuTreeselect()

defineExpose({ validate })
</script>
