interface SpanColumn {
  property?: string
}

interface SpanMethodArgs<T extends Record<string, unknown>> {
  row: T
  column: SpanColumn
  rowIndex: number
  columnIndex: number
}

interface SpanResult {
  rowspan: number
  colspan: number
}

export function getRowspanMethod<T extends Record<string, unknown>>(data: T[], rowspanArray: string[]) {
  const rowspanNumObject: Record<string, number[] | number> = {}

  rowspanArray.forEach((item) => {
    rowspanNumObject[item] = new Array(data.length).fill(1, 0, 1).fill(0, 1)
    rowspanNumObject[`${item}-index`] = 0
  })

  for (let i = 1; i < data.length; i++) {
    rowspanArray.forEach((key) => {
      const index = rowspanNumObject[`${key}-index`] as number
      const rows = rowspanNumObject[key] as number[]
      if (data[i][key] === data[i - 1][key]) {
        rows[index]++
      } else {
        rowspanNumObject[`${key}-index`] = i
        rows[i] = 1
      }
    })
  }

  return function spanMethod({ column, rowIndex }: SpanMethodArgs<T>): SpanResult {
    const property = column.property
    if (property && rowspanArray.includes(property)) {
      const rowspan = (rowspanNumObject[property] as number[])[rowIndex]
      if (rowspan > 0) return { rowspan, colspan: 1 }
      return { rowspan: 0, colspan: 0 }
    }
    return { rowspan: 1, colspan: 1 }
  }
}
