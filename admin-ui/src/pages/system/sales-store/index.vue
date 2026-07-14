<template>
  <div class="app-container sales-store-page system-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="page.queryParams" :inline="true" class="system-table-page__search">
      <el-form-item :label="page.t('salesStore.code')" prop="storeCode">
        <el-input v-model="page.queryParams.storeCode" clearable style="width: 150px" @keyup.enter="page.handleQuery" />
      </el-form-item>
      <el-form-item :label="page.t('salesStore.name')" prop="storeName">
        <el-input v-model="page.queryParams.storeName" clearable style="width: 170px" @keyup.enter="page.handleQuery" />
      </el-form-item>
      <el-form-item :label="page.t('salesStore.dept')" prop="deptId">
        <el-select v-model="page.queryParams.deptId" clearable filterable style="width: 180px">
          <el-option v-for="item in deptOptions" :key="item.deptId" :label="item.deptName" :value="item.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="page.t('salesStore.status')" prop="status">
        <el-select v-model="page.queryParams.status" clearable style="width: 120px">
          <el-option v-for="item in sysNormalDisable" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="page.handleQuery">{{ page.t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="page.resetQuery">{{ page.t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 system-table-page__toolbar">
      <el-button type="primary" icon="Plus" v-hasPermi="['system:sales-store:add']" @click="page.openAdd">
        {{ page.t('common.add') }}
      </el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="page.getList" />
    </el-row>

    <SalesStoreGrid
      :rows="rows"
      :loading="loading"
      :page-num="Number(page.queryParams.pageNum || 1)"
      :page-size="Number(page.queryParams.pageSize || 10)"
      :status-options="sysNormalDisable"
      :show-operation-column="showOperationColumn"
      :build-actions="page.buildRowActions"
      @row-dblclick="page.handleRowDblclick"
    />

    <pagination
      v-show="total > 0"
      v-model:page="page.queryParams.pageNum"
      v-model:limit="page.queryParams.pageSize"
      :total="total"
      class="system-table-page__pagination"
      @pagination="page.getList"
    />

    <SalesStoreFormDrawer
      ref="salesStoreFormRef"
      v-model="editorOpen"
      :title="editorTitle"
      :mode="editorMode"
      :form="editorForm"
      :dept-options="deptOptions"
      :before-close="page.editorCloseGuard.beforeClose"
      :is-dept-disabled="page.isDeptOptionDisabled"
      @cancel="page.closeEditor"
      @submit="page.submitEditor"
      @closed="page.handleEditorClosed"
    />

    <SalesStoreDisableCheckDrawer
      v-model="disableCheckOpen"
      :row="disableCheckRow"
      @cancel="disableCheckOpen = false"
      @confirm="page.confirmDisable"
      @closed="page.handleDisableCheckClosed"
    />
  </div>
</template>

<script setup lang="ts" name="SalesStorePage">
import SalesStoreDisableCheckDrawer from './components/SalesStoreDisableCheckDrawer.vue'
import SalesStoreFormDrawer from './components/SalesStoreFormDrawer.vue'
import SalesStoreGrid from './components/SalesStoreGrid.vue'
import { useSalesStorePage } from './composables/useSalesStorePage'

const page = useSalesStorePage()
const queryRef = page.queryRef
const salesStoreFormRef = page.formRef
const showSearch = page.showSearch
const rows = page.rows
const loading = page.loading
const total = page.total
const deptOptions = page.deptOptions
const sysNormalDisable = page.sys_normal_disable
const showOperationColumn = page.showOperationColumn
const editorOpen = page.editorOpen
const editorMode = page.editorMode
const editorForm = page.editorForm
const editorTitle = page.editorTitle
const disableCheckOpen = page.disableCheckOpen
const disableCheckRow = page.disableCheckRow
</script>
