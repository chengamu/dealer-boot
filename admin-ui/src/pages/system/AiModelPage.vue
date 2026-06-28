<template>
  <div class="app-container">
    <el-card shadow="never" class="provider-picker">
      <template #header>
        <div class="panel-header">
          <span>{{ t('ai.settings.provider') }}</span>
        </div>
      </template>
      <el-select v-model="providerCode" :placeholder="t('ai.settings.selectProviderFirst')" filterable @change="handleProviderChange">
        <el-option
          v-for="provider in providers"
          :key="provider.providerCode"
          :label="`${provider.providerName || provider.providerCode} / ${provider.providerCode}`"
          :value="provider.providerCode || ''"
        />
      </el-select>
    </el-card>
    <AiProviderModelPanel class="mt16" :provider="selectedProvider" />
  </div>
</template>

<script setup lang="ts" name="AiModelPage">
import { computed, onMounted, ref } from 'vue'
import AiProviderModelPanel from './ai-settings/AiProviderModelPanel.vue'
import { listAiProviders, type AiProviderConfig } from '@/api/ai-admin'
import { useAiSettingsI18n } from './ai-settings/useAiSettingsI18n'

const t = useAiSettingsI18n()
const providers = ref<AiProviderConfig[]>([])
const providerCode = ref('')
const selectedProvider = computed(() => providers.value.find((item) => item.providerCode === providerCode.value))

function handleProviderChange(value: string) {
  providerCode.value = value
}

onMounted(async () => {
  providers.value = await listAiProviders()
  providerCode.value = providers.value.find((item) => item.enabled)?.providerCode || providers.value[0]?.providerCode || ''
})
</script>

<style scoped>
.provider-picker {
  max-width: 720px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mt16 {
  margin-top: 16px;
}
</style>
