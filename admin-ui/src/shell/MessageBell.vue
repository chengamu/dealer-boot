<template>
  <el-popover width="380" placement="bottom-end" trigger="click" popper-class="message-popover" @show="loadNotices">
    <template #reference>
      <el-badge :is-dot="notices.length > 0">
        <button class="icon-button" type="button" :aria-label="t('message.title')" :title="t('message.title')">
          <el-icon><Bell /></el-icon>
        </button>
      </el-badge>
    </template>
    <div class="message-panel" v-loading="loading">
      <div class="message-head">
        <div>
          <strong>{{ t('message.notice') }}</strong>
          <span>{{ notices.length }}</span>
        </div>
        <el-button link type="primary" @click="openNoticePage">{{ t('message.viewAll') }}</el-button>
      </div>
      <div v-if="!loading && notices.length === 0" class="message-empty">
        <el-icon><Bell /></el-icon>
        <strong>{{ t('message.empty') }}</strong>
      </div>
      <el-scrollbar v-else height="280px">
        <button v-for="notice in notices" :key="notice.noticeId" type="button" class="message-item" @click="openNoticePage">
          <span class="message-item__icon"><el-icon><Bell /></el-icon></span>
          <span class="message-item__body">
            <strong>{{ notice.noticeTitle }}</strong>
            <small>{{ formatUtc(notice.createTime) }}</small>
          </span>
        </button>
      </el-scrollbar>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { requestPage } from '@/utils/request'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'

interface NoticeSummary {
  noticeId: number | string
  noticeTitle?: string
  createTime?: string
}

const router = useRouter()
const localeStore = useLocaleStore()
const loading = ref(false)
const notices = ref<NoticeSummary[]>([])
const t = (key: string) => getMessage(key, localeStore.language)

async function loadNotices() {
  loading.value = true
  try {
    const res = await requestPage<NoticeSummary>({
      url: '/system/notice/list',
      method: 'get',
      params: { pageNum: 1, pageSize: 6 }
    })
    notices.value = res.rows || []
  } finally {
    loading.value = false
  }
}

function openNoticePage() {
  router.push('/system/notice')
}
</script>

<style scoped lang="scss">
.icon-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  cursor: pointer;
  border: 0;
  border-radius: 6px;
  background: transparent;
}

.icon-button:hover {
  background: rgba(0, 0, 0, 0.025);
}

:deep(.el-badge__content.is-fixed) {
  top: 10px;
  right: 10px;
}

.message-panel {
  min-height: 210px;
  padding: 2px 0;
}

.message-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 10px;
  border-bottom: 1px solid #e8eef7;
  color: #001b5d;
}

.message-head > div {
  display: flex;
  gap: 8px;
  align-items: center;
}

.message-head span {
  min-width: 22px;
  padding: 2px 7px;
  border-radius: 999px;
  color: #075cff;
  font-size: 12px;
  font-weight: 800;
  text-align: center;
  background: #eaf2ff;
}

.message-empty {
  display: grid;
  min-height: 148px;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #71839c;
  border-radius: 8px;
  background: #f8fbff;
}

.message-empty .el-icon {
  width: 42px;
  height: 42px;
  color: #9aaec7;
  border-radius: 50%;
  background: #edf4fb;
}

.message-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  width: 100%;
  padding: 11px 8px;
  border: 0;
  border-bottom: 1px solid #edf2f8;
  color: #001b5d;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.message-item:hover {
  border-radius: 8px;
  background: #f6f9ff;
}

.message-item__icon {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: #075cff;
  border-radius: 50%;
  background: #eaf2ff;
}

.message-item__body {
  display: grid;
  min-width: 0;
  gap: 4px;
}

.message-item__body strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-item small {
  color: #64748b;
  font-size: 12px;
}

:global(.message-popover.el-popper) {
  border: 1px solid #dbe5f2;
  border-radius: 10px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.14);
}
</style>
