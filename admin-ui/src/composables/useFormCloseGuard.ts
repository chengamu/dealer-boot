import { ElMessageBox } from 'element-plus'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'

interface FormCloseGuardOptions {
  enabled: () => boolean
  getSnapshot: () => string
  close: () => void
  reset?: () => void
  t: (key: string) => string
}

export function useFormCloseGuard(options: FormCloseGuardOptions) {
  const guard = useUnsavedChangesGuard({
    enabled: options.enabled,
    getSnapshot: options.getSnapshot,
    confirmDiscard: confirmDiscardChanges
  })

  async function confirmDiscardChanges() {
    try {
      await ElMessageBox.confirm(options.t('common.unsavedChangesConfirm'), options.t('common.prompt'), {
        confirmButtonText: options.t('common.confirm'),
        cancelButtonText: options.t('common.cancel'),
        type: 'warning'
      })
      return true
    } catch {
      return false
    }
  }

  async function closeWithGuard() {
    await guard.closeWithGuard(options.close)
  }

  function beforeClose(done: () => void) {
    guard.canClose().then((allowed) => {
      if (allowed) done()
    })
  }

  function handleClosed() {
    options.reset?.()
    guard.resetPristine()
  }

  return {
    beforeClose,
    closeWithGuard,
    handleClosed,
    markPristine: guard.markPristine,
    resetPristine: guard.resetPristine
  }
}
