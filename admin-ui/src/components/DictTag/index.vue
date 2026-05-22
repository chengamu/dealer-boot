<template>
  <div>
    <template v-for="(item, index) in options" :key="`${item.value}-${index}`">
      <template v-if="values.includes(String(item.value))">
        <span
          v-if="(item.elTagType === 'default' || item.elTagType === '') && (item.elTagClass === '' || item.elTagClass == null)"
          :class="item.elTagClass"
        >{{ item.label + ' ' }}</span>
        <el-tag
          v-else
          :disable-transitions="true"
          :type="tagType(item.elTagType)"
          :class="item.elTagClass"
        >{{ item.label + ' ' }}</el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ handleArray(unmatchArray) }}
    </template>
  </div>
</template>

<script setup lang="ts">
type DictOption = {
  label: string
  value: string | number
  elTagType?: string
  elTagClass?: string | null
}

const props = withDefaults(defineProps<{
  options?: DictOption[]
  value?: number | string | Array<number | string>
  showValue?: boolean
  separator?: string
}>(), {
  options: () => [],
  showValue: true,
  separator: ','
})

const unmatchArray = ref<string[]>([])

const values = computed(() => {
  if (props.value === null || typeof props.value === 'undefined' || props.value === '') return []
  return Array.isArray(props.value) ? props.value.map((item) => String(item)) : String(props.value).split(props.separator)
})

const unmatch = computed(() => {
  unmatchArray.value = []
  if (props.value === null || typeof props.value === 'undefined' || props.value === '' || props.options.length === 0) return false
  let hasUnmatch = false
  values.value.forEach((item) => {
    if (!props.options.some((option) => String(option.value) === item)) {
      unmatchArray.value.push(item)
      hasUnmatch = true
    }
  })
  return hasUnmatch
})

function handleArray(array: string[]) {
  return array.length === 0 ? '' : array.join(' ')
}

function tagType(type?: string) {
  if (type === 'primary' || type === 'default' || type === '') return undefined
  return ['success', 'info', 'warning', 'danger'].includes(type || '') ? type : undefined
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
