import type { IncomeHistoryItem } from '../../types'

export type RhythmPoint = IncomeHistoryItem & {
  x: number
  y: number
}

export const flexRangeChart = {
  width: 720,
  centerY: 138,
  startX: 52,
  endX: 668,
} as const

function clamp(value: number, minimum: number, maximum: number) {
  return Math.min(Math.max(value, minimum), maximum)
}

function smoothPath(points: RhythmPoint[]) {
  if (points.length === 0) return ''

  return points.slice(1).reduce((path, point, index) => {
    const previous = points[index]
    const controlX = (previous.x + point.x) / 2
    return `${path} C ${controlX} ${previous.y}, ${controlX} ${point.y}, ${point.x} ${point.y}`
  }, `M ${points[0].x} ${points[0].y}`)
}

export function buildIncomeRhythm(incomeHistory: IncomeHistoryItem[], monthlyReference: number) {
  const chronologicalIncome = [...incomeHistory].sort((first, second) => first.month.localeCompare(second.month))

  if (chronologicalIncome.length === 0) {
    return { points: [] as RhythmPoint[], path: '', referenceY: flexRangeChart.centerY }
  }

  const values = chronologicalIncome.map((item) => item.amount)
  const minimum = Math.min(...values)
  const maximum = Math.max(...values)
  const range = maximum - minimum
  const relativeSpread = maximum > 0 ? range / maximum : 0
  const amplitude = range === 0 ? 0 : 12 + 42 * clamp(relativeSpread / 0.5, 0, 1)
  const interval = chronologicalIncome.length > 1
    ? (flexRangeChart.endX - flexRangeChart.startX) / (chronologicalIncome.length - 1)
    : 0

  const yForValue = (value: number) => {
    if (range === 0) return flexRangeChart.centerY
    const normalizedValue = clamp((value - minimum) / range, 0, 1)
    return flexRangeChart.centerY + (0.5 - normalizedValue) * amplitude * 2
  }

  const points = chronologicalIncome.map((item, index) => ({
    ...item,
    x: chronologicalIncome.length === 1
      ? flexRangeChart.width / 2
      : flexRangeChart.startX + interval * index,
    y: yForValue(item.amount),
  }))

  return {
    points,
    path: smoothPath(points),
    referenceY: yForValue(monthlyReference),
  }
}
