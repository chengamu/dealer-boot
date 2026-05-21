<template>
  <div class="icon-body">
    <el-input v-model="iconName" class="icon-search" clearable :placeholder="placeholder" @clear="filterIcons" @input="filterIcons" />
    <div class="icon-list">
      <div class="list-container">
        <button v-for="item in iconList" :key="item" class="icon-item-wrapper" type="button" @click="selectedIcon(item)">
          <div :class="['icon-item', { active: activeIcon === item }]">
            <svg-icon :icon-class="item" class-name="icon" style="height: 25px; width: 16px" />
            <span>{{ item }}</span>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import icons from './requireIcons'

withDefaults(
  defineProps<{
    activeIcon?: string
    placeholder?: string
  }>(),
  {
    placeholder: 'Search icon'
  }
)

const iconName = ref('')
const iconList = ref<string[]>(icons)
const emit = defineEmits<{
  selected: [name: string]
}>()

function filterIcons() {
  iconList.value = iconName.value ? icons.filter((item: string) => item.includes(iconName.value)) : icons
}

function selectedIcon(name: string) {
  emit('selected', name)
  document.body.click()
}

function reset() {
  iconName.value = ''
  iconList.value = icons
}

defineExpose({
  reset
})
</script>

<style lang="scss" scoped>
.icon-body {
  width: 100%;
  padding: 10px;

  .icon-search {
    position: relative;
    margin-bottom: 5px;
  }

  .icon-list {
    height: 200px;
    overflow: auto;

    .list-container {
      display: flex;
      flex-wrap: wrap;

      .icon-item-wrapper {
        display: flex;
        width: calc(100% / 3);
        height: 25px;
        padding: 0;
        border: 0;
        line-height: 25px;
        cursor: pointer;
        background: transparent;

        .icon-item {
          display: flex;
          max-width: 100%;
          height: 100%;
          padding: 0 5px;

          &:hover,
          &.active {
            background: #ececec;
            border-radius: 5px;
          }

          .icon {
            flex-shrink: 0;
          }

          span {
            display: inline-block;
            padding-left: 2px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            vertical-align: -0.15em;
            fill: currentColor;
          }
        }
      }
    }
  }
}
</style>
