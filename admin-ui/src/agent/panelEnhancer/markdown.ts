function appendInlineText(parent: HTMLElement, text: string) {
  const pattern = /(\*\*[^*]+\*\*|`[^`]+`)/g
  let lastIndex = 0
  for (const match of text.matchAll(pattern)) {
    if (match.index > lastIndex) {
      parent.append(document.createTextNode(text.slice(lastIndex, match.index)))
    }
    const value = match[0]
    if (value.startsWith('**')) {
      const strong = document.createElement('strong')
      strong.textContent = value.slice(2, -2)
      parent.append(strong)
    } else {
      const code = document.createElement('code')
      code.textContent = value.slice(1, -1)
      parent.append(code)
    }
    lastIndex = match.index + value.length
  }
  if (lastIndex < text.length) {
    parent.append(document.createTextNode(text.slice(lastIndex)))
  }
}

export function renderMarkdownText(text: string) {
  const wrapper = document.createElement('div')
  wrapper.className = 'bocoo-agent-md'
  const lines = text
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map((line) => line.trimEnd())
  let list: HTMLUListElement | HTMLOListElement | null = null

  const closeList = () => {
    list = null
  }

  lines.forEach((line) => {
    if (!line.trim()) {
      closeList()
      return
    }

    const heading = line.match(/^(#{1,3})\s+(.+)$/)
    if (heading) {
      closeList()
      const level = String(Math.min(heading[1].length + 2, 5)) as '3' | '4' | '5'
      const element = document.createElement(`h${level}`)
      appendInlineText(element, heading[2])
      wrapper.append(element)
      return
    }

    const bullet = line.match(/^[-*]\s+(.+)$/)
    const ordered = line.match(/^\d+\.\s+(.+)$/)
    if (bullet || ordered) {
      const shouldBeOrdered = Boolean(ordered)
      if (!list || (shouldBeOrdered && list.tagName !== 'OL') || (!shouldBeOrdered && list.tagName !== 'UL')) {
        list = document.createElement(shouldBeOrdered ? 'ol' : 'ul')
        wrapper.append(list)
      }
      const item = document.createElement('li')
      appendInlineText(item, bullet?.[1] || ordered?.[1] || '')
      list.append(item)
      return
    }

    closeList()
    const paragraph = document.createElement('p')
    appendInlineText(paragraph, line)
    wrapper.append(paragraph)
  })

  return wrapper
}

export function shouldRenderMarkdown(text: string) {
  return /(^|\n)(#{1,3}\s+|[-*]\s+|\d+\.\s+)/.test(text) || /\*\*[^*]+\*\*|`[^`]+`/.test(text)
}
