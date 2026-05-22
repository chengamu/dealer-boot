<template>
  <div class="app-container tenant-placeholder">
    <el-empty :description="t('tenant.applicationsDisabled')">
      <template #image>
        <el-icon class="tenant-placeholder__icon" aria-hidden="true"><DocumentChecked /></el-icon>
      </template>
      <el-button type="primary" @click="router.push('/index')">{{ t('common.back') }}</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { DocumentChecked } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
</script>

<style scoped>
.tenant-placeholder {
  display: grid;
  min-height: 420px;
  place-items: center;
}

.tenant-placeholder__icon {
  color: #8c8c8c;
  font-size: 72px;
}
</style>
