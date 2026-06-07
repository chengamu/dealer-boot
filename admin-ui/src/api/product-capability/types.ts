import type { PageQuery } from '@/types/api'

export interface ProductRecord {
  [key: string]: unknown
}

export interface ProductPageQuery extends PageQuery {
  [key: string]: unknown
}

export interface ReferenceCheckResult {
  canRemove?: boolean
  canDisable?: boolean
  referenceCount?: number
  messageKey?: string
  references?: Array<Record<string, unknown>>
}

export interface WorkbenchSummary {
  modelCount?: number
  draftCount?: number
  publishedCount?: number
  blockerCount?: number
  warningCount?: number
  pendingSyncCount?: number
  lastSyncTime?: string
}

export interface WorkbenchProgress {
  modelId?: number
  modelCode?: string
  modelName?: string
  categoryName?: string
  templateStatus?: string
  priceStatus?: string
  assetStatus?: string
  publishStatus?: string
  blockerCount?: number
  warningCount?: number
  updatedTime?: string
}

export interface WorkbenchPriority {
  taskId?: number
  taskType?: string
  severity?: string
  targetType?: string
  targetCode?: string
  targetName?: string
  ownerName?: string
  dueTime?: string
  status?: string
}

export interface WorkbenchSyncEvent {
  eventId?: number
  eventType?: string
  targetType?: string
  targetCode?: string
  status?: string
  retryCount?: number
  lastErrorKey?: string
  createdTime?: string
  updatedTime?: string
}

