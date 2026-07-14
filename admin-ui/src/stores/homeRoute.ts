import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import type { RouterVo } from '@/types/api'

export const HOME_FALLBACK_PATH = '/index'

export interface HomeRouteTarget {
  path: string
  title: string
  source: 'default' | 'first-menu' | 'neutral'
}

function normalizePath(path?: string) {
  if (!path) return ''
  const normalized = path.startsWith('/') ? path : `/${path}`
  return normalized.replace(/\/+/g, '/')
}

function joinPath(parent: string, child: string) {
  if (!child) return normalizePath(parent)
  if (child.startsWith('/')) return normalizePath(child)
  return normalizePath(`${parent}/${child}`)
}

function isViewMenu(route: RouterVo) {
  if (route.hidden) return false
  if (!route.component) return false
  if (route.component === 'Layout' || route.component === 'ParentView' || route.component === 'InnerLink') return false
  if (!route.meta?.title) return false
  return true
}

function findFirstMenu(routes: RouterVo[], parentPath = ''): HomeRouteTarget | null {
  for (const route of routes) {
    const fullPath = parentPath ? joinPath(parentPath, route.path) : normalizePath(route.path || '/')
    if (isViewMenu(route)) {
      return {
        path: fullPath,
        title: route.meta?.title || 'dashboard.home',
        source: 'first-menu'
      }
    }
    const childTarget = route.children?.length ? findFirstMenu(route.children, fullPath) : null
    if (childTarget) return childTarget
  }
  return null
}

function fallbackTitle() {
  const localeStore = useLocaleStore()
  return getMessage('dashboard.home', localeStore.language)
}

export function resolveHomeRouteTarget(): HomeRouteTarget {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const defaultRoute = normalizePath(userStore.defaultRoute)
  if (defaultRoute) {
    return {
      path: defaultRoute,
      title: userStore.defaultRouteTitle || fallbackTitle(),
      source: 'default'
    }
  }
  const firstMenu = findFirstMenu(permissionStore.routers)
  if (firstMenu) return firstMenu
  return {
    path: HOME_FALLBACK_PATH,
    title: fallbackTitle(),
    source: 'neutral'
  }
}

export function isHomeRoutePath(path?: string) {
  return normalizePath(path) === normalizePath(resolveHomeRouteTarget().path)
}
