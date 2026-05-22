<template>
  <component :is="type" v-bind="linkProps()">
    <slot />
  </component>
</template>

<script setup lang="ts">
import { isExternal } from '@/utils/validate'

const props = defineProps<{
  to: string | Record<string, unknown>
}>()

const isExt = computed(() => {
  return typeof props.to === 'string' && isExternal(props.to)
})

const type = computed(() => {
  if (isExt.value) {
    return 'a'
  }
  return 'router-link'
})

function linkProps() {
  if (isExt.value) {
    return {
      href: props.to as string,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: props.to
  }
}
</script>
