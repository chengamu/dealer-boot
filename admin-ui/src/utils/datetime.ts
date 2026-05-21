import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)

const UTC_PATTERN = /(?:Z|[+-]\d{2}:?\d{2})$/

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

export function currentTimeZone() {
  return dayjs.tz.guess()
}
