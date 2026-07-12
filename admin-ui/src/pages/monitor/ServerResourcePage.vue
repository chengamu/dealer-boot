<template>
  <div class="app-container server-resource-page">
    <el-skeleton v-if="loading && !resource" :rows="10" animated />
    <el-result v-else-if="loadFailed" icon="error" :title="t('serverResource.loadFailed')">
      <template #extra><el-button type="primary" @click="load">{{ t('serverResource.retry') }}</el-button></template>
    </el-result>
    <template v-else-if="resource">
      <ServerMetricOverview :resource="resource">
        <template #actions>
          <span class="collected-at">
            {{ t('serverResource.collectedAt') }} {{ formatUtc(resource.collectedAt, 'YYYY-MM-DD HH:mm:ss') }}
          </span>
          <el-tooltip :content="t('common.refresh')">
            <el-button :icon="Refresh" circle :loading="loading" @click="load" />
          </el-tooltip>
        </template>
      </ServerMetricOverview>
      <ServerInfoPanels :resource="resource" />
      <ServerDiskTable :disks="resource.disks" />
    </template>
  </div>
</template>

<script setup lang="ts" name="ServerResourcePage">
import { ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { getServerResource, type ServerResource } from '@/api/monitor/server'
import { formatUtc } from '@/utils/datetime'
import ServerDiskTable from './components/ServerDiskTable.vue'
import ServerInfoPanels from './components/ServerInfoPanels.vue'
import ServerMetricOverview from './components/ServerMetricOverview.vue'

const { t } = useI18n()
const resource = ref<ServerResource>()
const loading = ref(false)
const loadFailed = ref(false)

async function load() {
  loading.value = true
  loadFailed.value = false
  try {
    resource.value = await getServerResource()
  } catch {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

void load()
</script>

<style scoped>
.server-resource-page { display: grid; gap: 10px; padding: 42px 10px 10px; background: var(--admin-bg); }
.collected-at { color: #667085; font-size: 12px; font-variant-numeric: tabular-nums; }
@media (max-width: 520px) {
  .server-resource-page { padding-top: 32px; }
  .collected-at { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
</style>
