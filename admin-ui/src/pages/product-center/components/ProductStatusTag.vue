<template>
  <el-tag :type="tagType" effect="light" round>
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const props = defineProps<{
  value?: string | number | boolean
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const normalized = computed(() => String(props.value ?? ''))
const label = computed(() => {
  if (normalized.value === '1') return t('productCenter.status.enabled')
  if (normalized.value === '0') return t('productCenter.status.disabled')
  if (normalized.value === 'PUBLISHED') return t('productCenter.status.published')
  if (normalized.value === 'DRAFT') return t('productCenter.status.draft')
  return normalized.value || t('common.none')
})
const tagType = computed(() => {
  if (['1', 'PUBLISHED', 'PASS'].includes(normalized.value)) return 'success'
  if (['0', 'BLOCKER'].includes(normalized.value)) return 'danger'
  if (['DRAFT', 'WARNING'].includes(normalized.value)) return 'warning'
  return 'info'
})
</script>

