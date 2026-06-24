export interface UnsavedChangesGuardOptions {
  enabled: () => boolean
  getSnapshot: () => string
  confirmDiscard: () => Promise<boolean>
}

export function useUnsavedChangesGuard(options: UnsavedChangesGuardOptions) {
  let pristineSnapshot = ''

  function markPristine() {
    pristineSnapshot = options.getSnapshot()
  }

  function resetPristine() {
    pristineSnapshot = ''
  }

  function hasUnsavedChanges() {
    return options.enabled() && pristineSnapshot !== options.getSnapshot()
  }

  async function canClose() {
    if (!hasUnsavedChanges()) return true
    return options.confirmDiscard()
  }

  async function closeWithGuard(close: () => void) {
    if (!(await canClose())) return false
    close()
    return true
  }

  return {
    canClose,
    closeWithGuard,
    hasUnsavedChanges,
    markPristine,
    resetPristine
  }
}
