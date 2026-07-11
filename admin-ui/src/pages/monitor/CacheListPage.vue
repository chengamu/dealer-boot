<template>
  <div class="app-container cache-list-page">
    <el-row :gutter="10">
      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="cache-card">
          <template #header>
            <Collection class="card-icon" />
            <span>{{ t('cache.cacheList') }}</span>
            <el-button
              class="header-action"
              link
              type="primary"
              icon="Refresh"
              :aria-label="t('cache.refreshCacheNames')"
              :title="t('cache.refreshCacheNames')"
              @click="refreshCacheNames"
            />
          </template>
          <el-table
            v-loading="loading"
            :data="cacheNames"
            height="calc(100vh - 225px)"
            highlight-current-row
            style="width: 100%"
            @row-click="getCacheKeys"
          >
            <el-table-column :label="t('common.index')" width="70" type="index" align="center" />
            <el-table-column :label="t('cache.cacheName')" align="left" prop="cacheName" :show-overflow-tooltip="true">
              <template #default="{ row }">{{ nameFormatter(row) }}</template>
            </el-table-column>
            <el-table-column :label="t('user.remark')" align="left" prop="remark" :show-overflow-tooltip="true" />
            <el-table-column :label="t('common.operate')" width="120" align="center" class-name="small-padding fixed-width">
              <template #default="{ row }">
                <AdminTableActions :actions="[
                  {
                    label: t('common.clear'),
                    ariaLabel: t('cache.clearCacheName', { name: row.cacheName }),
                    title: t('cache.clearCacheName', { name: row.cacheName }),
                    icon: 'Delete',
                    type: 'danger',
                    permission: 'monitor:cache:remove',
                    stopPropagation: true,
                    onClick: () => handleClearCacheName(row)
                  }
                ]" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="cache-card">
          <template #header>
            <Key class="card-icon" />
            <span>{{ t('cache.keyList') }}</span>
            <el-button
              class="header-action"
              link
              type="primary"
              icon="Refresh"
              :aria-label="t('cache.refreshCacheKeys')"
              :title="t('cache.refreshCacheKeys')"
              @click="refreshCacheKeys"
            />
          </template>
          <el-table
            v-loading="subLoading"
            :data="cacheKeys"
            height="calc(100vh - 225px)"
            highlight-current-row
            style="width: 100%"
            @row-click="handleCacheValue"
          >
            <el-table-column :label="t('common.index')" width="70" type="index" align="center" />
            <el-table-column :label="t('cache.cacheKey')" align="left" :show-overflow-tooltip="true">
              <template #default="{ row }">{{ keyFormatter(row) }}</template>
            </el-table-column>
            <el-table-column :label="t('common.operate')" width="120" align="center" class-name="small-padding fixed-width">
              <template #default="{ row }">
                <AdminTableActions :actions="[
                  {
                    label: t('common.clear'),
                    ariaLabel: t('cache.clearCacheKey', { key: row }),
                    title: t('cache.clearCacheKey', { key: row }),
                    icon: 'Delete',
                    type: 'danger',
                    permission: 'monitor:cache:remove',
                    stopPropagation: true,
                    onClick: () => handleClearCacheKey(row)
                  }
                ]" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="cache-card">
          <template #header>
            <Document class="card-icon" />
            <span>{{ t('cache.cacheContent') }}</span>
            <el-button
              class="header-action"
              link
              type="primary"
              icon="Refresh"
              :aria-label="t('cache.clearAll')"
              :title="t('cache.clearAll')"
              @click="handleClearCacheAll"
              v-hasPermi="['monitor:cache:remove']"
            >
              {{ t('cache.clearAll') }}
            </el-button>
          </template>
          <el-form :model="cacheForm" label-width="110px">
            <el-form-item :label="`${t('cache.cacheName')}:`" prop="cacheName">
              <el-input v-model="cacheForm.cacheName" readonly />
            </el-form-item>
            <el-form-item :label="`${t('cache.cacheKey')}:`" prop="cacheKey">
              <el-input v-model="cacheForm.cacheKey" readonly />
            </el-form-item>
            <el-form-item :label="`${t('cache.cacheValue')}:`" prop="cacheValue">
              <el-input v-model="cacheForm.cacheValue" type="textarea" :rows="8" readonly />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="CacheListPage">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Collection, Document, Key } from '@element-plus/icons-vue'
import {
  clearCacheAll,
  clearCacheKey,
  clearCacheName,
  getCacheValue,
  listCacheKey,
  listCacheName,
  type CacheName,
  type CacheValue
} from '@/api/monitor/cache'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, value), message)
}

const cacheNames = ref<CacheName[]>([])
const cacheKeys = ref<string[]>([])
const cacheForm = ref<CacheValue>({})
const loading = ref(true)
const subLoading = ref(false)
const nowCacheName = ref('')

async function getCacheNames() {
  loading.value = true
  try {
    cacheNames.value = await listCacheName()
  } finally {
    loading.value = false
  }
}

async function refreshCacheNames() {
  try {
    await getCacheNames()
    ElMessage.success(t('cache.refreshCacheNamesSuccess'))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleClearCacheName(row: CacheName) {
  try {
    await clearCacheName(row.cacheName)
    ElMessage.success(t('cache.clearCacheNameSuccess', { name: row.cacheName }))
    await getCacheKeys()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function getCacheKeys(row?: CacheName) {
  const cacheName = row?.cacheName || nowCacheName.value
  if (!cacheName) return
  subLoading.value = true
  try {
    if (cacheName !== nowCacheName.value) {
      cacheKeys.value = []
      cacheForm.value = {}
    }
    cacheKeys.value = await listCacheKey(cacheName)
    nowCacheName.value = cacheName
    if (!cacheKeys.value.includes(cacheForm.value.cacheKey || '')) {
      cacheForm.value = {}
    }
  } finally {
    subLoading.value = false
  }
}

async function refreshCacheKeys() {
  try {
    await getCacheKeys()
    ElMessage.success(t('cache.refreshCacheKeysSuccess'))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleClearCacheKey(cacheKey: string) {
  if (!nowCacheName.value) return
  try {
    await clearCacheKey(nowCacheName.value, cacheKey)
    ElMessage.success(t('cache.clearCacheKeySuccess', { key: cacheKey }))
    await getCacheKeys()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function nameFormatter(row: CacheName) {
  return row.cacheName?.replace(':', '') || ''
}

function keyFormatter(cacheKey: string) {
  return nowCacheName.value ? cacheKey.replace(nowCacheName.value, '') : cacheKey
}

async function handleCacheValue(cacheKey: string) {
  if (!nowCacheName.value) return
  try {
    cacheForm.value = await getCacheValue(nowCacheName.value, cacheKey)
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleClearCacheAll() {
  try {
    await clearCacheAll()
    cacheKeys.value = []
    cacheForm.value = {}
    ElMessage.success(t('cache.clearAllSuccess'))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

onMounted(getCacheNames)
</script>

<style scoped>
.cache-card {
  height: calc(100vh - 125px);
}

.card-icon {
  width: 1em;
  height: 1em;
  margin-right: 6px;
  vertical-align: middle;
}

.header-action {
  float: right;
  padding: 3px 0;
}
</style>
