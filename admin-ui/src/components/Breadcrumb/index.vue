<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index === levelList.length - 1" class="no-redirect">
          {{ routeTitle(item.meta.title) }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ routeTitle(item.meta.title) }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import type { RouteLocationMatched, RouteLocationRaw } from 'vue-router'
import { getMessage } from '@/locales'
import { resolveHomeRouteTarget } from '@/stores/homeRoute'
import useLocaleStore from '@/stores/locale'

type BreadcrumbRoute = Pick<RouteLocationMatched, 'path' | 'redirect' | 'name' | 'meta'>

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const levelList = ref<BreadcrumbRoute[]>([])

function getBreadcrumb() {
  let matched: BreadcrumbRoute[] = route.matched.filter((item) => item.meta && item.meta.title)
  const first = matched[0]
  if (!isHomeRoute(first)) {
    const homeTarget = resolveHomeRouteTarget()
    matched = [{ path: homeTarget.path, redirect: undefined, name: 'Index', meta: { title: homeTarget.title } } as BreadcrumbRoute].concat(matched)
  }

  levelList.value = matched.filter((item) => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}

function routeTitle(title: unknown) {
  if (typeof title !== 'string') return ''
  return title.includes('.') ? getMessage(title, localeStore.language) : title
}

function isHomeRoute(routeItem?: BreadcrumbRoute) {
  if (!routeItem) return false
  const homeTarget = resolveHomeRouteTarget()
  return routeItem.path === homeTarget.path || String(routeItem.name || '').trim() === 'Index'
}

function handleLink(item: BreadcrumbRoute) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect as RouteLocationRaw)
    return
  }
  router.push(path)
}

watchEffect(() => {
  if (route.path.startsWith('/redirect/')) return
  getBreadcrumb()
})
getBreadcrumb()
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 13px;
  line-height: 46px;
  margin-left: 8px;

  .no-redirect {
    color: #667085;
    cursor: text;
  }
}
</style>
