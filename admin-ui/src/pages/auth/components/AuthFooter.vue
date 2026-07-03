<template>
  <footer class="auth-footer">
    <span>{{ t('apply.footerCopyright') }}</span>
    <i />
    <button type="button" @click="emit('openLegal', 'privacy')">{{ t('apply.privacy') }}</button>
    <i />
    <button type="button" @click="emit('openLegal', 'terms')">{{ t('apply.terms') }}</button>
    <i />
    <button type="button" @click="emit('openLegal', 'cookie')">{{ t('apply.cookiePolicy') }}</button>
  </footer>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { LoginLegalType } from '../types'

const { t } = useI18n()
const emit = defineEmits<{
  openLegal: [type: LoginLegalType]
}>()
</script>

<style scoped lang="scss">
.auth-footer {
  position: fixed;
  right: 0;
  bottom: 12px;
  left: 0;
  z-index: 4;
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: center;
  color: #7b8bad;
  font-size: 12px;
}

.auth-footer button {
  padding: 0;
  border: 0;
  color: #5d6f96;
  font: inherit;
  background: transparent;
  cursor: pointer;
}

.auth-footer i {
  width: 1px;
  height: 14px;
  background: #aebbd0;
}

@media (max-height: 960px) and (min-width: 1101px) {
  .auth-footer {
    bottom: 8px;
  }
}

@media (max-height: 820px) and (min-width: 1101px) {
  .auth-footer {
    bottom: 6px;
  }
}
</style>
