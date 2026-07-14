import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)

const UTC_PATTERN = /(?:Z|[+-]\d{2}:?\d{2})$/
const DATE_ONLY_PATTERN = /^\d{4}-\d{2}-\d{2}$/

export function parseUtc(input?: string | number | Date | null) {
  if (!input) return null
  if (input instanceof Date || typeof input === 'number') return dayjs(input)
  const value = String(input).trim()
  if (!value) return null
  return UTC_PATTERN.test(value) ? dayjs(value) : dayjs.utc(value)
}

export function formatUtc(
  input?: string | number | Date | null,
  format = 'YYYY-MM-DD HH:mm:ss',
  emptyText = '-'
) {
  const parsed = parseUtc(input)
  if (!parsed || !parsed.isValid()) return emptyText
  return parsed.local().format(format)
}

export function toUtcPayload(input?: string | number | Date | null) {
  if (!input) return undefined
  const parsed = dayjs(input)
  return parsed.isValid() ? parsed.utc().format('YYYY-MM-DDTHH:mm:ss[Z]') : undefined
}

function toUtcRangeBoundary(input: unknown, end: boolean) {
  if (typeof input === 'string' && DATE_ONLY_PATTERN.test(input)) {
    const parsed = dayjs(input)
    if (!parsed.isValid()) return undefined
    const boundary = end ? parsed.endOf('day') : parsed.startOf('day')
    return boundary.utc().format('YYYY-MM-DDTHH:mm:ss.SSS[Z]')
  }
  return toUtcPayload(input as string | number | Date | null)
}

export function withUtcDateRange<T extends Record<string, unknown>>(
  query: T,
  dateRange?: unknown[],
  beginKey = 'beginTime',
  endKey = 'endTime'
) {
  const params: Record<string, unknown> = { ...query }
  const range = Array.isArray(dateRange) ? dateRange : []
  if (range.length === 2) {
    params[beginKey] = toUtcRangeBoundary(range[0], false)
    params[endKey] = toUtcRangeBoundary(range[1], true)
  }
  return params as T & Record<string, unknown>
}

export function withUtcDateRangeParams<T extends Record<string, unknown>>(
  query: T,
  dateRange?: unknown[],
  beginKey = 'beginTime',
  endKey = 'endTime'
) {
  const existingParams = (query as { params?: Record<string, unknown> }).params
  const params: T & { params: Record<string, unknown> } = { ...query, params: { ...(existingParams || {}) } }
  const range = Array.isArray(dateRange) ? dateRange : []
  if (range.length === 2) {
    params.params[beginKey] = toUtcRangeBoundary(range[0], false)
    params.params[endKey] = toUtcRangeBoundary(range[1], true)
  }
  return params
}

export function currentTimeZone() {
  return dayjs.tz.guess()
}
