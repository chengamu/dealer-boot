import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'
import { useFormulaUsageRules } from './useFormulaUsageRules'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaUsageRuleVO
} from '@/api/product-capability/types'

type FormulaUsageEditorProps = {
  modelValue: boolean
  usageRow: ProductFormulaMaterialVO | null
  usageRows?: ProductFormulaMaterialVO[]
  materials?: ProductFormulaMaterialVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
}

type UsageSnapshot = {
  materialCodes: string[]
  usageRows: ProductFormulaMaterialVO[]
  rules: ProductFormulaUsageRuleVO[]
}

export function useFormulaUsageEditor(props: FormulaUsageEditorProps, close: () => void) {
  const localeStore = useLocaleStore()
  const t = (key: string) => getMessage(key, localeStore.language)
  const summaryCollapsed = ref(false)
  const usageSnapshot = ref<UsageSnapshot | null>(null)

  const currentUsageRows = computed(() => {
    if (props.usageRows?.length) return props.usageRows
    return props.usageRow ? [props.usageRow] : []
  })

  const usageRules = useFormulaUsageRules(props, t, currentUsageRows)
  const unsavedChangesGuard = useUnsavedChangesGuard({
    enabled: () => props.modelValue && Boolean(props.usageRow),
    getSnapshot: () => JSON.stringify(createUsageSnapshot()),
    confirmDiscard: confirmDiscardChanges
  })

  watch(() => props.modelValue, (open) => {
    if (open && props.usageRow) {
      summaryCollapsed.value = false
      usageRules.ensureInitialRule()
      usageRules.selectedRule.value = null
      captureUsageSnapshot()
      window.addEventListener('keydown', handleDrawerShortcut)
    } else {
      window.removeEventListener('keydown', handleDrawerShortcut)
      handleDrawerClosed()
    }
  })

  watch(() => props.usageRow?.materialCode, () => {
    if (!props.modelValue || !props.usageRow) return
    usageRules.ensureInitialRule()
    usageRules.selectedRule.value = null
  })

  onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleDrawerShortcut)
  })

  function cloneValue<T>(value: T): T {
    return JSON.parse(JSON.stringify(value)) as T
  }

  function createUsageSnapshot(): UsageSnapshot {
    const materialCodes = currentUsageRows.value.map((row) => row.materialCode).filter(Boolean) as string[]
    return {
      materialCodes,
      usageRows: cloneValue(currentUsageRows.value),
      rules: cloneValue(props.usageRules.filter((rule) => materialCodes.includes(String(rule.materialCode || ''))))
    }
  }

  function captureUsageSnapshot() {
    usageSnapshot.value = createUsageSnapshot()
    unsavedChangesGuard.markPristine()
  }

  function restoreUsageSnapshot() {
    const snapshot = usageSnapshot.value
    if (!snapshot) return

    snapshot.usageRows.forEach((snapshotRow) => {
      const targetRow = currentUsageRows.value.find((row) => row.materialCode === snapshotRow.materialCode)
      if (!targetRow) return
      const target = targetRow as Record<string, unknown>
      Object.keys(target).forEach((key) => delete target[key])
      Object.assign(target, cloneValue(snapshotRow))
    })

    const materialCodes = new Set(snapshot.materialCodes)
    if (!materialCodes.size) return
    for (let index = props.usageRules.length - 1; index >= 0; index--) {
      if (materialCodes.has(String(props.usageRules[index].materialCode || ''))) {
        props.usageRules.splice(index, 1)
      }
    }
    props.usageRules.push(...cloneValue(snapshot.rules))
  }

  async function confirmDiscardChanges() {
    try {
      await ElMessageBox.confirm(t('common.unsavedChangesConfirm'), t('common.prompt'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      restoreUsageSnapshot()
      unsavedChangesGuard.markPristine()
      return true
    } catch {
      return false
    }
  }

  async function closeDrawerWithGuard() {
    await unsavedChangesGuard.closeWithGuard(close)
  }

  function confirmAndClose() {
    usageRules.applyCurrentUsageToBatchRows()
    unsavedChangesGuard.markPristine()
    close()
  }

  function handleDrawerBeforeClose(done: () => void) {
    unsavedChangesGuard.canClose().then((allowed) => {
      if (allowed) done()
    })
  }

  function handleDrawerClosed() {
    usageRules.selectedRule.value = null
    usageSnapshot.value = null
    unsavedChangesGuard.resetPristine()
  }

  function handleDrawerShortcut(event: KeyboardEvent) {
    if (event.key !== 'Escape' || !props.modelValue) return
    event.preventDefault()
    closeDrawerWithGuard()
  }

  return {
    selectedRule: usageRules.selectedRule,
    summaryCollapsed,
    expressionEditorOpen: usageRules.expressionEditorOpen,
    expressionEditorText: usageRules.expressionEditorText,
    expressionEditorTarget: usageRules.expressionEditorTarget,
    formulaFields: usageRules.formulaFields,
    currentUsageRows,
    currentRules: usageRules.currentRules,
    closeDrawerWithGuard,
    confirmAndClose,
    handleDrawerBeforeClose,
    handleDrawerClosed,
    handleUsageModeChange: usageRules.handleUsageModeChange,
    syncFixedRuleFromRow: usageRules.syncFixedRuleFromRow,
    generateFabricRules: usageRules.generateFabricRules,
    addConditionalUsageRule: usageRules.addConditionalUsageRule,
    copySelectedRule: usageRules.copySelectedRule,
    removeSelectedRule: usageRules.removeSelectedRule,
    handleDefaultRuleChange: usageRules.handleDefaultRuleChange,
    syncFormula: usageRules.syncFormula,
    openExpressionEditor: usageRules.openExpressionEditor,
    confirmExpressionEditor: usageRules.confirmExpressionEditor
  }
}
