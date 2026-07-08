<template>
  <el-button v-if="visible" class="formula-order-option-help" link type="primary" @click="openHelp">
    {{ t('productCenter.formulaSimulation.needHelp') }}
  </el-button>

  <AdminDrawer v-model="drawerOpen" :title="title" size="640px" variant="detail" append-to-body>
    <div class="formula-order-option-help__content">{{ plainContent }}</div>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ProductFormulaOptionVO } from '@/api/product-capability/types'

const props = defineProps<{
  option: ProductFormulaOptionVO
  label: string
  t: (key: string, params?: Record<string, string | number>) => string
}>()

const drawerOpen = ref(false)
const title = computed(() => props.option.helpTitle || props.label || props.t('productCenter.formulaSimulation.needHelp'))
const helpMode = computed(() => helpDisplayMode(props.option.helpType, props.option.helpUrl, props.option.helpContent))
const safeUrl = computed(() => normalizeHelpUrl(props.option.helpUrl))
const plainContent = computed(() => stripHtml(String(props.option.helpContent || '')).trim())
const visible = computed(() => Boolean(props.option.helpEnabled && (
  (helpMode.value === 'LINK' && safeUrl.value)
  || (helpMode.value === 'TEXT' && plainContent.value)
  || safeUrl.value
  || plainContent.value
)))

function openHelp() {
  if (helpMode.value === 'TEXT' && plainContent.value) {
    drawerOpen.value = true
    return
  }
  if (safeUrl.value) {
    window.open(safeUrl.value, '_blank', 'noopener,noreferrer')
    return
  }
  drawerOpen.value = true
}

function helpDisplayMode(type?: string, url?: string, content?: string) {
  if (type === 'TEXT' || type === 'RICH_TEXT') return 'TEXT'
  if (!type && stripHtml(String(content || '')).trim() && !String(url || '').trim()) return 'TEXT'
  return 'LINK'
}

function normalizeHelpUrl(value?: string) {
  const url = String(value || '').trim()
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/') && !url.startsWith('//')) return url
  return ''
}

function stripHtml(value: string) {
  return value.replace(/<[^>]*>/g, '')
}
</script>

<style scoped>
.formula-order-option-help {
  height: auto;
  padding: 0;
  font-size: 12px;
}

.formula-order-option-help__content {
  color: #1f2937;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
