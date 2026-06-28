<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :class="dialogClass"
    v-bind="$attrs"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  width?: string
  variant?: 'form' | 'detail' | 'picker'
}>(), {
  title: '',
  width: undefined,
  variant: 'form'
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const dialogClass = computed(() => [
  'admin-dialog',
  `admin-dialog--${props.variant}`
])
</script>
