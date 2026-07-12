import { onBeforeUnmount, ref } from 'vue'

export function usePaymentPolling(refresh: () => Promise<void>) {
  const polling = ref(false)
  let timer: number | undefined

  function stop() {
    if (timer) window.clearInterval(timer)
    timer = undefined
    polling.value = false
  }

  function start() {
    stop()
    polling.value = true
    timer = window.setInterval(() => void refresh(), 3000)
  }

  onBeforeUnmount(stop)
  return { polling, start, stop }
}
