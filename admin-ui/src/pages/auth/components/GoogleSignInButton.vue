<template>
  <template v-if="googleClientId">
    <div class="login-divider"><span>{{ t('login.or') }}</span></div>
    <div ref="googleButtonRef" class="google-button-host" :class="{ 'is-loading': googleLoading }" />
  </template>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useGoogleSignIn } from '../composables/useGoogleSignIn'

const props = defineProps<{
  refreshKey?: string
}>()

const { t } = useI18n()
const { googleButtonRef, googleClientId, googleLoading, renderGoogleButton } = useGoogleSignIn()

watch(() => props.refreshKey, () => {
  void renderGoogleButton()
})

onMounted(() => {
  void renderGoogleButton()
})
</script>

<style scoped lang="scss">
.login-divider {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 18px;
  align-items: center;
  margin: 24px 0;
  color: #6d7da1;
}

.login-divider::before,
.login-divider::after {
  height: 1px;
  content: "";
  background: #d7e0ef;
}

.google-button-host {
  position: relative;
  display: grid;
  width: 100%;
  height: 52px;
  align-items: center;
  justify-content: center;
}

.google-button-host.is-loading {
  opacity: 0.72;
  pointer-events: none;
}

@media (max-width: 1280px) {
  .login-divider {
    margin: 22px 0;
  }

  .google-button-host {
    height: 48px;
  }
}

@media (max-height: 960px) and (min-width: 1101px) {
  .google-button-host {
    height: 46px;
  }

  .login-divider {
    margin: 16px 0;
  }
}
</style>
