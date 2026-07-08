<template>
  <section class="price-issue-panel">
    <div class="price-issue-panel__header">
      <h3>{{ t('productCenter.pricing.issues') }}</h3>
      <el-tag size="small" :type="issues.length ? 'warning' : 'success'">{{ issues.length }}</el-tag>
    </div>
    <el-empty v-if="!issues.length" :description="t('productCenter.pricing.noIssues')" :image-size="70" />
    <el-table v-else :data="issues" border size="small" height="220">
      <el-table-column prop="level" :label="t('productCenter.pricing.issueLevel')" width="98" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="issueTagType(row.level)">{{ issueLevel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sourceType" :label="t('productCenter.pricing.issueSource')" width="140" />
      <el-table-column prop="sourceCode" :label="t('productCenter.pricing.sourceCode')" width="140" />
      <el-table-column prop="message" :label="t('productCenter.pricing.issueMessage')" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">{{ issueMessage(row) }}</template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { PriceValidationIssue } from '@/api/product-pricing/types'

defineProps<{
  issues: PriceValidationIssue[]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function issueTagType(level?: string) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'success'
}

function issueLevel(level?: string) {
  if (level === 'ERROR') return t('productCenter.pricing.issueError')
  if (level === 'WARNING') return t('productCenter.pricing.issueWarning')
  return t('productCenter.pricing.issuePass')
}

function issueMessage(row: PriceValidationIssue) {
  return row.messageKey ? t(row.messageKey) : row.message || '-'
}
</script>

<style scoped>
.price-issue-panel {
  min-width: 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.price-issue-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.price-issue-panel h3 {
  margin: 0;
  color: #1d2129;
  font-size: 16px;
}
</style>
