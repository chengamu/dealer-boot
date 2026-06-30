export function revealSimulatorCursor() {
  const mask = document.getElementById('page-agent-runtime_simulator-mask')
  if (!mask) return

  const cursorElements = mask.querySelectorAll<HTMLElement>('[class*="cursor"]')
  cursorElements.forEach((element) => {
    element.style.setProperty('display', 'block', 'important')
    element.style.setProperty('opacity', '1', 'important')
  })

  const cursorRoot = Array.from(cursorElements).find((element) =>
    String(element.className).includes('cursor_')
  )
  if (cursorRoot) {
    cursorRoot.style.setProperty('--cursor-size', '46px')
    cursorRoot.style.setProperty('opacity', '1', 'important')
    cursorRoot.style.filter = 'drop-shadow(0 0 10px rgba(11, 109, 255, 0.36))'
  }

  const border = Array.from(cursorElements).find((element) =>
    String(element.className).includes('cursorBorder')
  )
  if (border) {
    border.style.background = 'linear-gradient(45deg, #075cff, #2aa7ff)'
    border.style.filter =
      'drop-shadow(0 0 5px rgba(7, 92, 255, 0.58)) drop-shadow(0 0 12px rgba(42, 167, 255, 0.28))'
  }

  const filling = Array.from(cursorElements).find((element) =>
    String(element.className).includes('cursorFilling')
  )
  if (filling) {
    filling.style.filter =
      'drop-shadow(0 1px 3px rgba(0, 0, 0, 0.28)) drop-shadow(0 0 8px rgba(7, 92, 255, 0.36))'
  }
}

export function enhanceSimulatorMask() {
  const mask = document.getElementById('page-agent-runtime_simulator-mask')
  if (!mask) return
  mask.style.cursor = 'none'
  const motion = mask.firstElementChild as HTMLElement | null
  if (motion) {
    motion.style.opacity = '0.12'
    motion.style.filter = 'hue-rotate(185deg) saturate(0.55) brightness(1.12)'
  }
  revealSimulatorCursor()
}
