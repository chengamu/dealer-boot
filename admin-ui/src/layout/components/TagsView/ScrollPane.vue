<template>
  <el-scrollbar
    ref="scrollContainer"
    :vertical="false"
    class="scroll-container"
    @wheel.prevent="handleScroll"
  >
    <slot />
  </el-scrollbar>
</template>

<script setup lang="ts">
import useTagsViewStore, { type TagView } from '@/stores/tagsView'

type ScrollbarInstance = {
  $el: HTMLElement
  $refs: {
    wrapRef: HTMLElement
  }
}

type WheelEventWithDelta = WheelEvent & {
  wheelDelta?: number
}

const tagAndTagSpacing = ref(4);
const scrollContainer = ref<ScrollbarInstance | null>(null);

const scrollWrapper = computed(() => scrollContainer.value?.$refs.wrapRef || null);

onMounted(() => {
  scrollWrapper.value?.addEventListener('scroll', emitScroll, true)
})
onBeforeUnmount(() => {
  scrollWrapper.value?.removeEventListener('scroll', emitScroll)
})

function handleScroll(e: WheelEventWithDelta) {
  const eventDelta = e.wheelDelta || -e.deltaY * 40
  const $scrollWrapper = scrollWrapper.value;
  if (!$scrollWrapper) return
  $scrollWrapper.scrollLeft = $scrollWrapper.scrollLeft + eventDelta / 4
}
const emits = defineEmits<{
  (event: 'scroll'): void
}>()
const emitScroll = () => {
  emits('scroll')
}

const tagsViewStore = useTagsViewStore()
const visitedViews = computed(() => tagsViewStore.visitedViews);

function moveToTarget(currentTag: TagView) {
  const $container = scrollContainer.value?.$el
  if (!$container) return
  const $containerWidth = $container.offsetWidth
  const $scrollWrapper = scrollWrapper.value;
  if (!$scrollWrapper) return

  let firstTag: TagView | null = null
  let lastTag: TagView | null = null

  // find first tag and last tag
  if (visitedViews.value.length > 0) {
    firstTag = visitedViews.value[0]
    lastTag = visitedViews.value[visitedViews.value.length - 1]
  }

  if (firstTag === currentTag) {
    $scrollWrapper.scrollLeft = 0
  } else if (lastTag === currentTag) {
    $scrollWrapper.scrollLeft = $scrollWrapper.scrollWidth - $containerWidth
  } else {
    const tagListDom = Array.from(document.getElementsByClassName('tags-view-item')) as HTMLElement[];
    const currentIndex = visitedViews.value.findIndex(item => item === currentTag)
    const prevPath = visitedViews.value[currentIndex - 1]?.path
    const nextPath = visitedViews.value[currentIndex + 1]?.path
    const prevTag = tagListDom.find((tag) => tag.dataset.path === prevPath)
    const nextTag = tagListDom.find((tag) => tag.dataset.path === nextPath)
    if (!prevTag || !nextTag) return

    // the tag's offsetLeft after of nextTag
    const afterNextTagOffsetLeft = nextTag.offsetLeft + nextTag.offsetWidth + tagAndTagSpacing.value

    // the tag's offsetLeft before of prevTag
    const beforePrevTagOffsetLeft = prevTag.offsetLeft - tagAndTagSpacing.value
    if (afterNextTagOffsetLeft > $scrollWrapper.scrollLeft + $containerWidth) {
      $scrollWrapper.scrollLeft = afterNextTagOffsetLeft - $containerWidth
    } else if (beforePrevTagOffsetLeft < $scrollWrapper.scrollLeft) {
      $scrollWrapper.scrollLeft = beforePrevTagOffsetLeft
    }
  }
}

defineExpose({
  moveToTarget,
})
</script>

<style lang='scss' scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow-x: auto;
  overflow-y: hidden;
  width: 100%;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
  :deep(.el-scrollbar__bar) {
    display: none;
  }
  :deep(.el-scrollbar__wrap) {
    height: 38px;
    overflow-y: hidden;
    scrollbar-width: none;
    &::-webkit-scrollbar {
      display: none;
    }
  }
  :deep(.el-scrollbar__view) {
    height: 38px;
  }
}
</style>
