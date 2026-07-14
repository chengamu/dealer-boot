import NProgress from 'nprogress'
import { ElMessage } from 'element-plus'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { i18n } from '@/i18n'
import { usePermissionStore } from '@/stores/permission'
import { HOME_FALLBACK_PATH, resolveHomeRouteTarget } from '@/stores/homeRoute'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { getContextPath } from '@/utils/config'
import { notFoundRoute, routes } from './staticRoutes'

export const constantRoutes = routes
export const dynamicRoutes: RouteRecordRaw[] = []

const router = createRouter({
  history: createWebHistory(getContextPath()),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

let dynamicRoutesLoaded = false
const dynamicRouteNames = new Set<string>()
let notFoundRouteRegistered = false

function removeNotFoundRoute() {
  if (notFoundRouteRegistered && router.hasRoute('NotFound')) router.removeRoute('NotFound')
  notFoundRouteRegistered = false
}

function ensureNotFoundRoute() {
  if (!notFoundRouteRegistered && !router.hasRoute('NotFound')) router.addRoute(notFoundRoute)
  notFoundRouteRegistered = true
}

export function resetDynamicRoutes() {
  removeNotFoundRoute()
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) router.removeRoute(name)
  })
  dynamicRouteNames.clear()
  dynamicRoutesLoaded = false
}

export function registerDynamicRoutes(routesToRegister: RouteRecordRaw[]) {
  removeNotFoundRoute()
  routesToRegister.forEach((route) => {
    const exists = router.getRoutes().some((registered) => registered.path === route.path)
    if (!exists) {
      router.addRoute('Root', route)
      if (route.name && typeof route.name === 'string') dynamicRouteNames.add(route.name)
    }
  })
  ensureNotFoundRoute()
  dynamicRoutesLoaded = true
}

async function ensureUserContext() {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  if (!userStore.user) await userStore.loadUser()
  if (!dynamicRoutesLoaded) {
    const accessRoutes = await permissionStore.generateRoutes()
    registerDynamicRoutes(accessRoutes)
    return false
  }
  return true
}

function resolveHomePath() {
  return resolveHomeRouteTarget().path || HOME_FALLBACK_PATH
}

router.beforeEach(async (to) => {
  NProgress.start()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const token = getToken()

  if (to.path === '/login' && token) {
    try {
      const ready = await ensureUserContext()
      if (userStore.forcePasswordChange) {
        return { path: '/user/profile', query: { forcePasswordChange: '1' }, replace: true }
      }
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : resolveHomePath()
      return ready ? { path: redirect, replace: true } : { ...to, replace: true }
    } catch (error) {
      await userStore.logout()
      permissionStore.reset()
      resetDynamicRoutes()
      ElMessage.error(error instanceof Error ? error.message : i18n.global.t('error.loadUserProfileFailed'))
      return '/login'
    }
  }

  if (to.meta.public) return true
  if (!token) return `/login?redirect=${encodeURIComponent(to.fullPath)}`

  try {
    const ready = await ensureUserContext()
    if (userStore.forcePasswordChange && to.path !== '/user/profile') {
      return { path: '/user/profile', query: { forcePasswordChange: '1' }, replace: true }
    }
    if (!ready) return { ...to, replace: true }
    if (to.path === HOME_FALLBACK_PATH) {
      const homePath = resolveHomePath()
      if (homePath !== HOME_FALLBACK_PATH) return { path: homePath, replace: true }
    }
  } catch (error) {
    await userStore.logout()
    permissionStore.reset()
    resetDynamicRoutes()
    ElMessage.error(error instanceof Error ? error.message : i18n.global.t('error.loadUserProfileFailed'))
    return '/login'
  }

  return true
})

router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())

export default router
