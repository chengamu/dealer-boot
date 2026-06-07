<template>
  <div>
    <el-table :data="rows" border>
      <el-table-column :label="t('productCenter.publish.checkCode')" prop="checkCode" min-width="150" />
      <el-table-column :label="t('productCenter.publish.checkNameCn')" prop="checkNameCn" min-width="160" />
      <el-table-column :label="t('productCenter.publish.checkStatus')" prop="checkStatus" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.checkStatus)">{{ row.checkStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.publish.messageCn')" prop="messageCn" min-width="220" show-overflow-tooltip />
      <el-table-column :label="t('common.operate')" width="130">
        <template #default="{ row }">
          <el-button link type="primary" @click="$emit('openItem', row)">
            {{ statusTag(row.checkStatus) === 'danger' ? t('productCenter.publish.fixBlocker') : t('productCenter.publish.viewDetail') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!rows.length" :description="t('productCenter.publish.emptyCheck')" />
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

defineProps<{
  rows: ProductRecord[]
  statusTag: (status: string) => 'success' | 'warning' | 'danger'
}>()

defineEmits<{
  openItem: [row: ProductRecord]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>
