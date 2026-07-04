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
  min-height: 240px;
  border: 1px solid #eef0f5;
  border-radius: 8px;
  background: #ffffff;
}

.form-build-page__icon {
  width: 36px;
  height: 36px;
}
</style>
