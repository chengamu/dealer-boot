<template>
  <transition-group name="fade-transform">
    <inner-link
      v-for="(item, index) in tagsViewStore.iframeViews"
      v-show="route.path === item.path"
      :key="item.path"
      :iframeId="'iframe' + index"
      :src="iframeUrl(item.meta.link, item.query)"
    />
  </transition-group>
</template>

<script setup lang="ts">
import InnerLink from '../InnerLink/index.vue'
import useTagsViewStore from '@/stores/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

function iframeUrl(url?: unknown, query?: Record<string, unknown>) {
  const targetUrl = typeof url === 'string' ? url : ''
  const targetQuery = query || {}
  if (Object.keys(targetQuery).length > 0) {
    const params = Object.keys(targetQuery).map((key) => `${key}=${targetQuery[key]}`).join('&')
    return `${targetUrl}?${params}`
  }
  return targetUrl
}
</script>
