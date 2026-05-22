type OverlayDiagnosticSource = 'snapshot' | 'mutation' | 'cleanup' | 'event'

const STORAGE_KEY = 'debug:overlays'
const OVERLAY_SELECTOR = '.el-overlay, .v-modal, .el-loading-mask'
const MAX_LOGS = 50
const BLOCKER_LIMIT = 20
const DEBUG_BUTTON_ID = 'overlay-debug-copy-button'
const IS_DEV = import.meta.env.DEV

declare global {
  interface Window {
    __overlayDebugLogs?: unknown[]
    copyOverlayDebugLogs?: () => string
  }
}

function isEnabled() {
  return IS_DEV && localStorage.getItem(STORAGE_KEY) === '1'
}

function getElementSummary(element: Element) {
  const computed = window.getComputedStyle(element)
  const rect = element.getBoundingClientRect()
  return {
    tag: element.tagName.toLowerCase(),
    id: element.id || undefined,
    className: String(element.className || '') || undefined,
    zIndex: computed.zIndex,
    display: computed.display,
    visibility: computed.visibility,
    pointerEvents: computed.pointerEvents,
    position: computed.position,
    opacity: computed.opacity,
    filter: computed.filter,
    backdropFilter: computed.backdropFilter,
    transform: computed.transform,
    mixBlendMode: computed.mixBlendMode,
    overflow: computed.overflow,
    backgroundColor: computed.backgroundColor,
    rect: {
      x: Math.round(rect.x),
      y: Math.round(rect.y),
      width: Math.round(rect.width),
      height: Math.round(rect.height)
    }
  }
}

function isPotentialBlocker(element: Element) {
  const computed = window.getComputedStyle(element)
  if (computed.display === 'none' || computed.visibility === 'hidden' || computed.pointerEvents === 'none') {
    return false
  }

  const rect = element.getBoundingClientRect()
  const coversMostViewport = rect.width >= window.innerWidth * 0.75 && rect.height >= window.innerHeight * 0.75
  const usesOverlayPosition = computed.position === 'fixed' || computed.position === 'absolute' || computed.position === 'sticky'

  return coversMostViewport && usesOverlayPosition
}

function getOverlayState(source: OverlayDiagnosticSource, detail?: unknown) {
  const centerX = window.innerWidth / 2
  const centerY = window.innerHeight / 2
  const centerElement = document.elementFromPoint(centerX, centerY)
  return {
    source,
    url: window.location.href,
    viewport: {
      width: window.innerWidth,
      height: window.innerHeight
    },
    htmlClass: document.documentElement.className.trim(),
    htmlStyle: document.documentElement.getAttribute('style') || '',
    bodyClass: document.body.className.trim(),
    bodyStyle: document.body.getAttribute('style') || '',
    activeElement: document.activeElement ? getElementSummary(document.activeElement) : null,
    elementAtCenter: centerElement
      ? getElementSummary(centerElement)
      : null,
    elementsAtCenter: document.elementsFromPoint(centerX, centerY).slice(0, 12).map(getElementSummary),
    centerAncestorChain: centerElement ? getAncestorChain(centerElement) : [],
    activeAncestorChain: document.activeElement ? getAncestorChain(document.activeElement) : [],
    overlays: Array.from(document.querySelectorAll(OVERLAY_SELECTOR)).map(getElementSummary),
    potentialBlockers: [document.documentElement, document.body, ...Array.from(document.documentElement.querySelectorAll('*'))]
      .filter(isPotentialBlocker)
      .slice(0, BLOCKER_LIMIT)
      .map(getElementSummary),
    detail
  }
}

function getElementsAtPoint(x: number, y: number) {
  return document.elementsFromPoint(x, y).slice(0, 12).map(getElementSummary)
}

function getAncestorChain(element: Element | null) {
  const chain: ReturnType<typeof getElementSummary>[] = []
  let current = element
  while (current && chain.length < 16) {
    chain.push(getElementSummary(current))
    current = current.parentElement
  }
  return chain
}

function copyLogsText() {
  return JSON.stringify(window.__overlayDebugLogs || [], null, 2)
}

function createDebugCopyButton() {
  const existing = document.getElementById(DEBUG_BUTTON_ID)
  if (existing) return existing

  const button = document.createElement('button')
  button.id = DEBUG_BUTTON_ID
  button.type = 'button'
  button.textContent = 'Copy overlay logs'
  button.style.cssText = [
    'position:fixed',
    'right:16px',
    'bottom:16px',
    'z-index:2147483647',
    'padding:8px 12px',
    'border:1px solid #1d4ed8',
    'border-radius:6px',
    'background:#fff',
    'color:#1d4ed8',
    'font:12px/1.2 Arial,sans-serif',
    'cursor:pointer',
    'box-shadow:0 6px 18px rgba(15,23,42,.18)'
  ].join(';')
  button.addEventListener('click', async () => {
    const text = copyLogsText()
    try {
      await navigator.clipboard.writeText(text)
      button.textContent = 'Copied'
    } catch {
      console.log(`[overlay-debug-copy]\n${text}`)
      button.textContent = 'Printed to console'
    }
    window.setTimeout(() => {
      button.textContent = 'Copy overlay logs'
    }, 1600)
  })
  document.body.appendChild(button)
  return button
}

export function logOverlayState(source: OverlayDiagnosticSource, detail?: unknown) {
  if (!isEnabled()) return
  const state = getOverlayState(source, detail)
  window.__overlayDebugLogs = [...(window.__overlayDebugLogs || []), state].slice(-MAX_LOGS)
  console.groupCollapsed(`[overlay-debug] ${source}`)
  console.log(state)
  console.log(`[overlay-debug-json] ${JSON.stringify(state)}`)
  console.groupEnd()
}

export function startOverlayDiagnostics() {
  if (!IS_DEV) return () => undefined

  const params = new URLSearchParams(window.location.search)
  if (params.get('overlayDebug') === '1') {
    localStorage.setItem(STORAGE_KEY, '1')
  }

  if (!isEnabled()) return () => undefined

  window.__overlayDebugLogs = []
  window.copyOverlayDebugLogs = copyLogsText
  const debugCopyButton = createDebugCopyButton()
  logOverlayState('snapshot', 'start')
  const snapshotTimer = window.setInterval(() => logOverlayState('snapshot', 'interval'), 1000)
  window.setTimeout(() => window.clearInterval(snapshotTimer), 10000)

  const eventOptions = { capture: true }
  const handlePointerEvent = (event: MouseEvent) => {
    logOverlayState('event', {
      type: event.type,
      target: event.target instanceof Element ? getElementSummary(event.target) : String(event.target),
      x: event.clientX,
      y: event.clientY,
      elementsAtPoint: getElementsAtPoint(event.clientX, event.clientY)
    })
  }
  const handleFocusEvent = (event: FocusEvent) => {
    logOverlayState('event', {
      type: event.type,
      target: event.target instanceof Element ? getElementSummary(event.target) : String(event.target)
    })
  }
  const handleKeyboardEvent = (event: KeyboardEvent) => {
    logOverlayState('event', {
      type: event.type,
      key: event.key,
      target: event.target instanceof Element ? getElementSummary(event.target) : String(event.target),
      activeElement: document.activeElement ? getElementSummary(document.activeElement) : null
    })
  }

  document.addEventListener('pointerdown', handlePointerEvent, eventOptions)
  document.addEventListener('click', handlePointerEvent, eventOptions)
  document.addEventListener('focusin', handleFocusEvent, eventOptions)
  document.addEventListener('keydown', handleKeyboardEvent, eventOptions)
  const observer = new MutationObserver((mutations) => {
    const changedNodes = mutations.flatMap((mutation) => [
      ...Array.from(mutation.addedNodes),
      ...Array.from(mutation.removedNodes)
    ])
    const overlayChanges = changedNodes
      .filter((node): node is Element => node.nodeType === Node.ELEMENT_NODE)
      .filter((node) => node.matches(OVERLAY_SELECTOR) || Boolean(node.querySelector(OVERLAY_SELECTOR)))

    if (overlayChanges.length > 0) {
      logOverlayState('mutation', overlayChanges.map(getElementSummary))
    }
  })

  observer.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['class', 'style'] })

  return () => {
    logOverlayState('snapshot', 'stop')
    window.clearInterval(snapshotTimer)
    debugCopyButton.remove()
    document.removeEventListener('pointerdown', handlePointerEvent, eventOptions)
    document.removeEventListener('click', handlePointerEvent, eventOptions)
    document.removeEventListener('focusin', handleFocusEvent, eventOptions)
    document.removeEventListener('keydown', handleKeyboardEvent, eventOptions)
    observer.disconnect()
  }
}

export function getOverlaySelector() {
  return OVERLAY_SELECTOR
}
