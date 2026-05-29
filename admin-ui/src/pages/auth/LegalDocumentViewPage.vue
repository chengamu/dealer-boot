<template>
  <main class="legal-page">
    <section class="legal-shell">
      <header class="legal-header">
        <button type="button" class="legal-back" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>{{ t('common.back') }}</span>
        </button>
        <el-button link type="primary" @click="loadDocument">
          <el-icon><Refresh /></el-icon>
          {{ t('common.refresh') }}
        </el-button>
      </header>

      <article class="legal-document" v-loading="loading">
        <p class="legal-type">{{ formatType(documentType) }}</p>
        <h1>{{ title }}</h1>
        <p v-if="publishedTime || version" class="legal-meta">
          <span v-if="version">{{ t('legal.version') }}: {{ version }}</span>
          <span v-if="publishedTime">{{ t('legal.publishedTime') }}: {{ formatUtc(publishedTime) }}</span>
        </p>
        <div class="legal-content">{{ content }}</div>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { getPublishedLegalDocument } from '@/api/system/legalDocument'
import { formatUtc } from '@/utils/datetime'

type LegalType = 'privacy' | 'terms' | 'cookie'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const loading = ref(false)
const remoteTitle = ref('')
const content = ref('')
const version = ref('')
const publishedTime = ref('')

const documentType = computed<LegalType>(() => {
  const type = String(route.params.type || '')
  return type === 'terms' || type === 'cookie' ? type : 'privacy'
})
const title = computed(() => remoteTitle.value || formatType(documentType.value))

function formatType(type: LegalType) {
  const keys: Record<LegalType, string> = {
    privacy: 'legal.typePrivacy',
    terms: 'legal.typeTerms',
    cookie: 'legal.typeCookie'
  }
  return t(keys[type])
}

async function loadDocument() {
  loading.value = true
  remoteTitle.value = ''
  content.value = t(`apply.legalPlaceholder.${documentType.value}`)
  version.value = ''
  publishedTime.value = ''
  try {
    const document = await getPublishedLegalDocument(documentType.value)
    remoteTitle.value = document.title || ''
    content.value = document.content || content.value
    version.value = document.version || ''
    publishedTime.value = document.publishedTime || ''
  } catch {
    // Keep the local fallback visible; the request interceptor displays backend errors.
  } finally {
    loading.value = false
  }
}

watch(documentType, loadDocument, { immediate: true })
</script>

<style scoped lang="scss">
.legal-page {
  min-height: 100vh;
  padding: 34px 18px;
  background: #eef5fb;
}

.legal-shell {
  width: min(980px, 100%);
  margin: 0 auto;
}

.legal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.legal-back {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  height: 34px;
  padding: 0;
  border: 0;
  color: #315078;
  font-weight: 700;
  background: transparent;
  cursor: pointer;
}

.legal-document {
  min-height: 520px;
  padding: 42px 52px;
  border: 1px solid #dbe5ef;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 54px rgba(28, 54, 86, 0.12);
}

.legal-type {
  margin: 0 0 10px;
  color: #075cff;
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #071a3d;
  font-size: 32px;
  line-height: 1.25;
}

.legal-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 22px;
  margin: 14px 0 28px;
  color: #66758d;
  font-size: 13px;
}

.legal-content {
  white-space: pre-wrap;
  color: #24344d;
  font-size: 15px;
  line-height: 1.78;
}

@media (max-width: 680px) {
  .legal-page {
    padding: 18px 10px;
  }

  .legal-document {
    min-height: 420px;
    padding: 28px 22px;
  }

  h1 {
    font-size: 25px;
  }
}
</style>
