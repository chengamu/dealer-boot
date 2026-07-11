import type { CustomerQuoteItem } from '@/api/customer/quote'
import type { CustomerQuoteCatalogSetup } from '@/api/customer/quote'

export interface QuoteWorkbenchItem extends CustomerQuoteItem {
  clientId: string
}

export type QuoteSetupMap = Record<string, CustomerQuoteCatalogSetup | undefined>
