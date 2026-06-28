<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    :size="size"
    :class="drawerClass"
    v-bind="$attrs"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  size?: string
  variant?: 'form' | 'detail' | 'wide'
}>(), {
  title: '',
  size: undefined,
  variant: 'form'
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const drawerClass = computed(() => [
  'admin-drawer',
  `admin-drawer--${props.variant}`
])
</script>
