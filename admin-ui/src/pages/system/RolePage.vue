<template>
  <div class="app-container role-page system-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px" class="system-table-page__search">
      <el-form-item :label="t('role.roleName')" prop="roleName">
        <el-input v-model="queryParams.roleName" :placeholder="t('role.roleNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('role.roleKey')" prop="roleKey">
        <el-input v-model="queryParams.roleKey" :placeholder="t('role.roleKeyPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('role.roleStatusPlaceholder')" clearable style="width: 240px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" style="width: 330px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          range-separator="-"
          :start-placeholder="t('common.startDate')"
          :end-placeholder="t('common.endDate')"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 system-table-page__toolbar">
      <el-col :span="1.5"><el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['system:role:add']">{{ t('common.add') }}</el-button></el-col>
      <el-col :span="1.5"><el-button plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:role:edit']">{{ t('common.edit') }}</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:role:remove']">{{ t('common.delete') }}</el-button></el-col>
      <el-col :span="1.5"><el-button plain icon="Download" @click="handleExport" v-hasPermi="['system:role:export']">{{ t('common.export') }}</el-button></el-col>
      <span class="selection-count">{{ t('common.selectedCount', { count: ids.length }) }}</span>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="roleList" border class="system-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('role.roleName')" prop="roleName" :show-overflow-tooltip="true" width="150" />
      <el-table-column :label="t('role.roleKey')" prop="roleKey" :show-overflow-tooltip="true" width="150" />
      <el-table-column :label="t('role.roleSort')" prop="roleSort" width="100" align="right" />
      <el-table-column :label="t('user.status')" align="center" width="100">
        <template #default="{ row }"><el-switch v-model="row.status" active-value="1" inactive-value="0" @change="handleStatusChange(row)" /></template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="170">
        <template #default="{ row }"><span>{{ formatUtc(row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column v-if="showOperationColumn" :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }"><AdminTableActions :actions="roleRowActions(row)" /></template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="system-table-page__pagination" @pagination="getList" />

    <AdminDrawer
      v-model="open"
      :title="title"
      size="560px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('role.roleName')" prop="roleName"><el-input v-model="form.roleName" :placeholder="t('role.roleNamePlaceholder')" /></el-form-item>
        <el-form-item prop="roleKey">
          <template #label>
            <span><el-tooltip :content="t('role.roleKeyTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('role.roleKey') }}</span>
          </template>
          <el-input v-model="form.roleKey" :placeholder="t('role.roleKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('role.roleSort')" prop="roleSort"><el-input-number v-model="form.roleSort" controls-position="right" :min="0" /></el-form-item>
        <el-form-item :label="t('user.status')">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('role.menuPermission')">
          <div class="permission-tree-block">
            <p class="permission-tree-hint">{{ t('role.menuPermissionHint') }}</p>
            <div class="permission-tree-actions">
              <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">{{ t('common.expandCollapse') }}</el-checkbox>
              <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">{{ t('role.selectAllNone') }}</el-checkbox>
              <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">{{ t('role.parentChildLink') }}</el-checkbox>
            </div>
          </div>
          <el-tree
            ref="menuRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            :empty-text="t('common.loading')"
            :props="{ label: 'label', children: 'children' }"
            @check="syncSelectedMenuIds"
          />
        </el-form-item>
        <el-form-item :label="t('role.defaultHome')">
          <div class="default-home-block">
            <p class="permission-tree-hint">{{ t('role.defaultHomeHint') }}</p>
            <el-select v-model="form.defaultMenuId" clearable filterable :placeholder="t('role.defaultHomePlaceholder')" style="width: 100%">
              <el-option v-for="option in defaultHomeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item :label="t('user.remark')"><el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button><el-button @click="cancel">{{ t('common.cancel') }}</el-button></div></template>
    </AdminDrawer>

    <AdminDrawer
      v-model="openDataScope"
      :title="t('role.assignDataScope')"
      size="560px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="dataScopeCloseGuard.beforeClose"
      @closed="dataScopeCloseGuard.handleClosed"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item :label="t('role.roleName')"><el-input v-model="form.roleName" disabled /></el-form-item>
        <el-form-item :label="t('role.roleKey')"><el-input v-model="form.roleKey" disabled /></el-form-item>
        <el-form-item :label="t('role.scopeRange')">
          <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
            <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="form.dataScope === '2'" :label="t('role.dataPermission')">
          <div class="permission-tree-block">
            <p class="permission-tree-hint">{{ t('role.dataPermissionHint') }}</p>
            <div class="permission-tree-actions">
              <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">{{ t('common.expandCollapse') }}</el-checkbox>
              <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">{{ t('role.selectAllNone') }}</el-checkbox>
              <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">{{ t('role.parentChildLink') }}</el-checkbox>
            </div>
          </div>
          <el-tree
            ref="deptRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            :empty-text="t('common.loading')"
            :props="{ label: 'label', children: 'children' }"
          />
        </el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitDataScope">{{ t('common.confirm') }}</el-button><el-button @click="cancelDataScope">{{ t('common.cancel') }}</el-button></div></template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="RolePage">
import { QuestionFilled } from '@element-plus/icons-vue'
import { useRolePage } from './role/useRolePage'

const {
  t,
  sys_normal_disable,
  roleList,
  open,
  loading,
  showSearch,
  ids,
  total,
  dateRange,
  menuOptions,
  menuExpand,
  menuNodeAll,
  deptExpand,
  deptNodeAll,
  deptOptions,
  openDataScope,
  queryRef,
  roleRef,
  menuRef,
  deptRef,
  form,
  queryParams,
  dataScopeOptions,
  defaultHomeOptions,
  single,
  multiple,
  showOperationColumn,
  title,
  rules,
  formCloseGuard,
  dataScopeCloseGuard,
  formatUtc,
  getList,
  handleQuery,
  resetQuery,
  handleDelete,
  roleRowActions,
  handleExport,
  handleSelectionChange,
  handleStatusChange,
  handleAdd,
  handleUpdate,
  handleCheckedTreeExpand,
  handleCheckedTreeNodeAll,
  handleCheckedTreeConnect,
  submitForm,
  cancel,
  dataScopeSelectChange,
  handleDataScope,
  submitDataScope,
  cancelDataScope,
  syncSelectedMenuIds
} = useRolePage()
</script>
