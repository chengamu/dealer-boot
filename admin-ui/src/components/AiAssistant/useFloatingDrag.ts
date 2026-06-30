import { computed, onBeforeUnmount, onMounted, reactive } from 'vue'

type FloatingDragOptions = {
  storageKey: string
  size: number
  edgePadding?: number
  dragThreshold?: number
  onTap: () => void | Promise<void>
}

export function useFloatingDrag(options: FloatingDragOptions) {
  const edgePadding = options.edgePadding ?? 12
  const dragThreshold = options.dragThreshold ?? 6
  const position = reactive({ x: 0, y: 0 })
  const dragState = reactive({
    active: false,
    moved: false,
    pointerId: 0,
    grabOffsetX: 0,
    grabOffsetY: 0,
    startX: 0,
    startY: 0,
    target: null as HTMLElement | null
  })

  const style = computed(() => ({
    transform: `translate3d(${position.x}px, ${position.y}px, 0)`
  }))

  onMounted(restorePosition)
  onBeforeUnmount(removeDragListeners)

  function onPointerDown(event: PointerEvent) {
    if (event.button !== 0) return
    const target = event.currentTarget as HTMLElement
    dragState.active = true
    dragState.moved = false
    dragState.pointerId = event.pointerId
    dragState.grabOffsetX = event.clientX - position.x
    dragState.grabOffsetY = event.clientY - position.y
    dragState.startX = event.clientX
    dragState.startY = event.clientY
    dragState.target = target
    target.setPointerCapture?.(event.pointerId)
    event.preventDefault()
    document.addEventListener('pointermove', onPointerMove)
    document.addEventListener('pointerup', onPointerUp)
    document.addEventListener('pointercancel', onPointerUp)
    target.addEventListener('lostpointercapture', onLostPointerCapture, { once: true })
    window.addEventListener('blur', cancelDrag, { once: true })
  }

  function onPointerMove(event: PointerEvent) {
    if (!dragState.active || event.pointerId !== dragState.pointerId) return
    const deltaX = event.clientX - dragState.startX
    const deltaY = event.clientY - dragState.startY
    if (Math.hypot(deltaX, deltaY) >= dragThreshold) {
      dragState.moved = true
    }
    if (!dragState.moved) return
    event.preventDefault()
    setPosition(event.clientX - dragState.grabOffsetX, event.clientY - dragState.grabOffsetY)
  }

  function onPointerUp(event: PointerEvent) {
    if (!dragState.active || event.pointerId !== dragState.pointerId) return
    finishDrag()
  }

  function onLostPointerCapture() {
    cancelDrag()
  }

  function cancelDrag() {
    if (!dragState.active) return
    finishDrag(true)
  }

  function finishDrag(cancelled = false) {
    const moved = dragState.moved
    dragState.active = false
    dragState.target?.releasePointerCapture?.(dragState.pointerId)
    dragState.target?.removeEventListener('lostpointercapture', onLostPointerCapture)
    dragState.target = null
    removeDragListeners()
    if (moved || cancelled) {
      savePosition()
      return
    }
    void options.onTap()
  }

  function restorePosition() {
    const fallback = getBoundedPosition(
      window.innerWidth - options.size - 24,
      window.innerHeight - options.size - 24
    )
    try {
      const saved = JSON.parse(localStorage.getItem(options.storageKey) || 'null') as {
        x?: number
        y?: number
      } | null
      if (typeof saved?.x === 'number' && typeof saved?.y === 'number') {
        setPosition(saved.x, saved.y)
        return
      }
    } catch {
      localStorage.removeItem(options.storageKey)
    }
    position.x = fallback.x
    position.y = fallback.y
  }

  function savePosition() {
    localStorage.setItem(options.storageKey, JSON.stringify({ x: position.x, y: position.y }))
  }

  function setPosition(x: number, y: number) {
    const next = getBoundedPosition(x, y)
    position.x = next.x
    position.y = next.y
  }

  function getBoundedPosition(x: number, y: number) {
    const maxX = Math.max(edgePadding, window.innerWidth - options.size - edgePadding)
    const maxY = Math.max(edgePadding, window.innerHeight - options.size - edgePadding)
    return {
      x: Math.round(Math.min(Math.max(edgePadding, x), maxX)),
      y: Math.round(Math.min(Math.max(edgePadding, y), maxY))
    }
  }

  function removeDragListeners() {
    document.removeEventListener('pointermove', onPointerMove)
    document.removeEventListener('pointerup', onPointerUp)
    document.removeEventListener('pointercancel', onPointerUp)
    window.removeEventListener('blur', cancelDrag)
  }

  return {
    isDragging: computed(() => dragState.active && dragState.moved),
    style,
    onPointerDown
  }
}
