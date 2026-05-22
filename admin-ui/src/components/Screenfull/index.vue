<template>
  <button
    type="button"
    class="screenfull-button"
    :aria-label="label"
    :title="label"
    @click="toggle"
  >
    <svg-icon class-name="screenfull-svg" :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" />
  </button>
</template>

<script setup lang="ts">
import { useFullscreen } from '@vueuse/core'
import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'

const { isFullscreen, toggle } = useFullscreen();
const localeStore = useLocaleStore()
const label = computed(() => getMessage(isFullscreen.value ? 'shell.exitFullscreen' : 'shell.enterFullscreen', localeStore.language))
</script>

<style lang="scss" scoped>
.screenfull-button {
  width: 26px;
  height: 50px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
}

.screenfull-svg {
  display: inline-block;
  fill: #5a5e66;
  width: 20px;
  height: 20px;
  vertical-align: 10px;
}
</style>
