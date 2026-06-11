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

  <el-dialog v-model="listOpen" width="720px" class="message-dialog message-list-dialog">
    <template #header>
      <div class="message-dialog-head">
        <span class="message-dialog-head__icon"><el-icon><Bell /></el-icon></span>
        <span class="message-dialog-head__text">
          <strong>{{ t('message.notice') }}</strong>
          <small>{{ total }} / {{ unreadCount }} {{ t('message.unread') }}</small>
        </span>
        <span v-if="unreadCount > 0" class="message-dialog-head__pill">{{ unreadCount }}</span>
      </div>
    </template>
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
      <div class="dialog-footer">
        <el-button v-if="unreadCount > 0" type="primary" plain @click="markAllRead">{{ t('message.markAllRead') }}</el-button>
        <el-button @click="listOpen = false">{{ t('common.close') }}</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="detailOpen" width="720px" class="message-dialog message-detail-dialog">
    <template #header>
      <div class="message-dialog-head">
        <span class="message-dialog-head__icon is-detail"><el-icon><Bell /></el-icon></span>
        <span class="message-dialog-head__text">
          <strong>{{ activeNotice.noticeTitle || t('message.notice') }}</strong>
          <small v-if="activeNotice.createTime">{{ formatUtc(activeNotice.createTime) }}</small>
        </span>
      </div>
    </template>
    <article v-loading="detailLoading" class="message-detail">
      <div class="message-detail__content" v-html="activeNotice.noticeContent || '-'" />
    </article>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="detailOpen = false">{{ t('common.close') }}</el-button>
      </div>
    </template>
  </el-dialog>
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
.message-list__body small,
.message-dialog-head small {
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

.message-dialog-head {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.message-dialog-head__icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  color: #075cff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: linear-gradient(180deg, #eff6ff 0%, #eaf2ff 100%);
}

.message-dialog-head__icon.is-detail {
  color: #ffffff;
  border-color: #075cff;
  background: linear-gradient(180deg, #1677ff 0%, #075cff 100%);
  box-shadow: 0 8px 18px rgba(7, 92, 255, 0.2);
}

.message-dialog-head__text {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.message-dialog-head strong {
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-dialog-head__pill {
  min-width: 32px;
  padding: 5px 10px;
  border-radius: 999px;
  color: #075cff;
  font-size: 13px;
  font-weight: 800;
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
  min-height: 200px;
  border-top: 1px solid #e6edf6;
}

.message-detail__content {
  max-height: 58vh;
  overflow: auto;
  padding: 22px 2px 4px;
  color: #1f2937;
  font-size: 15px;
  line-height: 1.75;
}

:global(.message-detail-dialog .el-dialog__header),
:global(.message-list-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 22px 56px 18px 32px;
  background: #ffffff !important;
}

:global(.message-detail-dialog.el-dialog),
:global(.message-list-dialog.el-dialog) {
  width: min(720px, calc(100vw - 40px));
  border-color: #dfe8f4;
  border-radius: 18px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.2);
}

:global(.message-detail-dialog .el-dialog__body),
:global(.message-list-dialog .el-dialog__body) {
  padding: 0 32px 24px;
}

:global(.message-detail-dialog .el-dialog__footer),
:global(.message-list-dialog .el-dialog__footer) {
  padding: 16px 32px 24px;
  border-top: 1px solid #e6edf6;
}

:global(.message-detail-dialog .el-dialog__headerbtn),
:global(.message-list-dialog .el-dialog__headerbtn) {
  top: 20px;
  right: 26px;
}
</style>
