<template>
  <div class="app-container standard-grid-page">
    <el-form v-show="showSearch" inline class="standard-grid-page__search" @submit.prevent>
      <slot name="filters" />
      <el-form-item class="standard-grid-page__actions">
        <el-button type="primary" icon="Search" @click="$emit('query')">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="$emit('reset')">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 standard-grid-page__toolbar">
      <slot name="toolbar" />
      <right-toolbar v-model:showSearch="shellShowSearch" @queryTable="$emit('query')" />
    </el-row>

    <slot />

    <pagination
      v-show="total > 0"
      :page="page"
      :limit="limit"
      :total="total"
      @update:page="$emit('update:page', $event)"
      @update:limit="$emit('update:limit', $event)"
      @pagination="$emit('pagination')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  showSearch: boolean
  total: number
  page: number
  limit: number
}>()

const emit = defineEmits<{
  (event: 'query'): void
  (event: 'reset'): void
  (event: 'pagination'): void
  (event: 'update:showSearch', value: boolean): void
  (event: 'update:page', value: number): void
  (event: 'update:limit', value: number): void
}>()

const { t } = useI18n()

const shellShowSearch = computed({
  get: () => props.showSearch,
  set: (value: boolean) => emit('update:showSearch', value)
})
</script>

<style scoped>
.standard-grid-page {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.standard-grid-page__search,
.standard-grid-page__toolbar {
  padding: 8px 12px;
  border: 1px solid #e3e9f2;
  background: #fff;
}

.standard-grid-page__search {
  display: flex;
  align-items: center;
  gap: 8px 12px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
}

.standard-grid-page__search :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 0;
}

.standard-grid-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.standard-grid-page__actions {
  margin-left: auto;
}
</style>
