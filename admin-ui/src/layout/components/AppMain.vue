<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
<!--      <transition name="fade-transform" mode="out-in">-->
        <keep-alive :include="tagsViewStore.cachedViews">
          <component v-if="!route.meta.link" :is="Component" :key="route.path"/>
        </keep-alive>
<!--      </transition>-->
    </router-view>
    <iframe-toggle />
  </section>
</template>

<script setup lang="ts">
import IframeToggle from "./IframeToggle/index.vue"
import useTagsViewStore from '@/stores/tagsView'

const tagsViewStore = useTagsViewStore()
</script>

<style lang="scss" scoped>
.app-main {
  /* 60 = navbar */
  min-height: calc(100vh - 60px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.fixed-header + .app-main {
  padding-top: 60px;
}

.hasTagsView {
  .app-main {
    /* 96 = navbar + tags-view = 60 + 36 */
    min-height: calc(100vh - 96px);
  }

  .fixed-header + .app-main {
    padding-top: 96px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}
</style>
