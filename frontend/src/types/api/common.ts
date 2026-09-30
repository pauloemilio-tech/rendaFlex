import type { RecommendationPriority } from '../enums/recommendationPriority'
import type { TransactionCategory } from '../enums/transactionCategory'
import type { TransactionType } from '../enums/transactionType'

export type IncomeHistoryItem = {
  month: string
  amount: number
}

export type TransactionInput = {
  description: string
  amount: number
  date: string
  type: TransactionType
}

export type ClassifiedTransaction = {
  description: string
  amount?: number
  date?: string
  type?: TransactionType
  predictedCategory?: TransactionCategory
  classificationProbability?: number
}

export type FinancialMetrics = {
  averageIncome: number
  incomeVariationCoefficientPercentage: number
  debtRatioPercentage: number
  fixedCommitmentPercentage: number
  monthlyReference: number
  monthlyCommitments: number
  availableMargin: number
}

export type Recommendation = {
  priority: RecommendationPriority
  message: string
}

export type CategorySummary = Partial<Record<TransactionCategory, number>>
export type CategoryPercentages = Partial<Record<TransactionCategory, number>>
