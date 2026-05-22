import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import RouteView from '@/components/RouteView.vue'
import InnerLink from '@/layout/components/InnerLink/index.vue'
import { getRouters } from '@/api/auth'
import type { RouterVo } from '@/types/api'

const migratedViewModules: Record<string, () => Promise<unknown>> = {
  'monitor/online/index': () => import('@/pages/monitor/OnlineUsersPage.vue'),
  'monitor/logininfor/index': () => import('@/pages/monitor/LoginInfoPage.vue'),
  'monitor/operlog/index': () => import('@/pages/monitor/OperationLogPage.vue'),
  'monitor/cache/index': () => import('@/pages/monitor/CacheMonitorPage.vue'),
  'monitor/cache/list': () => import('@/pages/monitor/CacheListPage.vue'),
  'monitor/admin/index': () => import('@/pages/monitor/AdminMonitorPage.vue'),
  'monitor/xxljob/index': () => import('@/pages/monitor/XxlJobPage.vue'),
  'system/user/index': () => import('@/pages/system/user/UserPage.vue'),
  'system/user/authRole': () => import('@/pages/system/user/UserAuthRolePage.vue'),
  'system/user/profile': () => import('@/pages/system/user/UserProfilePage.vue'),
  'system/post/index': () => import('@/pages/system/PostPage.vue'),
  'system/notice/index': () => import('@/pages/system/NoticePage.vue'),
  'system/config/index': () => import('@/pages/system/ConfigPage.vue'),
  'system/dept/index': () => import('@/pages/system/DeptPage.vue'),
  'system/menu/index': () => import('@/pages/system/MenuPage.vue'),
  'system/role/index': () => import('@/pages/system/RolePage.vue'),
  'system/role/authUser': () => import('@/pages/system/RoleAuthUserPage.vue'),
  'system/dict/index': () => import('@/pages/system/DictTypePage.vue'),
  'system/dict/data': () => import('@/pages/system/DictDataPage.vue'),
  'system/oss/index': () => import('@/pages/system/OssPage.vue'),
  'system/oss/config': () => import('@/pages/system/OssConfigPage.vue'),
  'system/tenant/applications': () => import('@/pages/system/TenantApplicationsPlaceholder.vue'),
  'tool/gen/index': () => import('@/pages/tool/gen/GenPage.vue'),
  'tool/gen/editTable': () => import('@/pages/tool/gen/GenEditPage.vue'),
  'tool/build/index': () => import('@/pages/tool/FormBuildPage.vue')
}

function normalizePath(path: string) {
  return path.replace(/\/+/g, '/')
}

function joinPath(parent: string, child: string) {
  if (child.startsWith('/')) return normalizePath(child)
  return normalizePath(`${parent}/${child}`)
}

function loadView(component?: string) {
  if (!component || component === 'Layout') return Layout
  if (component === 'ParentView') return RouteView
  if (component === 'InnerLink') return InnerLink
  return migratedViewModules[component] || (() => import('@/pages/error/Error404Page.vue'))
}

function convertRoute(route: RouterVo, parentPath = ''): RouteRecordRaw {
  const fullPath = parentPath ? joinPath(parentPath, route.path) : route.path || '/'
  const converted = {
    path: route.component === 'Layout' ? fullPath : route.path,
    name: route.name || fullPath.replace(/[^\w]/g, '_'),
    component: loadView(route.component),
    redirect: route.redirect,
    meta: {
      title: route.meta?.title,
      icon: route.meta?.icon,
      noCache: route.meta?.noCache,
      link: route.meta?.link
    }
  } as RouteRecordRaw
  const children = route.children?.map((child) => convertRoute(child, fullPath))
  if (children?.length) converted.children = children
  if (!converted.redirect) delete converted.redirect
  return converted
}

function convertMenus(routes: RouterVo[]) {
  return routes.filter((route) => !route.hidden)
}

function flattenLayoutChildren(routes: RouterVo[]) {
  const children: RouteRecordRaw[] = []
  routes.forEach((route) => {
    if (route.component === 'Layout') {
      route.children?.forEach((child) => {
        const childRoute = convertRoute(child, route.path)
        childRoute.path = joinPath(route.path, child.path)
        children.push(childRoute)
      })
      return
    }
    children.push(convertRoute(route))
  })
  return children
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routers: [] as RouterVo[],
    routeRecords: [] as RouteRecordRaw[],
    sidebarRouteOverride: null as RouterVo[] | null,
    loaded: false
  }),
  getters: {
    routes: (state) => state.routeRecords,
    addRoutes: (state) => state.routeRecords,
    defaultRoutes: (state) => state.routeRecords,
    topbarRouters: (state) => state.routers,
    sidebarRouters: (state) => state.sidebarRouteOverride || convertMenus(state.routers)
  },
  actions: {
    setSidebarRouters(routes: RouterVo[] | null) {
      this.sidebarRouteOverride = routes
    },
    async generateRoutes() {
      const routers = await getRouters()
      this.routers = routers
      this.routeRecords = flattenLayoutChildren(routers)
      this.sidebarRouteOverride = null
      this.loaded = true
      return this.routeRecords
    },
    reset() {
      this.routers = []
      this.routeRecords = []
      this.sidebarRouteOverride = null
      this.loaded = false
    }
  }
})

export default usePermissionStore
