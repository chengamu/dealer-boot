import { sanitizePanelText } from './sanitize'
import { getPanelHistorySection, scrollPanelToBottom } from './scroll'
import { enhanceSimulatorMask, revealSimulatorCursor } from './simulator'

type AgentLike = EventTarget & {
  readonly status?: string
}

type AgentActivity =
  | { type: 'thinking' }
  | { type: 'executing'; tool: string; input: unknown }
  | { type: 'executed'; tool: string; input: unknown; output: string; duration: number }
  | { type: 'retrying'; attempt: number; maxAttempts: number }
  | { type: 'error'; message: string }

const installedAgents = new WeakSet<EventTarget>()
let typingTimer: number | null = null

function getHistorySection() {
  return getPanelHistorySection()
}

function ensureLiveCard() {
  const history = getHistorySection()
  if (!history) return null

  let card = history.querySelector<HTMLElement>('[data-bocoo-live-activity="true"]')
  if (card) return card

  card = document.createElement('div')
  card.className = 'bocoo-agent-live-card'
  card.dataset.bocooLiveActivity = 'true'
  card.innerHTML = `
    <span class="bocoo-agent-live-icon"></span>
    <span class="bocoo-agent-live-text"></span>
  `
  history.appendChild(card)
  scrollPanelToBottom()
  return card
}

function setLiveText(text: string) {
  const card = ensureLiveCard()
  const target = card?.querySelector<HTMLElement>('.bocoo-agent-live-text')
  if (!card || !target) return

  const cleanText = sanitizePanelText(text)
  if (typingTimer) {
    window.clearInterval(typingTimer)
    typingTimer = null
  }

  card.classList.add('is-typing')
  target.textContent = ''
  let index = 0
  typingTimer = window.setInterval(() => {
    index += 1
    target.textContent = cleanText.slice(0, index)
    scrollPanelToBottom()
    if (index >= cleanText.length) {
      if (typingTimer) window.clearInterval(typingTimer)
      typingTimer = null
      card.classList.remove('is-typing')
    }
  }, 16)
}

function removeLiveCard() {
  if (typingTimer) {
    window.clearInterval(typingTimer)
    typingTimer = null
  }
  window.setTimeout(() => {
    document.querySelector('[data-bocoo-live-activity="true"]')?.remove()
  }, 500)
}

function setRunningClass(running: boolean) {
  document.getElementById('page-agent-runtime_agent-panel')?.classList.toggle('bocoo-agent-running', running)
}

function activityText(activity: AgentActivity) {
  switch (activity.type) {
    case 'thinking':
      return '正在理解页面和你的需求...'
    case 'executing':
      return toolText(activity.tool)
    case 'executed':
      return sanitizePanelText(activity.output || '页面操作已完成，正在继续检查结果...')
    case 'retrying':
      return `刚才没有成功，正在自动重试 ${activity.attempt}/${activity.maxAttempts}...`
    case 'error':
      return sanitizePanelText(activity.message || '执行遇到问题，请稍后重试。')
    default:
      return '正在处理...'
  }
}

function toolText(tool: string) {
  if (/click/i.test(tool)) return '正在点击页面控件...'
  if (/input|type|fill/i.test(tool)) return '正在填写页面字段...'
  if (/select/i.test(tool)) return '正在选择页面选项...'
  if (/scroll/i.test(tool)) return '正在滚动页面查找内容...'
  if (/observe|state|get/i.test(tool)) return '正在读取当前页面...'
  return '正在执行页面操作...'
}

export function bindPanelActivity(agent: AgentLike) {
  if (installedAgents.has(agent)) return
  installedAgents.add(agent)

  agent.addEventListener('statuschange', () => {
    if (agent.status === 'running') {
      setRunningClass(true)
      setLiveText('正在准备读取当前页面...')
      enhanceSimulatorMask()
      return
    }
    if (agent.status === 'completed' || agent.status === 'error' || agent.status === 'stopped') {
      setRunningClass(false)
      scrollPanelToBottom()
      scrollPanelToBottom(250)
      removeLiveCard()
    }
  })

  agent.addEventListener('activity', (event) => {
    revealSimulatorCursor()
    setLiveText(activityText((event as CustomEvent<AgentActivity>).detail))
  })
}
