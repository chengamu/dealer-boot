<template>
  <div>
    <el-dropdown trigger="click" @command="handleSetSize">
      <button
        type="button"
        class="size-icon--style"
        :aria-label="t('shell.layoutSize')"
        :title="t('shell.layoutSize')"
      >
        <svg-icon class-name="size-icon" icon-class="size" />
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="item of sizeOptions" :key="item.value" :disabled="size === item.value" :command="item.value">
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import useAppStore from '@/stores/app'
import useLocaleStore from '@/stores/locale'

type AppSize = 'large' | 'default' | 'small'

const appStore = useAppStore()
const localeStore = useLocaleStore()
const size = computed(() => appStore.size)
const t = (key: string) => getMessage(key, localeStore.language)
const { proxy } = getCurrentInstance() as unknown as { proxy: { $modal: { loading: (message: string) => void } } }
const sizeOptions = computed<Array<{ label: string; value: AppSize }>>(() => [
  { label: t('shell.sizeLarge'), value: 'large' },
  { label: t('shell.sizeDefault'), value: 'default' },
  { label: t('shell.sizeSmall'), value: 'small' }
])

function handleSetSize(size: AppSize) {
  proxy.$modal.loading(t('shell.settingSize'))
  appStore.setSize(size)
  window.setTimeout(() => window.location.reload(), 1000)
}
</script>

<style lang="scss" scoped>
.size-icon--style {
  width: 26px;
  height: 50px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 18px;
  line-height: 50px;
}
</style>
