<template>
  <el-dialog
    :model-value="modelValue"
    :width="width"
    :class="dialogClass"
    v-bind="$attrs"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header>
      <slot name="header">
        <div class="admin-dialog__header">
          <div>
            <p v-if="eyebrow" class="admin-dialog__eyebrow">{{ eyebrow }}</p>
            <h2 class="admin-dialog__title">{{ title }}</h2>
            <p v-if="subtitle" class="admin-dialog__subtitle">{{ subtitle }}</p>
          </div>
          <div v-if="$slots.actions" class="admin-dialog__actions">
            <slot name="actions" />
          </div>
        </div>
      </slot>
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
  subtitle?: string
  eyebrow?: string
  width?: string
  variant?: 'form' | 'detail' | 'picker'
}>(), {
  title: '',
  subtitle: '',
  eyebrow: '',
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
