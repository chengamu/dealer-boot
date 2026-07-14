<template>
  <div
    ref="zoneRef"
    class="upload-drop-zone"
    :class="{ 'is-dragging': dragging }"
    tabindex="0"
    @click="focus"
    @paste="handlePaste"
    @dragenter.prevent="handleDragEnter"
    @dragover.prevent
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <el-icon v-if="showIcon" class="upload-drop-zone__icon"><UploadFilled /></el-icon>
    <div v-if="label" class="upload-drop-zone__label">{{ label }}</div>
    <slot />
    <div v-if="hint" class="upload-drop-zone__hint">{{ hint }}</div>
    <slot name="tip" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { getClipboardFiles, getDroppedFiles } from '@/composables/uploadIntake'

withDefaults(defineProps<{
  label?: string
  hint?: string
  showIcon?: boolean
}>(), {
  label: '',
  hint: '',
  showIcon: true
})

const emit = defineEmits<{
  files: [files: File[]]
}>()
const zoneRef = ref<HTMLElement>()
const dragging = ref(false)
let dragDepth = 0

function focus() {
  zoneRef.value?.focus()
}

function handlePaste(event: ClipboardEvent) {
  const files = getClipboardFiles(event)
  if (!files.length) return
  event.preventDefault()
  emit('files', files)
}

function handleDragEnter() {
  dragDepth++
  dragging.value = true
}

function handleDragLeave() {
  dragDepth = Math.max(0, dragDepth - 1)
  dragging.value = dragDepth > 0
}

function handleDrop(event: DragEvent) {
  dragDepth = 0
  dragging.value = false
  const files = getDroppedFiles(event)
  if (files.length) emit('files', files)
}
</script>

<style scoped>
.upload-drop-zone {
  padding: 24px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  text-align: center;
  transition: border-color 0.2s, background-color 0.2s, box-shadow 0.2s;
}

.upload-drop-zone:focus,
.upload-drop-zone.is-dragging {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
  outline: none;
}

.upload-drop-zone__icon {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 42px;
}

.upload-drop-zone__label {
  margin-bottom: 10px;
  color: var(--el-text-color-regular);
}

.upload-drop-zone__hint {
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
