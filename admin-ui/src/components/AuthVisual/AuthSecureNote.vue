<template>
  <div class="auth-secure-note">
    <span><el-icon><CircleCheckFilled /></el-icon></span>
    <div>
      <strong>{{ title }}</strong>
      <small>{{ description }}</small>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CircleCheckFilled } from '@element-plus/icons-vue'

defineProps<{
  title: string
  description: string
}>()
</script>

<style scoped lang="scss">
.auth-secure-note {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 14px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 8px;
  background: #f3f8ff;
}

.auth-secure-note span {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  color: #075cff;
  border-radius: 50%;
  background: #e6f0ff;
}

.auth-secure-note small {
  display: block;
  margin-top: 4px;
  color: #66789c;
}
</style>
