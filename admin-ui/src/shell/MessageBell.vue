<template>
  <el-popover v-model:visible="panelVisible" width="420" placement="bottom-end" trigger="click" popper-class="message-popover" @show="refreshPanel">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <button class="icon-button" type="button" :aria-label="t('message.title')" :title="t('message.title')">
          <el-icon><Bell /></el-icon>
        </button>
      </el-badge>
    </template>
    <section class="message-panel" v-loading="loading">
      <header class="message-head">
        <div>
          <strong>{{ t('message.notice') }}</strong>
          <span v-if="unreadCount > 0">{{ unreadCount }}</span>
        </div>
        <div class="message-head__actions">
          <el-button v-if="unreadCount > 0" link type="primary" @click="markAllRead">{{ t('message.markAllRead') }}</el-button>
          <el-button link type="primary" @click="openNoticeList">{{ t('message.viewAll') }}</el-button>
        </div>
      </header>
      <div v-if="!loading && notices.length === 0" class="message-empty">
        <el-icon><Bell /></el-icon>
        <strong>{{ t('message.empty') }}</strong>
      </div>
      <el-scrollbar v-else height="312px">
        <button
          v-for="notice in notices"
          :key="notice.noticeId"
          type="button"
          class="message-item"
          :class="{ 'is-unread': !notice.read }"
          @click="openNoticeDetail(notice)"
        >
          <span class="message-item__icon"><el-icon><Bell /></el-icon></span>
          <span class="message-item__body">
            <span class="message-item__title">
              <i v-if="!notice.read" />
              <strong>{{ notice.noticeTitle }}</strong>
            </span>
            <small>{{ formatUtc(notice.createTime) }}</small>
          </span>
          <span class="message-item__state">{{ notice.read ? t('message.read') : t('message.unread') }}</span>
        </button>
      </el-scrollbar>
    </section>
  </el-popover>

  <AdminDialog v-model="listOpen" :title="t('message.notice')" width="720px" variant="detail" class="message-dialog message-list-dialog">
    <div class="message-dialog-meta">
      <span>{{ total }} / {{ unreadCount }} {{ t('message.unread') }}</span>
      <span v-if="unreadCount > 0" class="message-dialog-meta__pill">{{ unreadCount }}</span>
    </div>
    <section v-loading="loading" class="message-list">
      <button
        v-for="notice in notices"
        :key="notice.noticeId"
        type="button"
        class="message-list__item"
        :class="{ 'is-unread': !notice.read }"
        @click="openNoticeDetail(notice)"
      >
        <span class="message-list__mark" />
        <span class="message-list__body">
          <strong>{{ notice.noticeTitle }}</strong>
          <small>{{ formatUtc(notice.createTime) }}</small>
        </span>
        <span class="message-list__status">{{ notice.read ? t('message.read') : t('message.unread') }}</span>
        <el-icon class="message-list__arrow"><ArrowRight /></el-icon>
      </button>
      <el-empty v-if="!loading && notices.length === 0" :description="t('message.empty')" />
    </section>
    <template #footer>
      <AdminDialogFooter>
        <el-button v-if="unreadCount > 0" type="primary" plain @click="markAllRead">{{ t('message.markAllRead') }}</el-button>
        <el-button @click="listOpen = false">{{ t('common.close') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>

  <AdminDialog v-model="detailOpen" :title="activeNotice.noticeTitle || t('message.notice')" width="720px" variant="detail" class="message-dialog message-detail-dialog">
    <article v-loading="detailLoading" class="message-detail">
      <p v-if="activeNotice.createTime" class="message-dialog-meta">{{ formatUtc(activeNotice.createTime) }}</p>
      <div class="message-detail__content" v-html="activeNotice.noticeContent || '-'" />
    </article>
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="detailOpen = false">{{ t('common.close') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ArrowRight, Bell } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import { getMyNotice, getMyUnreadNoticeCount, listMyNotices, markAllMyNoticesRead, type Notice } from '@/api/system/notice'

const localeStore = useLocaleStore()
const loading = ref(false)
const detailLoading = ref(false)
const panelVisible = ref(false)
const listOpen = ref(false)
const detailOpen = ref(false)
const notices = ref<Notice[]>([])
const activeNotice = ref<Notice>({})
const unreadCount = ref(0)
const total = ref(0)
const t = (key: string) => getMessage(key, localeStore.language)

onMounted(() => {
  refreshUnreadCount()
})

async function refreshUnreadCount() {
  unreadCount.value = await getMyUnreadNoticeCount()
}

async function loadNotices(pageSize = 6) {
  loading.value = true
  try {
    const res = await listMyNotices({ pageNum: 1, pageSize })
    notices.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function refreshPanel() {
  await Promise.all([loadNotices(6), refreshUnreadCount()])
}

async function openNoticeList() {
  panelVisible.value = false
  listOpen.value = true
  await Promise.all([loadNotices(20), refreshUnreadCount()])
}

async function openNoticeDetail(notice: Notice) {
  panelVisible.value = false
  activeNotice.value = notice
  detailOpen.value = true
  if (!notice.noticeId) return
  detailLoading.value = true
  try {
    activeNotice.value = await getMyNotice(notice.noticeId)
    notices.value = notices.value.map(item => item.noticeId === notice.noticeId ? { ...item, read: true, readTime: activeNotice.value.readTime } : item)
    await refreshUnreadCount()
  } finally {
    detailLoading.value = false
  }
}

async function markAllRead() {
  await markAllMyNoticesRead()
  unreadCount.value = 0
  notices.value = notices.value.map(item => ({ ...item, read: true }))
}
</script>

<style scoped lang="scss">
.icon-button {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  cursor: pointer;
  border: 0;
  border-radius: 12px;
  background: transparent;
  color: #4b5565;
  transition: background 0.18s ease, color 0.18s ease;
}

.icon-button:hover {
  background: #f4f7fb;
  color: #1f2937;
}

:deep(.el-badge__content.is-fixed) {
  top: 7px;
  right: 7px;
}

.message-panel {
  min-height: 230px;
  background: #ffffff;
}

.message-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 60px;
  padding: 0 18px;
  border-bottom: 1px solid #e8eef7;
  color: #001b5d;
}

.message-head > div:first-child {
  display: flex;
  gap: 8px;
  align-items: center;
}

.message-head span {
  min-width: 26px;
  padding: 4px 8px;
  border-radius: 999px;
  color: #075cff;
  font-size: 13px;
  font-weight: 800;
  text-align: center;
  background: #eaf2ff;
}

.message-head__actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.message-empty {
  display: grid;
  min-height: 170px;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #71839c;
  background: #fbfdff;
}

.message-empty .el-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  color: #9aaec7;
  border-radius: 14px;
  background: #edf4fb;
}

.message-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 72px;
  padding: 12px 18px;
  border: 0;
  border-bottom: 1px solid #edf2f8;
  color: #001b5d;
  text-align: left;
  background: #ffffff;
  cursor: pointer;
}

.message-item:hover {
  background: #f7fbff;
}

.message-item.is-unread {
  background: linear-gradient(90deg, #f7fbff 0%, #ffffff 58%);
}

.message-item__icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #075cff;
  border-radius: 14px;
  background: #eaf2ff;
}

.message-item__body {
  display: grid;
  min-width: 0;
  gap: 5px;
}

.message-item__title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.message-item__title i {
  width: 7px;
  height: 7px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: #ff4d4f;
}

.message-item__title strong,
.message-list__body strong {
  overflow: hidden;
  color: #001b5d;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-item small,
.message-list__body small {
  color: #667085;
  font-size: 13px;
}

.message-item__state,
.message-list__status {
  padding: 3px 9px;
  border-radius: 999px;
  color: #71839c;
  font-size: 12px;
  background: #f2f4f7;
}

.message-item.is-unread .message-item__state,
.message-list__item.is-unread .message-list__status {
  color: #075cff;
  background: #eaf2ff;
}

:global(.message-popover.el-popper) {
  border: 1px solid #dbe5f2;
  border-radius: 16px !important;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.14);
  overflow: hidden;
}

.message-dialog-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  color: #667085;
  font-size: 13px;
  line-height: 1.4;
}

.message-dialog-meta__pill {
  min-width: 24px;
  padding: 2px 8px;
  border-radius: 999px;
  color: #075cff;
  font-weight: 700;
  text-align: center;
  background: #eaf2ff;
}

.message-list {
  display: grid;
  max-height: 540px;
  overflow: auto;
  border: 1px solid #e6edf6;
  border-radius: 14px;
  background: #f8fbff;
}

.message-list__item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto 20px;
  align-items: center;
  gap: 14px;
  min-height: 64px;
  padding: 10px 14px;
  border: 0;
  border-bottom: 1px solid #eaf0f8;
  color: #001b5d;
  text-align: left;
  background: #ffffff;
  cursor: pointer;
}

.message-list__item:last-child {
  border-bottom: 0;
}

.message-list__item:hover {
  background: #f8fbff;
}

.message-list__mark {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #d0d5dd;
}

.message-list__item.is-unread .message-list__mark {
  background: #ff4d4f;
}

.message-list__body {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.message-list__arrow {
  color: #9aaec7;
}

.message-detail {
  min-height: 0;
}

.message-detail__content {
  max-height: 48vh;
  min-height: 72px;
  overflow: auto;
  padding: 16px 18px;
  border: 1px solid #e6edf6;
  border-radius: 10px;
  background: #fbfdff;
  color: #1f2937;
  font-size: 14px;
  line-height: 1.7;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

</style>
