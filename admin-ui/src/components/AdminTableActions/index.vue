<template>
  <div class="admin-table-actions" :class="{ 'is-empty': !visibleActions.length }">
    <span v-if="!visibleActions.length" class="admin-table-actions__placeholder">-</span>
    <el-button
      v-for="action in inlineActions"
      :key="action.key || action.label"
      v-bind="action.attrs || {}"
      class="admin-table-actions__button"
      link
      :type="actionType(action)"
      :icon="action.icon"
      :disabled="action.disabled"
      :loading="action.loading"
      :aria-label="action.ariaLabel || action.label"
      :title="action.title || action.label"
      @click="handleAction(action, $event)"
    >
      {{ action.label }}
    </el-button>
    <el-dropdown v-if="dropdownActions.length" trigger="click" popper-class="admin-table-actions__dropdown" @click.stop>
      <el-button class="admin-table-actions__more" :aria-label="moreLabel" :title="moreLabel">
        <el-icon><MoreFilled /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="action in dropdownActions"
            :key="action.key || action.label"
            v-bind="action.attrs || {}"
            :icon="action.icon"
            :disabled="action.disabled || action.loading"
            :class="{ 'is-danger': isDanger(action) }"
            @click="handleAction(action)"
          >
            {{ action.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component, type PropType } from 'vue'
import { MoreFilled } from '@element-plus/icons-vue'
import { checkPermi } from '@/utils/permission'

type AdminTableActionType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

export interface AdminTableAction {
  key?: string | number
  label: string
  ariaLabel?: string
  title?: string
  icon?: string | Component
  type?: AdminTableActionType
  permission?: string | string[]
  hidden?: boolean
  disabled?: boolean
  loading?: boolean
  danger?: boolean
  primary?: boolean
  stopPropagation?: boolean
  attrs?: Record<string, unknown>
  onClick: () => void | Promise<void>
}

const props = defineProps({
  actions: {
    type: Array as PropType<AdminTableAction[]>,
    default: () => []
  },
  moreLabel: {
    type: String,
    default: '更多'
  }
})

const visibleActions = computed(() => props.actions.filter((action) => !action.hidden && hasPermission(action)))
const inlineActions = computed(() => {
  if (visibleActions.value.length <= 2) return visibleActions.value
  const primaryAction = visibleActions.value.find((action) => action.primary && !isDanger(action))
  if (primaryAction) return [primaryAction]
  const firstSafeAction = visibleActions.value.find((action) => !isDanger(action))
  return firstSafeAction ? [firstSafeAction] : []
})
const dropdownActions = computed(() => {
  const inlineSet = new Set(inlineActions.value)
  return visibleActions.value.filter((action) => !inlineSet.has(action))
})

function hasPermission(action: AdminTableAction) {
  if (!action.permission) return true
  const permissions = Array.isArray(action.permission) ? action.permission : [action.permission]
  const nextPermissions = permissions.filter(Boolean)
  return nextPermissions.length ? checkPermi(nextPermissions) : true
}

function isDanger(action: AdminTableAction) {
  return action.danger || action.type === 'danger'
}

function actionType(action: AdminTableAction) {
  if (isDanger(action)) return 'danger'
  return action.type || 'primary'
}

function handleAction(action: AdminTableAction, event?: MouseEvent) {
  if (action.stopPropagation) event?.stopPropagation()
  if (action.disabled || action.loading) return
  void action.onClick()
}
</script>
