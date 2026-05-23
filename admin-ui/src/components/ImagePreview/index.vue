<template>
  <el-image
    :src="`${realSrc}`"
    fit="cover"
    :style="`width:${realWidth};height:${realHeight};`"
    :preview-src-list="realSrcList"
    preview-teleported
  >
    <template #error>
      <div class="image-slot">
        <el-icon><picture-filled /></el-icon>
      </div>
    </template>
  </el-image>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  src?: string
  width?: number | string
  height?: number | string
}>(), {
  src: '',
  width: '',
  height: ''
});

const realSrc = computed(() => {
  if (!props.src) {
    return '';
  }
  const real_src = props.src.split(",")[0];
  return real_src;
});

const realSrcList = computed(() => {
  if (!props.src) {
    return [];
  }
  const real_src_list = props.src.split(",");
  const srcList: string[] = [];
  real_src_list.forEach((item) => srcList.push(item));
  return srcList;
});

const realWidth = computed(() =>
  typeof props.width == "string" ? props.width : `${props.width}px`
);

const realHeight = computed(() =>
  typeof props.height == "string" ? props.height : `${props.height}px`
);
</script>

<style lang="scss" scoped>
.el-image {
  border: 1px solid var(--admin-border-soft, #eef2f7);
  border-radius: 10px;
  background-color: #f7faff;
  box-shadow: 0 6px 16px rgba(16, 24, 40, 0.05);
  overflow: hidden;

  :deep(.el-image__inner) {
    transition: transform 0.2s ease;
    cursor: pointer;

    &:hover {
      transform: scale(1.04);
    }
  }

  :deep(.image-slot) {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    color: #909399;
    font-size: 30px;
  }
}
</style>
