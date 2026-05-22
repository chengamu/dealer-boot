<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip v-if="search" class="item" effect="dark" :content="showSearch ? t('common.hideSearch') : t('common.showSearch')" placement="top">
        <el-button circle icon="Search" :aria-label="showSearch ? t('common.hideSearch') : t('common.showSearch')" :title="showSearch ? t('common.hideSearch') : t('common.showSearch')" @click="toggleSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('common.refresh')" placement="top">
        <el-button circle icon="Refresh" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="refresh()" />
      </el-tooltip>
      <el-tooltip v-if="columns" class="item" effect="dark" :content="t('common.columnVisibility')" placement="top">
        <el-button circle icon="Menu" :aria-label="t('common.columnVisibility')" :title="t('common.columnVisibility')" @click="showColumn()" />
      </el-tooltip>
    </el-row>
    <el-dialog v-model="open" :title="title" append-to-body>
      <el-transfer
        v-model="value"
        :titles="[t('common.show'), t('common.hide')]"
        :data="columns"
        @change="dataChange"
      />
    </el-dialog>
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
const t = (key: string) => getMessage(key, localeStore.language)

const value = ref<Array<number | string>>([])
const title = ref(t('common.columnVisibility'))
const open = ref(false)

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

<style lang="scss" scoped>
:deep(.el-transfer__button) {
  border-radius: 50%;
  display: block;
  margin-left: 0px;
}
:deep(.el-transfer__button:first-child) {
  margin-bottom: 10px;
}

.my-el-transfer {
  text-align: center;
}
</style>
