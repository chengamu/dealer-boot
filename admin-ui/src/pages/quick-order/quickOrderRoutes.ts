import type { RouterVo } from '@/types/api'

export const quickOrderRouteComponents = {
  workbench: 'dealer-quick-order/workbench',
  list: 'dealer-quick-order/list',
  review: 'dealer-quick-order/review',
  quote: 'customer/quotes'
} as const

const routeFallbacks: Record<string, string> = {
  [quickOrderRouteComponents.workbench]: '/sales/quickOrders',
  [quickOrderRouteComponents.list]: '/sales/quickOrders',
  [quickOrderRouteComponents.review]: '/sales/quickOrders',
  [quickOrderRouteComponents.quote]: '/sales/quotes'
}

export function resolveRoutePath(routes: RouterVo[], componentKey: string, fallback?: string) {
  const matched = findRoute(routes, componentKey)
  return matched || fallback || routeFallbacks[componentKey] || '/'
}

function findRoute(routes: RouterVo[], componentKey: string, parentPath = ''): string | undefined {
  for (const route of routes) {
    const fullPath = parentPath ? joinPath(parentPath, route.path) : route.path
    if (route.component === componentKey) return fullPath
    const nested = route.children?.length ? findRoute(route.children, componentKey, fullPath) : undefined
    if (nested) return nested
  }
  return undefined
}

function joinPath(parent: string, child?: string) {
  return `${parent}/${child || ''}`.replace(/\/+/g, '/')
}
