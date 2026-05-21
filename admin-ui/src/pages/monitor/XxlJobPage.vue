<template>
  <ExternalFrame :src="url" :title="t('external.xxlJobTitle')" :empty-text="t('external.missingUrl')" />
</template>

<script setup lang="ts" name="XxlJobPage">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ExternalFrame from '@/components/ExternalFrame.vue'

const { t } = useI18n()
const url = computed(() => window.__APP_CONFIG__?.VITE_APP_XXL_JOB_ADMIN || import.meta.env.VITE_APP_XXL_JOB_ADMIN || '/xxl-job-admin')
</script>
