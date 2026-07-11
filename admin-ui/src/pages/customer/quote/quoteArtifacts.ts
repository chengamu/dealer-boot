import { customerQuoteApi, type CustomerQuote } from '@/api/customer/quote'

export async function openQuotePdf(quote: CustomerQuote, print = false) {
  if (!quote.quoteId) return
  const blob = await customerQuoteApi.pdf(quote.quoteId)
  const url = URL.createObjectURL(blob)
  const target = window.open(url, '_blank', 'noopener')
  if (print && target) target.addEventListener('load', () => target.print(), { once: true })
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
