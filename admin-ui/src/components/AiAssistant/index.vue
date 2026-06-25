<template>
  <div v-if="visible" class="ai-assistant" data-agent-label="基础信息 AI 助手">
    <el-button
      class="ai-assistant__fab"
      circle
      type="primary"
      :aria-label="t('aiAssistant.open')"
      :loading="loading"
      @click="openOfficialAgent"
    >
      <el-icon><ChatDotRound /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { openPageAgentPanel } from '@/agent/pageAgent'
import useUserStore from '@/stores/user'

const route = useRoute()
const { t } = useI18n()
const userStore = useUserStore()
const loading = ref(false)

const enabledRoutes = [
  { path: '/product-master/categories', permission: 'product:base:list' },
  { path: '/product-master/base-attributes', permission: 'product:base:list' },
  { path: '/product-master/material-attributes', permission: 'product:material-attribute:list' },
  { path: '/product-master/materials', permission: 'product:base:list' },
  { path: '/product-master/units', permission: 'product:unit:list' },
  { path: '/product-master/product-dicts', permission: 'product:dict:list' },
  { path: '/product-master/material-types', permission: 'product:material-type:list' },
  { path: '/product-master/media-assets', permission: 'product:asset:list' },
  { path: '/product-master/media-bindings', permission: 'product:asset:list' }
]

const visible = computed(() => {
  const matched = enabledRoutes.find((item) => route.path.startsWith(item.path))
  if (!matched) return false
  return userStore.permissions.some((permission) => permission === '*:*:*' || permission === matched.permission)
})

async function openOfficialAgent() {
  if (loading.value) return
  loading.value = true
  try {
    await openPageAgentPanel()
  } catch (error) {
    const key = error instanceof Error ? error.message : 'aiAssistant.failed'
    ElMessage.error(t(key === 'pageAgent.disabled' || key === 'pageAgent.missingConfig' ? key : 'aiAssistant.failed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1200;
}

.ai-assistant__fab {
  width: 48px;
  height: 48px;
  box-shadow: 0 10px 28px rgba(25, 93, 194, 0.28);
}
</style>
