<template>
  <section v-loading="loading" class="external-frame" :style="{ height }">
    <iframe
      v-if="src"
      :src="src"
      :title="title"
      class="external-frame__iframe"
      loading="lazy"
      @load="loading = false"
    />
    <el-empty v-else :description="emptyText" />
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps<{
  src?: string
  title: string
  emptyText: string
}>()

const loading = ref(Boolean(props.src))
const viewportHeight = ref(document.documentElement.clientHeight)
const height = computed(() => `${Math.max(viewportHeight.value - 95, 320)}px`)

function resize() {
  viewportHeight.value = document.documentElement.clientHeight
}

onMounted(() => {
  window.addEventListener('resize', resize)
  window.setTimeout(() => {
    loading.value = false
  }, 800)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.external-frame {
  width: 100%;
  min-height: 320px;
}

.external-frame__iframe {
  width: 100%;
  height: 100%;
  border: 0;
}
</style>
