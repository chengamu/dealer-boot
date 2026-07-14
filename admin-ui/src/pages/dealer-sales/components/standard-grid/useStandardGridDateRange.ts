import { ref } from 'vue'

function formatDate(value: Date) {
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function currentMonthDateRange() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  return [formatDate(start), formatDate(now)]
}

export function useStandardGridDateRange() {
  const dateRange = ref<string[]>(currentMonthDateRange())

  function resetDateRange() {
    dateRange.value = currentMonthDateRange()
  }

  return {
    dateRange,
    resetDateRange
  }
}
