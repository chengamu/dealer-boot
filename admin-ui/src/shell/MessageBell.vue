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
        <strong>{{ t('message.notice') }}</strong>
        <el-button link type="primary" @click="router.push('/system/notice')">{{ t('message.viewAll') }}</el-button>
      </div>
      <el-empty v-if="!loading && notices.length === 0" :image-size="72" :description="t('message.empty')" />
      <el-scrollbar v-else height="280px">
        <div v-for="notice in notices" :key="notice.noticeId" class="message-item">
          <strong>{{ notice.noticeTitle }}</strong>
          <span>{{ formatUtc(notice.createTime) }}</span>
        </div>
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
  min-height: 180px;
}

.message-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #e8eef7;
  color: #001b5d;
}

.message-item {
  display: grid;
  gap: 5px;
  padding: 12px 4px;
  border-bottom: 1px solid #edf2f8;
  color: #001b5d;
}

.message-item:hover {
  background: #f6f9ff;
}

.message-item span {
  color: #64748b;
  font-size: 12px;
}

:global(.message-popover.el-popper) {
  border: 1px solid #dbe5f2;
  border-radius: 10px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.14);
}
</style>
