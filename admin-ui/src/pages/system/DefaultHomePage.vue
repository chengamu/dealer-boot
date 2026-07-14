<template>
  <div class="app-container default-home-page">
    <el-result icon="info" :title="t('shell.homeNeutralTitle')" :sub-title="t('shell.homeNeutralDescription')" />
  </div>
</template>

<script setup lang="ts" name="DefaultHomePage">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.default-home-page {
  display: grid;
  min-height: calc(100vh - 96px);
  place-items: center;
}
</style>
