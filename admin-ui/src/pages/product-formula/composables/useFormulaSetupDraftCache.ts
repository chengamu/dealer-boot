import { ElMessageBox } from 'element-plus'
import { computed, onBeforeUnmount, ref, watch, type Ref } from 'vue'
import type { ProductFormulaSetupVO, ProductFormulaVO } from '@/api/product-capability/types'
import {
  buildFormulaDraftKey,
  deleteFormulaSetupDraft,
  getFormulaSetupDraft,
  pruneExpiredFormulaDrafts,
  putFormulaSetupDraft,
  type FormulaDraftSection,
  type FormulaSetupDraftRecord
} from '@/pages/product-formula/utils/formulaDraftStore'

interface FormulaSetupDraftCacheOptions {
  activeTab: Ref<'content' | 'options'>
  currentFormulaId: Ref<string>
  formula: Ref<ProductFormulaVO>
  setup: ProductFormulaSetupVO
  tenantId: () => string
  userId: () => string
  t: (key: string) => string
}

interface DraftContext {
  draftKey: string
  tenantId: string
  userId: string
  formulaId: string
  formulaCode?: string
  formulaName?: string
  section: FormulaDraftSection
  serverUpdateTime?: string
}

const DEBOUNCE_MS = 3000
const SNAPSHOT_MS = 30000
const RESTORED_SESSION_PREFIX = 'productFormula.setup.restoredDraft.'

export function useFormulaSetupDraftCache(options: FormulaSetupDraftCacheOptions) {
  const cacheStatus = ref('')
  const hasPendingDraft = ref(false)
  let cacheEnabled = false
  let dirty = false
  let baselinePayload = ''
  let activeContext: DraftContext | undefined
  let debounceTimer: number | undefined
  let snapshotTimer: number | undefined

  const section = computed<FormulaDraftSection>(() => options.activeTab.value === 'options' ? 'options' : 'materials')
  const draftKey = computed(() => buildFormulaDraftKey(options.tenantId(), options.userId(), options.currentFormulaId.value, section.value))

  watch(options.setup, () => {
    if (!cacheEnabled) return
    if (payloadSignature(section.value, options.setup) === baselinePayload) {
      dirty = false
      hasPendingDraft.value = false
      window.clearTimeout(debounceTimer)
      return
    }
    dirty = true
    hasPendingDraft.value = true
    cacheStatus.value = options.t('productCenter.formulaSetupDraft.editing')
    scheduleSave()
  }, { deep: true })

  async function afterSetupLoaded() {
    cacheEnabled = false
    dirty = false
    hasPendingDraft.value = false
    window.clearTimeout(debounceTimer)
    await pruneExpiredSafely()
    activeContext = options.currentFormulaId.value ? currentContext() : undefined
    await offerRestoreDraft()
    baselinePayload = payloadSignature(section.value, options.setup)
    cacheEnabled = true
    startSnapshotTimer()
  }

  async function beforeSetupReload() {
    await flushDraft()
    cacheEnabled = false
    window.clearTimeout(debounceTimer)
    activeContext = undefined
  }

  async function clearCurrentDraft() {
    cacheEnabled = false
    dirty = false
    window.clearTimeout(debounceTimer)
    const context = activeContext || currentContext()
    await deleteFormulaSetupDraft(context.draftKey)
    clearRestoredDraft(context.draftKey)
    hasPendingDraft.value = false
    cacheStatus.value = ''
  }

  async function flushDraft() {
    if (!cacheEnabled || !dirty || !activeContext) return
    window.clearTimeout(debounceTimer)
    await saveDraft(activeContext)
  }

  function scheduleSave() {
    window.clearTimeout(debounceTimer)
    debounceTimer = window.setTimeout(() => {
      if (dirty && activeContext) void saveDraft(activeContext)
    }, DEBOUNCE_MS)
  }

  function startSnapshotTimer() {
    window.clearInterval(snapshotTimer)
    snapshotTimer = window.setInterval(() => { void flushDraft() }, SNAPSHOT_MS)
  }

  async function saveDraft(context: DraftContext) {
    try {
      const savedAt = Date.now()
      cacheStatus.value = options.t('productCenter.formulaSetupDraft.saving')
      await putFormulaSetupDraft({
        draftKey: context.draftKey,
        tenantId: context.tenantId,
        userId: context.userId,
        formulaId: context.formulaId,
        formulaCode: context.formulaCode,
        formulaName: context.formulaName,
        section: context.section,
        serverUpdateTime: context.serverUpdateTime,
        savedAt,
        payload: payloadOf(context.section, options.setup)
      })
      dirty = false
      baselinePayload = payloadSignature(context.section, options.setup)
      hasPendingDraft.value = true
      if (hasRestoredDraft(context.draftKey)) markRestoredDraft(context.draftKey, savedAt)
      cacheStatus.value = `${options.t('productCenter.formulaSetupDraft.saved')} ${formatTime(savedAt)}`
    } catch (error) {
      cacheStatus.value = options.t('productCenter.formulaSetupDraft.failed')
      console.warn(error)
    }
  }

  async function offerRestoreDraft() {
    if (!activeContext) return
    const draft = await getDraftSafely()
    if (!draft) return
    if (!isDraftForContext(draft, activeContext)) {
      await deleteFormulaSetupDraft(draft.draftKey)
      clearRestoredDraft(draft.draftKey)
      cacheStatus.value = ''
      return
    }
    const stale = isServerNewer(draft, activeContext.serverUpdateTime)
    if (!stale && isRestoredDraft(draft)) {
      applyDraft(draft)
      hasPendingDraft.value = true
      cacheStatus.value = `${options.t('productCenter.formulaSetupDraft.restored')} ${formatTime(draft.savedAt)}`
      return
    }
    const message = stale
      ? options.t('productCenter.formulaSetupDraft.restoreStaleMessage')
      : options.t('productCenter.formulaSetupDraft.restoreMessage')
    try {
      await ElMessageBox.confirm(message.replace('{time}', formatTime(draft.savedAt)), options.t('productCenter.formulaSetupDraft.restoreTitle'), {
        confirmButtonText: stale ? options.t('productCenter.formulaSetupDraft.restoreAnyway') : options.t('productCenter.formulaSetupDraft.restore'),
        cancelButtonText: options.t('productCenter.formulaSetupDraft.discard'),
        showClose: false,
        closeOnClickModal: false,
        closeOnPressEscape: false,
        type: stale ? 'warning' : 'info'
      })
      applyDraft(draft)
      markRestoredDraft(draft.draftKey, draft.savedAt)
      hasPendingDraft.value = true
      cacheStatus.value = `${options.t('productCenter.formulaSetupDraft.restored')} ${formatTime(draft.savedAt)}`
    } catch {
      await deleteFormulaSetupDraft(draft.draftKey)
      clearRestoredDraft(draft.draftKey)
      hasPendingDraft.value = false
      cacheStatus.value = ''
    }
  }

  async function getDraftSafely() {
    try {
      return activeContext ? await getFormulaSetupDraft(activeContext.draftKey) : undefined
    } catch (error) {
      console.warn(error)
      return undefined
    }
  }

  async function pruneExpiredSafely() {
    try {
      await pruneExpiredFormulaDrafts()
    } catch (error) {
      console.warn(error)
    }
  }

  function applyDraft(draft: FormulaSetupDraftRecord) {
    const payload = draft.payload || {}
    if (draft.section === 'materials') {
      options.setup.materials = payload.materials || []
      options.setup.usageRules = payload.usageRules || []
    } else {
      options.setup.options = payload.options || []
      options.setup.optionValues = payload.optionValues || []
      options.setup.optionMaterials = payload.optionMaterials || []
      options.setup.restrictions = payload.restrictions || []
    }
  }

  onBeforeUnmount(() => {
    void flushDraft()
    window.clearTimeout(debounceTimer)
    window.clearInterval(snapshotTimer)
  })

  return { cacheStatus, hasPendingDraft, afterSetupLoaded, beforeSetupReload, clearCurrentDraft, flushDraft }

  function currentContext(): DraftContext {
    return {
      draftKey: draftKey.value,
      tenantId: options.tenantId(),
      userId: options.userId(),
      formulaId: options.currentFormulaId.value,
      formulaCode: options.formula.value.formulaCode,
      formulaName: options.formula.value.formulaName,
      section: section.value,
      serverUpdateTime: options.formula.value.updateTime
    }
  }
}

function payloadOf(section: FormulaDraftSection, setup: ProductFormulaSetupVO): ProductFormulaSetupVO {
  if (section === 'materials') return { materials: plainRows(setup.materials), usageRules: plainRows(setup.usageRules) }
  return {
    options: plainRows(setup.options),
    optionValues: plainRows(setup.optionValues),
    optionMaterials: plainRows(setup.optionMaterials),
    restrictions: plainRows(setup.restrictions)
  }
}

function payloadSignature(section: FormulaDraftSection, setup: ProductFormulaSetupVO) {
  return JSON.stringify(payloadOf(section, setup))
}

function plainRows<T extends Record<string, unknown>>(rows?: T[]) {
  return (rows || []).map((row) => ({ ...row }))
}

function isServerNewer(draft: FormulaSetupDraftRecord, serverUpdateTime?: string) {
  if (!draft.serverUpdateTime || !serverUpdateTime) return false
  return new Date(serverUpdateTime).getTime() > new Date(draft.serverUpdateTime).getTime()
}

function isDraftForContext(draft: FormulaSetupDraftRecord, context: DraftContext) {
  return draft.formulaId === context.formulaId
    && draft.section === context.section
    && draft.formulaCode === context.formulaCode
    && draft.formulaName === context.formulaName
}

function hasRestoredDraft(draftKey: string) {
  return Boolean(window.sessionStorage.getItem(restoredSessionKey(draftKey)))
}

function isRestoredDraft(draft: FormulaSetupDraftRecord) {
  const restoredAt = Number(window.sessionStorage.getItem(restoredSessionKey(draft.draftKey)) || 0)
  return restoredAt === draft.savedAt
}

function markRestoredDraft(draftKey: string, savedAt: number) {
  window.sessionStorage.setItem(restoredSessionKey(draftKey), String(savedAt))
}

function clearRestoredDraft(draftKey: string) {
  window.sessionStorage.removeItem(restoredSessionKey(draftKey))
}

function restoredSessionKey(draftKey: string) {
  return `${RESTORED_SESSION_PREFIX}${draftKey}`
}

function formatTime(value: number) {
  return new Date(value).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
