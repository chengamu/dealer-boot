<template>
  <div
    v-if="visible"
    class="ai-assistant"
    :class="{ 'is-dragging': floating.isDragging.value }"
    :style="floating.style.value"
    data-agent-label="智能助手"
    data-page-agent-ignore="true"
    @pointerdown="floating.onPointerDown"
  >
    <el-button
      class="ai-assistant__fab"
      circle
      :aria-label="t('aiAssistant.open')"
      :loading="loading"
      @click.prevent.stop
      @dragstart.prevent
      @keydown.enter.prevent="openOfficialAgent"
      @keydown.space.prevent="openOfficialAgent"
    >
      <img class="ai-assistant__robot" :src="robotImage" alt="" aria-hidden="true" draggable="false" />
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { openPageAgentPanel } from '@/agent/pageAgent'
import { getAiBootstrap } from '@/api/ai'
import { checkPermi } from '@/utils/permission'
import robotImage from '@/assets/ai-assistant/curtain-robot.png'
import { useFloatingDrag } from './useFloatingDrag'

const { t } = useI18n()
const loading = ref(false)
const visible = ref(false)
const floating = useFloatingDrag({
  storageKey: 'bocoo:ai-assistant-position',
  size: 44,
  onTap: openOfficialAgent
})

onMounted(async () => {
  if (!checkPermi(['ai:assistant:use'])) {
    visible.value = false
    return
  }
  try {
    const bootstrap = await getAiBootstrap()
    visible.value = bootstrap.enabled
  } catch {
    visible.value = false
  }
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
  top: 0;
  left: 0;
  z-index: 1200;
  width: 44px;
  height: 44px;
  will-change: transform;

  &.is-dragging {
    z-index: 3000;
  }

  &.is-dragging .ai-assistant__fab {
    cursor: grabbing;
    transform: scale(1.06);
  }
}

.ai-assistant__fab {
  width: 44px !important;
  height: 44px !important;
  padding: 0;
  overflow: visible;
  border: 0;
  background: transparent;
  box-shadow: none;
  cursor: grab;
  touch-action: none;
  user-select: none;
  transition:
    transform 0.12s ease,
    filter 0.12s ease;

  :deep(span) {
    display: block;
    width: 100%;
    height: 100%;
  }

  &:hover {
    background: transparent;
    transform: translateY(-1px);
  }

  &:focus,
  &:focus-visible {
    outline: none;
    box-shadow: none;
  }
}

.ai-assistant__robot {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 50%;
  filter: drop-shadow(0 14px 28px rgba(25, 93, 194, 0.24));
}
</style>
