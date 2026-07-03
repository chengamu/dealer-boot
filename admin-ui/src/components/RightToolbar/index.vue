<template>
  <div class="top-right-btn admin-toolbar-tools" :style="style">
    <el-row class="admin-toolbar-tools__group">
      <el-tooltip v-if="search" class="item" effect="dark" :content="showSearch ? t('common.hideSearch') : t('common.showSearch')" placement="top">
        <el-button
          circle
          icon="Search"
          class="admin-toolbar-tools__button"
          :class="{ 'is-active': showSearch }"
          :aria-label="showSearch ? t('common.hideSearch') : t('common.showSearch')"
          :title="showSearch ? t('common.hideSearch') : t('common.showSearch')"
          @click="toggleSearch()"
        />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('common.refresh')" placement="top">
        <el-button circle icon="Refresh" class="admin-toolbar-tools__button" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="refresh()" />
      </el-tooltip>
      <el-tooltip v-if="columns" class="item" effect="dark" :content="t('common.columnVisibility')" placement="top">
        <el-button circle icon="Menu" class="admin-toolbar-tools__button" :aria-label="t('common.columnVisibility')" :title="t('common.columnVisibility')" @click="showColumn()" />
      </el-tooltip>
    </el-row>
    <AdminDialog v-model="open" :title="title" width="560px" class="column-visibility-dialog" append-to-body>
      <el-transfer
        v-model="value"
        :titles="[t('common.show'), t('common.hide')]"
        :data="columns"
        @change="dataChange"
      />
      <template #footer>
        <AdminDialogFooter :status="hiddenCountText">
          <el-button @click="open = false">{{ t('common.close') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'

type ToolbarColumn = {
  key: number | string
  label?: string
  visible?: boolean
}

const props = withDefaults(defineProps<{
  showSearch?: boolean
  columns?: ToolbarColumn[]
  search?: boolean
  gutter?: number
}>(), {
  showSearch: true,
  search: true,
  gutter: 10
})

const emits = defineEmits<{
  (e: 'update:showSearch', value: boolean): void
  (e: 'queryTable'): void
}>()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const value = ref<Array<number | string>>([])
const title = computed(() => t('common.columnVisibility'))
const open = ref(false)
const hiddenCountText = computed(() => `${t('common.hide')}：${value.value.length}`)

const style = computed(() => {
  const ret: Record<string, string> = {}
  if (props.gutter) {
    ret.marginRight = `${props.gutter / 2}px`
  }
  return ret
})

function toggleSearch() {
  emits('update:showSearch', !props.showSearch)
}

function refresh() {
  emits('queryTable')
}

function dataChange(data: Array<number | string>) {
  props.columns?.forEach((column) => {
    column.visible = !data.includes(column.key)
  })
}

function showColumn() {
  open.value = true
}

props.columns?.forEach((column, index) => {
  if (column.visible === false) {
    value.value.push(column.key ?? index)
  }
})
</script>
