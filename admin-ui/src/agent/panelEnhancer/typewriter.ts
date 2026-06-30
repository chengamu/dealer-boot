import { renderMarkdownText, shouldRenderMarkdown } from './markdown'
import { sanitizePanelText } from './sanitize'
import { scrollPanelToBottom } from './scroll'

const typewriterDelay = 12
const maxAnimatedChars = 260

function getCardTextElement(card: HTMLElement) {
  const content = card.querySelector<HTMLElement>('[class*="historyContent"]')
  if (!content) return null
  const nodes = Array.from(content.children).filter((element) => !String(element.className).includes('statusIcon'))
  return nodes[0] as HTMLElement | undefined
}

function isAssistantCard(card: HTMLElement) {
  const className = String(card.className)
  return className.includes('output') || className.includes('question')
}

function replaceWithMarkdown(target: HTMLElement, text: string) {
  if (!shouldRenderMarkdown(text)) {
    target.textContent = text
    return
  }
  target.replaceChildren(renderMarkdownText(text))
}

function animateText(target: HTMLElement, text: string) {
  target.dataset.bocooTypewriter = 'running'
  target.textContent = ''
  const source = text.length > maxAnimatedChars ? text.slice(0, maxAnimatedChars) : text
  let index = 0
  const timer = window.setInterval(() => {
    index += 1
    target.textContent = source.slice(0, index)
    scrollPanelToBottom()
    if (index >= source.length) {
      window.clearInterval(timer)
      replaceWithMarkdown(target, text)
      target.dataset.bocooTypewriter = 'done'
      scrollPanelToBottom()
      scrollPanelToBottom(120)
    }
  }, typewriterDelay)
}

export function enhanceTypewriter(root: HTMLElement) {
  root.querySelectorAll<HTMLElement>('[class*="historyItem"]').forEach((card) => {
    if (!isAssistantCard(card)) return
    const target = getCardTextElement(card)
    if (!target || target.dataset.bocooTypewriter) return
    const text = sanitizePanelText(target.textContent || '').trim()
    if (!text) return
    animateText(target, text)
    scrollPanelToBottom()
  })
}
