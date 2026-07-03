<template>
  <el-drawer
    :model-value="modelValue"
    :size="size"
    :class="drawerClass"
    v-bind="$attrs"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <slot name="header">
        <div class="admin-drawer__header">
          <div>
            <p v-if="eyebrow" class="admin-drawer__eyebrow">{{ eyebrow }}</p>
            <h2 class="admin-drawer__title">{{ title }}</h2>
            <p v-if="subtitle" class="admin-drawer__subtitle">{{ subtitle }}</p>
          </div>
          <div v-if="$slots.actions" class="admin-drawer__actions">
            <slot name="actions" />
          </div>
        </div>
      </slot>
    </template>
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
  subtitle?: string
  eyebrow?: string
  size?: string
  variant?: 'form' | 'detail' | 'wide'
}>(), {
  title: '',
  subtitle: '',
  eyebrow: '',
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
