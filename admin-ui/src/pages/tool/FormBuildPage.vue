<template>
  <div class="form-build-page">
    <el-result icon="info" :title="t('toolBuild.title')" :sub-title="t('toolBuild.description')">
      <template #extra>
        <svg-icon icon-class="build" class="form-build-page__icon" />
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts" name="FormBuildPage">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style scoped>
.form-build-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

.form-build-page__icon {
  width: 42px;
  height: 42px;
}
</style>
