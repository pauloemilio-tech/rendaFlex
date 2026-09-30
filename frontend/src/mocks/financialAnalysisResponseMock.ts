import { FinancialProfile, RecommendationPriority, TransactionCategory, TransactionType } from '../types'
import type { FinancialAnalysisResponse } from '../types'

export const financialAnalysisResponseMock: FinancialAnalysisResponse = {
  financialProfile: FinancialProfile.UNDER_OBSERVATION,
  probability: 0.86,
  metrics: { averageIncome: 3450.75, incomeVariationCoefficientPercentage: 12.4, debtRatioPercentage: 31.8, fixedCommitmentPercentage: 27.35, monthlyReference: 3450.75, monthlyCommitments: 943.79, availableMargin: 2506.96 },
  classifiedTransactions: [
    { description: 'Pagamento de trabalho autônomo', amount: 900, date: '2026-07-02', type: TransactionType.INCOME },
    { description: 'Supermercado', amount: 386.5, date: '2026-07-08', type: TransactionType.EXPENSE, predictedCategory: TransactionCategory.FOOD, classificationProbability: 0.94 },
    { description: 'Transporte por aplicativo', amount: 52, date: '2026-07-12', type: TransactionType.EXPENSE, predictedCategory: TransactionCategory.TRANSPORT, classificationProbability: 0.91 },
    { description: 'Conta de internet', amount: 119.9, date: '2026-07-15', type: TransactionType.EXPENSE, predictedCategory: TransactionCategory.SERVICES, classificationProbability: 0.89 },
  ],
  categorySummary: { [TransactionCategory.FOOD]: 386.5, [TransactionCategory.TRANSPORT]: 52, [TransactionCategory.SERVICES]: 119.9 },
  categoryPercentages: { [TransactionCategory.FOOD]: 69.22, [TransactionCategory.TRANSPORT]: 9.31, [TransactionCategory.SERVICES]: 21.47 },
  recommendations: [
    { priority: RecommendationPriority.HIGH, message: 'Revise os compromissos fixos antes de assumir novas parcelas.' },
    { priority: RecommendationPriority.MEDIUM, message: 'Mantenha uma reserva para os meses de menor renda.' },
  ],
}
