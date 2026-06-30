import { sanitizeTextNodes } from './sanitize'
import { scrollPanelToBottom } from './scroll'
import { enhanceSimulatorMask } from './simulator'
import { installPanelEnhancerStyle } from './style'
import { enhanceTypewriter } from './typewriter'
export { bindPanelActivity } from './activity'

function hideDebugButtons(root: HTMLElement) {
  root
    .querySelectorAll<HTMLElement>(
      'button[title="复制测速日志"], button[title="清空测速日志"], button[title="Copy perf logs"], button[title="Clear perf logs"]'
    )
    .forEach((button) => {
      button.style.display = 'none'
    })
}

function enhancePanel(root: HTMLElement) {
  hideDebugButtons(root)
  sanitizeTextNodes(root)
  enhanceTypewriter(root)
  enhanceSimulatorMask()
  scrollPanelToBottom()
}

export function installPageAgentPanelEnhancer() {
  const root = document.getElementById('page-agent-runtime_agent-panel')
  if (!root || root.dataset.bocooPanelEnhancerInstalled === 'true') return

  root.dataset.bocooPanelEnhancerInstalled = 'true'
  installPanelEnhancerStyle()
  enhancePanel(root)
  const observer = new MutationObserver(() => enhancePanel(root))
  observer.observe(root, {
    childList: true,
    subtree: true,
    characterData: true
  })
}
