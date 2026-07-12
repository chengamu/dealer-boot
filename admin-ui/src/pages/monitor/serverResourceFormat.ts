const metricNumberFormatter = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2
})

function formatMetricNumber(value: number) {
  return metricNumberFormatter.format(value)
}

export function formatBytes(value?: number | null) {
  if (value === null || value === undefined || value < 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let amount = value
  let index = 0
  while (amount >= 1024 && index < units.length - 1) {
    amount /= 1024
    index += 1
  }
  return `${formatMetricNumber(amount)} ${units[index]}`
}

export function formatPercent(value?: number | null) {
  return `${formatMetricNumber(value ?? 0)}%`
}

export function clampPercent(value?: number | null) {
  return Math.min(100, Math.max(0, value ?? 0))
}

export function usageColor(value?: number | null) {
  const percent = value ?? 0
  if (percent >= 90) return '#f56c6c'
  if (percent >= 75) return '#e6a23c'
  return '#1677ff'
}

export function formatDuration(seconds: number | undefined, labels: { day: string; hour: string; minute: string }) {
  const value = Math.max(0, Math.floor(seconds ?? 0))
  const days = Math.floor(value / 86400)
  const hours = Math.floor((value % 86400) / 3600)
  const minutes = Math.floor((value % 3600) / 60)
  const parts = []
  if (days) parts.push(`${days}${labels.day}`)
  if (hours || days) parts.push(`${hours}${labels.hour}`)
  parts.push(`${minutes}${labels.minute}`)
  return parts.join(' ')
}
