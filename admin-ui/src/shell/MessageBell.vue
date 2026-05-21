<template>
  <el-popover width="360" placement="bottom" trigger="click" @show="loadNotices">
    <template #reference>
      <el-badge :is-dot="notices.length > 0">
        <button class="icon-button" type="button" :aria-label="t('message.title')" :title="t('message.title')">
          <el-icon><Bell /></el-icon>
        </button>
      </el-badge>
    </template>
    <div class="message-panel">
      <div class="message-head">
        <strong>{{ t('message.notice') }}</strong>
        <el-button link type="primary" @click="router.push('/system/notice')">{{ t('message.viewAll') }}</el-button>
      </div>
      <el-empty v-if="!loading && notices.length === 0" :image-size="80" :description="t('message.empty')" />
      <el-scrollbar v-else height="280px">
        <div v-for="notice in notices" :key="notice.noticeId" class="message-item">
          <strong>{{ notice.noticeTitle }}</strong>
          <span>{{ parseTime(notice.createTime) || '-' }}</span>
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
import { parseTime } from '@/utils/ruoyi'

const router = useRouter()
const localeStore = useLocaleStore()
const loading = ref(false)
const notices = ref<Record<string, any>[]>([])
const t = (key: string) => getMessage(key, localeStore.language)

async function loadNotices() {
  loading.value = true
  try {
    const res = await requestPage<Record<string, any>>({
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

.message-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.message-item {
  display: grid;
  gap: 5px;
  padding: 12px 2px;
  border-bottom: 1px solid #ebeef5;
}

.message-item span {
  color: #909399;
  font-size: 12px;
}
</style>
