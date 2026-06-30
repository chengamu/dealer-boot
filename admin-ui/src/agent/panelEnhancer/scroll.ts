export function getPanelHistorySection() {
  return document.querySelector<HTMLElement>(
    '#page-agent-runtime_agent-panel [class*="historySection"]:not([class*="historySectionWrapper"])'
  )
}

export function scrollPanelToBottom(delay = 0) {
  window.setTimeout(() => {
    const history = getPanelHistorySection()
    if (!history) return
    history.scrollTop = history.scrollHeight
  }, delay)
}
