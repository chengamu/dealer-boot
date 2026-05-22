<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('menu.menuName')" prop="menuName">
        <el-input v-model="queryParams.menuName" :placeholder="t('menu.menuNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('menu.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('menu.menuStatus')" clearable style="width: 200px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['system:menu:add']">{{ t('common.add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">{{ t('common.expandCollapse') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="menuName" :label="t('menu.menuName')" :show-overflow-tooltip="true" width="160" />
      <el-table-column prop="icon" :label="t('menu.icon')" align="center" width="100">
        <template #default="{ row }">
          <svg-icon v-if="row.icon" :icon-class="row.icon" />
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" :label="t('menu.orderNum')" width="70" />
      <el-table-column prop="perms" :label="t('menu.perms')" :show-overflow-tooltip="true" />
      <el-table-column prop="component" :label="t('menu.component')" :show-overflow-tooltip="true" />
      <el-table-column prop="status" :label="t('menu.status')" width="90">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" width="170" prop="createTime">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:menu:edit']">{{ t('common.edit') }}</el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(row)" v-hasPermi="['system:menu:add']">{{ t('common.add') }}</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:menu:remove']">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="open" :title="title" width="680px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="t('menu.parentMenu')">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
                value-key="menuId"
                :placeholder="t('menu.parentMenuPlaceholder')"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('menu.menuType')" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio value="M">{{ t('menu.menuTypeDirectory') }}</el-radio>
                <el-radio value="C">{{ t('menu.menuTypeMenu') }}</el-radio>
                <el-radio value="F">{{ t('menu.menuTypeButton') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="24">
            <el-form-item :label="t('menu.menuIcon')" prop="icon">
              <el-popover v-model:visible="showChooseIcon" placement="bottom-start" :width="540" trigger="click" @show="showSelectIcon">
                <template #reference>
                  <el-input v-model="form.icon" :placeholder="t('menu.iconPlaceholder')" readonly @blur="showSelectIcon" v-click-outside="hideSelectIcon">
                    <template #prefix>
                      <svg-icon v-if="form.icon" :icon-class="form.icon" class="el-input__icon menu-icon-preview" />
                      <el-icon v-else><Search /></el-icon>
                    </template>
                  </el-input>
                </template>
                <icon-select ref="iconSelectRef" :active-icon="form.icon" :placeholder="t('menu.iconSearchPlaceholder')" @selected="selected" />
              </el-popover>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('menu.menuName')" prop="menuName">
              <el-input v-model="form.menuName" :placeholder="t('menu.menuNamePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('menu.i18nKey')" prop="i18nKey">
              <el-input v-model="form.i18nKey" :placeholder="t('menu.i18nKeyPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('menu.displayOrder')" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.externalTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.externalLink') }}</span>
              </template>
              <el-radio-group v-model="form.isFrame">
                <el-radio value="0">{{ t('common.yes') }}</el-radio>
                <el-radio value="1">{{ t('common.no') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item prop="path">
              <template #label>
                <span><el-tooltip :content="t('menu.pathTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.path') }}</span>
              </template>
              <el-input v-model="form.path" :placeholder="t('menu.pathPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="12">
            <el-form-item prop="component">
              <template #label>
                <span><el-tooltip :content="t('menu.componentTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.component') }}</span>
              </template>
              <el-input v-model="form.component" :placeholder="t('menu.componentPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'M'" :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.permsTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.permissionString') }}</span>
              </template>
              <el-input v-model="form.perms" :placeholder="t('menu.permsPlaceholder')" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.queryParamTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.queryParam') }}</span>
              </template>
              <el-input v-model="form.queryParam" :placeholder="t('menu.queryParamPlaceholder')" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.cacheTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.cache') }}</span>
              </template>
              <el-radio-group v-model="form.isCache">
                <el-radio value="1">{{ t('menu.cached') }}</el-radio>
                <el-radio value="0">{{ t('menu.notCached') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.visibleTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.visible') }}</span>
              </template>
              <el-radio-group v-model="form.visible">
                <el-radio v-for="dict in sys_show_hide" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <template #label>
                <span><el-tooltip :content="t('menu.statusTooltip')" placement="top"><el-icon><QuestionFilled /></el-icon></el-tooltip>{{ t('menu.menuStatus') }}</span>
              </template>
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="MenuPage">
import { computed, nextTick, reactive, ref } from 'vue'
import { ClickOutside as vClickOutside, ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { QuestionFilled, Search } from '@element-plus/icons-vue'
import IconSelect from '@/components/IconSelect/index.vue'
import { addMenu, delMenu, getMenu, listMenu, updateMenu, type Menu, type MenuQuery } from '@/api/system/menu'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { handleTree } from '@/utils/ruoyi'

interface DictOption {
  label: string
  value: string
  elTagType?: string
  elTagClass?: string
}

type IconSelectExpose = { reset: () => void }

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_show_hide, sys_normal_disable } = useDict('sys_show_hide', 'sys_normal_disable')

const menuList = ref<Menu[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const menuOptions = ref<Menu[]>([])
const isExpandAll = ref(false)
const refreshTable = ref(true)
const showChooseIcon = ref(false)
const queryRef = ref<FormInstance>()
const menuRef = ref<FormInstance>()
const iconSelectRef = ref<IconSelectExpose>()
const form = ref<Menu>({})
const queryParams = reactive<MenuQuery>({})

const title = computed(() => (form.value.menuId ? t('menu.editTitle') : t('menu.addTitle')))
const rules = computed<FormRules>(() => ({
  menuName: [{ required: true, message: t('menu.menuNameRequired'), trigger: 'blur' }],
  orderNum: [{ required: true, message: t('menu.orderNumRequired'), trigger: 'blur' }],
  path: [{ required: true, message: t('menu.pathRequired'), trigger: 'blur' }]
}))

function reset() {
  form.value = {
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: 'M',
    orderNum: undefined,
    isFrame: '1',
    isCache: '0',
    visible: '1',
    status: '1'
  }
  menuRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listMenu(queryParams)
    menuList.value = handleTree(response.data || [], 'menuId') as Menu[]
  } finally {
    loading.value = false
  }
}

async function getTreeselect() {
  const response = await listMenu()
  menuOptions.value = [{ menuId: 0, menuName: t('menu.root'), children: handleTree(response.data || [], 'menuId') as Menu[] }]
}

function cancel() {
  open.value = false
  reset()
}

function showSelectIcon() {
  iconSelectRef.value?.reset()
  showChooseIcon.value = true
}

function selected(name: string) {
  form.value.icon = name
  showChooseIcon.value = false
}

function hideSelectIcon(event: MouseEvent) {
  const target = (event.relatedTarget || event.target) as HTMLElement | null
  if (!target?.className?.toString().includes('el-input__inner')) showChooseIcon.value = false
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleAdd(row?: Menu) {
  reset()
  try {
    await getTreeselect()
    form.value.parentId = row?.menuId ?? 0
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

async function handleUpdate(row: Menu) {
  reset()
  try {
    await getTreeselect()
    if (!row.menuId) return
    const response = await getMenu(row.menuId)
    form.value = response.data || {}
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function submitForm() {
  const valid = await menuRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.menuId) {
      await updateMenu(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addMenu(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleDelete(row: Menu) {
  if (!row.menuId) return
  try {
    await ElMessageBox.confirm(t('menu.deleteConfirm', { name: row.menuName || '' }), t('common.prompt'), { type: 'warning' })
    await delMenu(row.menuId)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

getList()
</script>

<style scoped>
.menu-icon-preview {
  width: 16px;
  height: 32px;
}
</style>
