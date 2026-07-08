<template>
  <div class="option-help-editor">
    <span class="option-help-editor__label">{{ t('productCenter.formulaSetup.helpEnabled') }}</span>
    <div class="option-help-editor__controls">
      <el-switch :model-value="option.helpEnabled" @update:model-value="updateEnabled" />
      <el-button size="small" :disabled="!option.helpEnabled" @click="openDialog">
        {{ t('productCenter.formulaSetup.helpSetting') }}
      </el-button>
      <span v-if="option.helpEnabled" class="option-help-editor__summary">{{ summary }}</span>
    </div>
  </div>

  <el-dialog v-model="dialogOpen" :title="t('productCenter.formulaSetup.helpSetting')" width="520px" append-to-body>
    <div class="option-help-dialog">
      <label>
        <span>{{ t('productCenter.formulaSetup.helpMode') }}</span>
        <el-radio-group v-model="draftMode">
          <el-radio-button label="LINK">{{ t('productCenter.formulaSetup.helpModeLink') }}</el-radio-button>
          <el-radio-button label="TEXT">{{ t('productCenter.formulaSetup.helpModeText') }}</el-radio-button>
        </el-radio-group>
      </label>
      <label>
        <span>{{ t('productCenter.formulaSetup.helpTitle') }}</span>
        <el-input v-model="draftTitle" :placeholder="t('productCenter.formulaSetup.helpTitlePlaceholder')" />
      </label>
      <label v-if="draftMode === 'LINK'">
        <span>{{ t('productCenter.formulaSetup.helpUrl') }}</span>
        <el-input v-model="draftUrl" :placeholder="t('productCenter.formulaSetup.helpUrlPlaceholder')" />
      </label>
      <label v-else>
        <span>{{ t('productCenter.formulaSetup.helpContent') }}</span>
        <el-input
          v-model="draftContent"
          type="textarea"
          :rows="5"
          :placeholder="t('productCenter.formulaSetup.helpContentPlaceholder')"
        />
      </label>
    </div>
    <template #footer>
      <el-button @click="dialogOpen = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirmDialog">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ProductFormulaOptionVO } from '@/api/product-capability/types'

const props = defineProps<{
  option: ProductFormulaOptionVO
  t: (key: string, params?: Record<string, string | number>) => string
}>()

const dialogOpen = ref(false)
const draftMode = ref<'LINK' | 'TEXT'>('LINK')
const draftTitle = ref('')
const draftUrl = ref('')
const draftContent = ref('')
const currentMode = computed(() => helpMode(props.option))
const summary = computed(() => {
  const configured = currentMode.value === 'LINK'
    ? Boolean(String(props.option.helpUrl || '').trim())
    : Boolean(plainText(props.option.helpContent).trim())
  if (!configured) return props.t('productCenter.formulaSetup.helpNotConfigured')
  return currentMode.value === 'LINK'
    ? props.t('productCenter.formulaSetup.helpModeLink')
    : props.t('productCenter.formulaSetup.helpModeText')
})

function updateEnabled(value: string | number | boolean) {
  props.option.helpEnabled = Boolean(value)
  if (props.option.helpEnabled && !props.option.helpType) {
    props.option.helpType = 'LINK'
  }
}

function openDialog() {
  draftMode.value = currentMode.value
  draftTitle.value = props.option.helpTitle || ''
  draftUrl.value = props.option.helpUrl || ''
  draftContent.value = plainText(props.option.helpContent)
  dialogOpen.value = true
}

function confirmDialog() {
  props.option.helpType = draftMode.value
  props.option.helpTitle = draftTitle.value.trim()
  props.option.helpUrl = draftMode.value === 'LINK' ? draftUrl.value.trim() : ''
  props.option.helpContent = draftMode.value === 'TEXT' ? plainText(draftContent.value).trim() : ''
  dialogOpen.value = false
}

function helpMode(option: ProductFormulaOptionVO): 'LINK' | 'TEXT' {
  if (option.helpType === 'TEXT' || option.helpType === 'RICH_TEXT') return 'TEXT'
  if (!option.helpType && plainText(option.helpContent).trim() && !String(option.helpUrl || '').trim()) return 'TEXT'
  return 'LINK'
}

function plainText(value?: string) {
  return String(value || '').replace(/<[^>]*>/g, '')
}
</script>

<style scoped>
.option-help-editor {
  display: grid;
  width: 220px;
  min-width: 0;
  gap: 6px;
}

.option-help-editor__label {
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.option-help-editor__controls {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  height: 32px;
}

.option-help-editor__controls :deep(.el-button) {
  height: 32px;
  padding: 0 10px;
}

.option-help-editor__summary {
  min-width: 0;
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-help-dialog {
  display: grid;
  gap: 14px;
}

.option-help-dialog label {
  display: grid;
  gap: 6px;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}
</style>
