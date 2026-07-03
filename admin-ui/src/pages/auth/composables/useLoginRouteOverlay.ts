import { onMounted, onUnmounted } from 'vue'

const overlaySelector = '.el-overlay, .v-modal, .el-loading-mask'

export function useLoginRouteOverlay() {
  let overlayCleanupTimer: number | undefined

  function clearStaleOverlays() {
    document.body.classList.remove('el-popup-parent--hidden')
    const overlays = Array.from(document.querySelectorAll(overlaySelector))
    overlays.forEach((element) => element.remove())
  }

  onMounted(() => {
    document.body.classList.add('login-route-active')
    clearStaleOverlays()
    overlayCleanupTimer = window.setInterval(clearStaleOverlays, 100)
    window.setTimeout(() => {
      if (overlayCleanupTimer) window.clearInterval(overlayCleanupTimer)
      overlayCleanupTimer = undefined
    }, 3000)
  })

  onUnmounted(() => {
    document.body.classList.remove('login-route-active')
    if (overlayCleanupTimer) window.clearInterval(overlayCleanupTimer)
  })
}
