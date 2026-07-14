import type { RouteLocationNormalizedLoaded } from 'vue-router'

export type PayPageMode =
  | 'business-orders'
  | 'business-credit'
  | 'business-receivable'
  | 'platform-payments'
  | 'platform-bank'
  | 'platform-credit'
  | 'platform-receivable'
  | 'platform-reconciliation'

function routeHints(route: RouteLocationNormalizedLoaded) {
  return [
    route.path,
    route.name,
    route.meta?.title,
    route.meta?.i18nKey
  ]
    .filter(Boolean)
    .join('|')
    .toLowerCase()
}

function includesAny(text: string, keywords: string[]) {
  return keywords.some((keyword) => text.includes(keyword))
}

export function resolvePayPageMode(route: RouteLocationNormalizedLoaded, permissions: string[]) {
  const hints = routeHints(route)
  const business = includesAny(hints, ['finance.business', 'business', 'dealer', 'merchant'])
  if (business && includesAny(hints, ['receivable'])) return 'business-receivable'
  if (business && includesAny(hints, ['credit'])) return 'business-credit'
  if (includesAny(hints, ['reconciliation', 'reconcile'])) return 'platform-reconciliation'
  if (includesAny(hints, ['receivable'])) return 'platform-receivable'
  if (includesAny(hints, ['credit'])) return 'platform-credit'
  if (includesAny(hints, ['bank', 'transfer'])) return 'platform-bank'
  if (includesAny(hints, ['business', 'dealer', 'merchant'])) return 'business-orders'
  if (permissions.some((item) => item.startsWith('platform:finance:payment:'))) return 'platform-payments'
  return 'business-orders'
}
