import { commonBaseInfoInstruction } from './common'
import { globalCapabilityInstruction } from './capabilityIndex'
import type { LocalizedInstructionText, PageAgentInstructionLocale, PageAgentPageInstruction } from './types'

interface PageInstructionRoute {
  key: string
  routes: string[]
  load: () => Promise<PageAgentPageInstruction>
}

const pageInstructionRoutes: PageInstructionRoute[] = [
  {
    key: 'productCategory',
    routes: ['/product-master/categories'],
    load: async () => (await import('./productCategory')).productCategoryInstruction
  },
  {
    key: 'unit',
    routes: ['/product-master/units'],
    load: async () => (await import('./unit')).unitInstruction
  },
  {
    key: 'materialType',
    routes: ['/product-master/material-types'],
    load: async () => (await import('./materialType')).materialTypeInstruction
  },
  {
    key: 'material',
    routes: ['/product-master/materials'],
    load: async () => (await import('./material')).materialInstruction
  }
]

const instructionCache = new Map<string, PageAgentPageInstruction>()
const loadingCache = new Map<string, Promise<PageAgentPageInstruction>>()

export async function preloadBaseInfoPageInstructions(url: string) {
  const entry = matchPageInstructionRoute(url)
  if (!entry) return undefined
  if (instructionCache.has(entry.key)) return instructionCache.get(entry.key)
  let loading = loadingCache.get(entry.key)
  if (!loading) {
    loading = entry.load().then((instruction) => {
      instructionCache.set(entry.key, instruction)
      loadingCache.delete(entry.key)
      return instruction
    })
    loadingCache.set(entry.key, loading)
  }
  return loading
}

export function getBaseInfoPageInstructions(url: string, locale: string = 'zh_CN') {
  const entry = matchPageInstructionRoute(url)
  if (!entry) return undefined
  const matched = instructionCache.get(entry.key)
  if (!matched) return undefined
  const instructionLocale = normalizeInstructionLocale(locale)
  return [
    localizedText(commonBaseInfoInstruction, instructionLocale),
    instructionLocale === 'en_US' ? `Page instruction pack: ${localizedText(matched.title, instructionLocale)}` : `页面知识包：${localizedText(matched.title, instructionLocale)}`,
    localizedText(matched.content, instructionLocale)
  ].join('\n\n')
}

export function normalizeInstructionLocale(locale: string): PageAgentInstructionLocale {
  return locale === 'en_US' ? 'en_US' : 'zh_CN'
}

export function getGlobalCapabilityInstructions(locale: string = 'zh_CN') {
  return localizedText(globalCapabilityInstruction, normalizeInstructionLocale(locale))
}

function matchPageInstructionRoute(url: string) {
  const pathname = safePathname(url)
  return pageInstructionRoutes.find((item) => item.routes.some((route) => pathname.startsWith(route)))
}

function localizedText(text: LocalizedInstructionText, locale: PageAgentInstructionLocale) {
  return text[locale] || text.zh_CN
}

function safePathname(url: string) {
  try {
    return new URL(url, window.location.origin).pathname
  } catch {
    return window.location.pathname
  }
}
