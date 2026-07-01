import type { ProductFormulaSetupVO } from '@/api/product-capability/types'

export type FormulaDraftSection = 'materials' | 'options'

export interface FormulaSetupDraftRecord {
  draftKey: string
  schemaVersion: 3
  tenantId: string
  userId: string
  formulaId: string
  formulaCode?: string
  formulaName?: string
  section: FormulaDraftSection
  serverUpdateTime?: string
  savedAt: number
  payload: ProductFormulaSetupVO
}

const DB_NAME = 'bocoo_formula_drafts'
const STORE_NAME = 'formula_setup_drafts'
const DB_VERSION = 1
const SCHEMA_VERSION = 3
const DRAFT_TTL_MS = 14 * 24 * 60 * 60 * 1000

let dbPromise: Promise<IDBDatabase> | null = null

export function buildFormulaDraftKey(tenantId: string, userId: string, formulaId: string, section: FormulaDraftSection) {
  return [tenantId || 'tenant', userId || 'user', formulaId, section].join(':')
}

export function isFormulaDraftExpired(record: FormulaSetupDraftRecord, now = Date.now()) {
  return record.schemaVersion !== SCHEMA_VERSION || now - record.savedAt > DRAFT_TTL_MS
}

export async function getFormulaSetupDraft(draftKey: string) {
  const record = await request<FormulaSetupDraftRecord | undefined>('readonly', (store) => store.get(draftKey))
  if (record && isFormulaDraftExpired(record)) {
    await deleteFormulaSetupDraft(draftKey)
    return undefined
  }
  return record
}

export async function putFormulaSetupDraft(record: Omit<FormulaSetupDraftRecord, 'schemaVersion'>) {
  await request('readwrite', (store) => store.put({ ...record, schemaVersion: SCHEMA_VERSION }))
}

export async function deleteFormulaSetupDraft(draftKey: string) {
  await request('readwrite', (store) => store.delete(draftKey))
}

export async function pruneExpiredFormulaDrafts(now = Date.now()) {
  const records = await request<FormulaSetupDraftRecord[]>('readonly', (store) => store.getAll())
  await Promise.all(records.filter((record) => isFormulaDraftExpired(record, now)).map((record) => deleteFormulaSetupDraft(record.draftKey)))
}

function openDb() {
  if (dbPromise) return dbPromise
  dbPromise = new Promise((resolve, reject) => {
    if (!window.indexedDB) {
      reject(new Error('IndexedDB is not available.'))
      return
    }
    const dbRequest = window.indexedDB.open(DB_NAME, DB_VERSION)
    dbRequest.onupgradeneeded = () => {
      const db = dbRequest.result
      if (!db.objectStoreNames.contains(STORE_NAME)) db.createObjectStore(STORE_NAME, { keyPath: 'draftKey' })
    }
    dbRequest.onsuccess = () => resolve(dbRequest.result)
    dbRequest.onerror = () => reject(dbRequest.error || new Error('Failed to open IndexedDB.'))
  })
  return dbPromise
}

async function request<T>(mode: IDBTransactionMode, action: (store: IDBObjectStore) => IDBRequest<T>) {
  const db = await openDb()
  return new Promise<T>((resolve, reject) => {
    const transaction = db.transaction(STORE_NAME, mode)
    const storeRequest = action(transaction.objectStore(STORE_NAME))
    storeRequest.onsuccess = () => resolve(storeRequest.result)
    storeRequest.onerror = () => reject(storeRequest.error || new Error('IndexedDB request failed.'))
    transaction.onerror = () => reject(transaction.error || new Error('IndexedDB transaction failed.'))
  })
}
