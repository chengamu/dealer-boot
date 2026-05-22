import type { DirectiveBinding, ObjectDirective } from 'vue'

interface CopyTextElement extends HTMLElement {
  $copyValue?: string
  $copyCallback?: (value?: string) => void
  $destroyCopy?: () => void
}

const copyText: ObjectDirective<CopyTextElement, string | ((value?: string) => void)> = {
  beforeMount(el: CopyTextElement, { value, arg }: DirectiveBinding<string | ((value?: string) => void)>) {
    if (arg === 'callback' && typeof value === 'function') {
      el.$copyCallback = value
      return
    }

    el.$copyValue = String(value ?? '')
    const handler = () => {
      copyTextToClipboard(el.$copyValue || '')
      el.$copyCallback?.(el.$copyValue)
    }
    el.addEventListener('click', handler)
    el.$destroyCopy = () => el.removeEventListener('click', handler)
  },
  unmounted(el: CopyTextElement) {
    el.$destroyCopy?.()
  }
}

function copyTextToClipboard(input: string, { target = document.body }: { target?: HTMLElement } = {}) {
  const element = document.createElement('textarea')
  const previouslyFocusedElement = document.activeElement as HTMLElement | null

  element.value = input
  element.setAttribute('readonly', '')
  element.style.contain = 'strict'
  element.style.position = 'absolute'
  element.style.left = '-9999px'
  element.style.fontSize = '12pt'

  const selection = document.getSelection()
  const originalRange = selection && selection.rangeCount > 0 ? selection.getRangeAt(0) : null

  target.append(element)
  element.select()
  element.selectionStart = 0
  element.selectionEnd = input.length

  let isSuccess = false
  try {
    isSuccess = document.execCommand('copy')
  } catch {
    isSuccess = false
  }

  element.remove()

  if (selection && originalRange) {
    selection.removeAllRanges()
    selection.addRange(originalRange)
  }

  previouslyFocusedElement?.focus()
  return isSuccess
}

export default copyText
