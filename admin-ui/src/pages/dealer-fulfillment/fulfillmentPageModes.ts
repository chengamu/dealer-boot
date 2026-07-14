import type { RouteLocationNormalizedLoaded } from 'vue-router'
import type { FulfillmentAudience, FulfillmentGridKind } from '@/api/dealer-fulfillment'

export type FulfillmentPageMode = {
  audience: FulfillmentAudience
  kind: FulfillmentGridKind
}

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

export function resolveFulfillmentPageMode(route: RouteLocationNormalizedLoaded, permissions: string[]): FulfillmentPageMode {
  const hints = routeHints(route)
  const audience: FulfillmentAudience = includesAny(hints, ['factory'])
    ? 'factory'
    : includesAny(hints, ['platform', 'admin'])
      ? 'platform'
      : includesAny(hints, ['progress', 'business'])
        ? 'business'
        : permissions.some((item) => item.startsWith('dealer:fulfillment:factory:'))
          ? 'factory'
          : permissions.some((item) => item.startsWith('dealer:fulfillment:admin:'))
            ? 'platform'
            : 'business'
  const kind: FulfillmentGridKind = includesAny(hints, ['package'])
    ? 'package'
    : includesAny(hints, ['tracking', 'track'])
      ? 'tracking'
      : includesAny(hints, ['shipment', 'shipping'])
        ? 'shipment'
        : 'production'
  return { audience, kind }
}
