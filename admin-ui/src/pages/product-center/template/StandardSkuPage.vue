<template>
  <div class="app-container product-center-page">
    <el-empty :description="t('productCenter.standardSku.placeholder')" />
  </div>
</template>

<script setup lang="ts" name="StandardSkuPage">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>
