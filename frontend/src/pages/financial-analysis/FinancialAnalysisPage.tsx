import { useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowRight, LoaderCircle } from 'lucide-react'
import { ApplicationErrorMessage } from '../../components/common/ApplicationErrorMessage'
import { ButtonLink } from '../../components/common/ButtonLink'
import { IncomeHistorySection } from '../../components/financial-analysis/IncomeHistorySection'
import { MonthlyCommitmentsSection } from '../../components/financial-analysis/MonthlyCommitmentsSection'
import { SavingFrequencyField } from '../../components/financial-analysis/SavingFrequencyField'
import { TransactionsSection } from '../../components/financial-analysis/TransactionsSection'
import { useFinancialAnalysis } from '../../hooks/useFinancialAnalysis'
import { mapFinancialAnalysisFormToRequest } from '../../mappers/financialAnalysisMapper'
import { analyzeFinancialSituation } from '../../services/financialAnalysisService'
import type { FinancialAnalysisFormErrors, FinancialAnalysisFormState } from '../../types'
import { resolveApplicationError, type ApplicationError } from '../../utils/applicationError'
import { hasFormErrors, validateFinancialAnalysisForm } from '../../utils/financialAnalysisValidation'
import '../../styles/forms.css'

const createInitialIncomeHistory = () => [1, 2, 3].map((id) => ({ id: String(id), month: '', amount: '' }))
const emptyErrors: FinancialAnalysisFormErrors = { incomeHistory: {}, transactions: {} }

export function FinancialAnalysisPage() {
  const nextIncomeId = useRef(4)
  const nextTransactionId = useRef(2)
  const navigate = useNavigate()
  const { setAnalysis, clearResult } = useFinancialAnalysis()
  const [formState, setFormState] = useState<FinancialAnalysisFormState>({ incomeHistory: createInitialIncomeHistory(), savingFrequency: '', transactions: [{ id: '1', description: '', amount: '', date: '', type: '' }], monthlyDebtPayments: '', otherFixedMonthlyExpenses: '' })
  const [errors, setErrors] = useState(emptyErrors)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<ApplicationError | null>(null)

  const addIncomeMonth = () => setFormState((current) => current.incomeHistory.length >= 6 ? current : { ...current, incomeHistory: [...current.incomeHistory, { id: String(nextIncomeId.current++), month: '', amount: '' }] })
  const removeIncomeMonth = (id: string) => setFormState((current) => current.incomeHistory.length <= 3 ? current : { ...current, incomeHistory: current.incomeHistory.filter((item) => item.id !== id) })
  const updateIncomeMonth = (id: string, field: 'month' | 'amount', value: string) => setFormState((current) => ({ ...current, incomeHistory: current.incomeHistory.map((item) => item.id === id ? { ...item, [field]: value } : item) }))
  const addTransaction = () => setFormState((current) => ({ ...current, transactions: [...current.transactions, { id: String(nextTransactionId.current++), description: '', amount: '', date: '', type: '' }] }))
  const removeTransaction = (id: string) => setFormState((current) => current.transactions.length === 1 ? current : { ...current, transactions: current.transactions.filter((item) => item.id !== id) })
  const updateTransaction = (id: string, field: 'description' | 'amount' | 'date' | 'type', value: string) => setFormState((current) => ({ ...current, transactions: current.transactions.map((item) => item.id === id ? { ...item, [field]: value } : item) }))
  const updateMonthlyCommitment = (field: 'monthlyDebtPayments' | 'otherFixedMonthlyExpenses', value: string) => setFormState((current) => ({ ...current, [field]: value }))

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (isSubmitting) return
    const nextErrors = validateFinancialAnalysisForm(formState)
    setErrors(nextErrors)
    setSubmitError(null)
    if (hasFormErrors(nextErrors)) return
    setIsSubmitting(true)
    clearResult()
    try {
      const request = mapFinancialAnalysisFormToRequest(formState)
      const result = await analyzeFinancialSituation(request)
      setAnalysis(request, result)
      navigate('/analysis/result')
    } catch (error) {
      setSubmitError(resolveApplicationError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="analysis-page application-page application-page--analysis">
      <header className="page-heading workflow-heading">
        <span className="eyebrow">Análise financeira</span>
        <h1>Vamos conhecer melhor a sua renda</h1>
        <p>Preencha as informações para gerar uma análise de caráter informativo.</p>
      </header>

      <form className="analysis-form" onSubmit={submit} noValidate>
        <IncomeHistorySection items={formState.incomeHistory} errors={errors.incomeHistory} onAdd={addIncomeMonth} onChange={updateIncomeMonth} onRemove={removeIncomeMonth} />
        <SavingFrequencyField value={formState.savingFrequency} error={errors.savingFrequency} onChange={(savingFrequency) => setFormState((current) => ({ ...current, savingFrequency }))} />
        <TransactionsSection items={formState.transactions} errors={errors.transactions} onAdd={addTransaction} onChange={updateTransaction} onRemove={removeTransaction} />
        <MonthlyCommitmentsSection monthlyDebtPayments={formState.monthlyDebtPayments} otherFixedMonthlyExpenses={formState.otherFixedMonthlyExpenses} errors={errors} onChange={updateMonthlyCommitment} />
        {submitError && <ApplicationErrorMessage error={submitError} />}
        <div className="form-footer">
          <p aria-live="polite">{isSubmitting ? 'Sua análise está sendo processada.' : 'O resultado será mantido somente durante esta sessão.'}</p>
          <div>
            <ButtonLink to="/" variant="secondary">Voltar ao início</ButtonLink>
            <button className="button button-primary" type="submit" disabled={isSubmitting}>
              <span className="button-content">{isSubmitting ? <><LoaderCircle className="loading-spinner" size={17} aria-hidden="true" />Analisando...</> : <>Analisar situação financeira<ArrowRight className="button-icon" size={17} aria-hidden="true" /></>}</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
