import type { LoginUser } from '@/types/api'

export type DashboardAccessMode = 'business' | 'platformSales' | 'redirectPayment' | 'redirectFulfillment'

export interface DashboardAccessPolicy {
  mode: DashboardAccessMode
  audience?: 'business' | 'platformSales'
}

const FINANCE_ROLES = new Set(['platform_finance'])
const FULFILLMENT_ROLES = new Set(['factory_production', 'factory_shipping'])
const PLATFORM_SALES_PERMISSION = 'platform:sales:dashboard:view'

export function resolveDashboardAccess(
  user?: LoginUser | null,
  roles: string[] = [],
  permissions: string[] = []
): DashboardAccessPolicy {
  if (!user || user.tenantType !== 'PLATFORM') {
    return { mode: 'business', audience: 'business' }
  }

  const roleSet = new Set<string>([...roles, ...(user.roles || [])].filter(Boolean))
  if (hasAny(roleSet, FINANCE_ROLES)) return { mode: 'redirectPayment' }
  if (hasAny(roleSet, FULFILLMENT_ROLES)) return { mode: 'redirectFulfillment' }
  if (permissions.includes(PLATFORM_SALES_PERMISSION)) return { mode: 'platformSales', audience: 'platformSales' }
  return { mode: 'business', audience: 'business' }
}

function hasAny(values: Set<string>, expected: Set<string>) {
  for (const value of values) {
    if (expected.has(value)) return true
  }
  return false
}
