function easeInOutQuad(time: number, start: number, change: number, duration: number) {
  let current = time / (duration / 2)
  if (current < 1) return (change / 2) * current * current + start
  current--
  return (-change / 2) * (current * (current - 2) - 1) + start
}

const requestAnimFrame = window.requestAnimationFrame || ((callback: FrameRequestCallback) => window.setTimeout(callback, 1000 / 60))

function move(amount: number) {
  document.documentElement.scrollTop = amount
  if (document.body.parentElement) document.body.parentElement.scrollTop = amount
  document.body.scrollTop = amount
}

function position() {
  return document.documentElement.scrollTop || document.body.parentElement?.scrollTop || document.body.scrollTop
}

export function scrollTo(to: number, duration = 500, callback?: () => void) {
  const start = position()
  const change = to - start
  const increment = 20
  let currentTime = 0

  const animateScroll = () => {
    currentTime += increment
    const val = easeInOutQuad(currentTime, start, change, duration)
    move(val)
    if (currentTime < duration) {
      requestAnimFrame(animateScroll)
      return
    }
    callback?.()
  }

  animateScroll()
}
