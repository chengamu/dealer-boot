<template>
  <div />
</template>

<script setup lang="ts" name="RedirectPage">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const path = Array.isArray(route.params.path) ? route.params.path.join('/') : String(route.params.path || '')

router.replace({ path: `/${path}`, query: route.query })
</script>
