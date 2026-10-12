import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowRight, LoaderCircle } from 'lucide-react'
import { ApplicationErrorMessage } from '../../components/common/ApplicationErrorMessage'
import { ButtonLink } from '../../components/common/ButtonLink'
import { NewExpenseSection } from '../../components/expense-simulation/NewExpenseSection'
import { useExpenseSimulation } from '../../hooks/useExpenseSimulation'
import { useFinancialAnalysis } from '../../hooks/useFinancialAnalysis'
import { mapExpenseSimulationFormToRequest, mapExpenseSimulationResponseToViewModel } from '../../mappers/expenseSimulationMapper'
import { simulateExpense } from '../../services/expenseSimulationService'
import type { ExpenseSimulationFormErrors, ExpenseSimulationFormState } from '../../types'
import { resolveApplicationError, type ApplicationError } from '../../utils/applicationError'
import { financialProfileDetails, formatCurrency, formatPercentage, formatProbability } from '../../utils/financialAnalysisPresentation'
import { hasExpenseSimulationFormErrors, validateExpenseSimulationForm } from '../../utils/expenseSimulationValidation'
import '../../styles/forms.css'

const initialState: ExpenseSimulationFormState = { expenseDescription: '', totalAmount: '', installments: '1' }

export function ExpenseSimulationPage() {
  const navigate = useNavigate()
  const { request: analysisRequest, result: analysis } = useFinancialAnalysis()
  const { setResult, clearResult } = useExpenseSimulation()
  const [formState, setFormState] = useState(initialState)
  const [errors, setErrors] = useState<ExpenseSimulationFormErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<ApplicationError | null>(null)

  if (!analysisRequest || !analysis) {
    return (
      <div className="application-page application-page--empty">
        <section className="empty-result">
          <span className="eyebrow">Análise necessária</span>
          <h1>Faça sua análise antes de simular</h1>
          <p>A simulação usa o resultado da sua análise financeira atual para apresentar a comparação entre os cenários.</p>
          <div className="result-actions">
            <ButtonLink to="/analysis">Fazer análise financeira</ButtonLink>
            <ButtonLink to="/" variant="secondary">Voltar para o início</ButtonLink>
          </div>
        </section>
      </div>
    )
  }

  const profile = financialProfileDetails[analysis.financialProfile]
  const updateField = (field: keyof ExpenseSimulationFormState, value: string) => setFormState((current) => ({ ...current, [field]: value }))

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (isSubmitting) return
    const nextErrors = validateExpenseSimulationForm(formState)
    setErrors(nextErrors)
    setSubmitError(null)
    if (hasExpenseSimulationFormErrors(nextErrors)) return
    setIsSubmitting(true)
    clearResult()
    try {
      const request = mapExpenseSimulationFormToRequest(analysisRequest, formState)
      const response = await simulateExpense(request)
      setResult(mapExpenseSimulationResponseToViewModel(response))
      navigate('/expense-simulation/result')
    } catch (error) {
      setSubmitError(resolveApplicationError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const metrics = [
    { label: 'Renda média', value: formatCurrency(analysis.metrics.averageIncome) },
    { label: 'Endividamento', value: formatPercentage(analysis.metrics.debtRatioPercentage) },
    { label: 'Comprometimento fixo', value: formatPercentage(analysis.metrics.fixedCommitmentPercentage) },
  ]

  return (
    <div className="analysis-page application-page application-page--simulation">
      <header className="page-heading workflow-heading">
        <span className="eyebrow">Segunda etapa</span>
        <h1>Simule uma nova despesa</h1>
        <p>Partimos da análise financeira já realizada. Agora, informe somente os dados da compra que deseja avaliar.</p>
      </header>

      <section className="analysis-recap" aria-labelledby="analysis-recap-title">
        <div className="analysis-recap-profile">
          <div className="analysis-recap-heading"><span aria-hidden="true">1</span><strong>Sua análise atual</strong></div>
          <h2 id="analysis-recap-title">{profile.name}</h2>
          <p>{formatProbability(analysis.probability)} de probabilidade do perfil</p>
        </div>
        <dl>{metrics.map((metric) => <div key={metric.label}><dt>{metric.label}</dt><dd>{metric.value}</dd></div>)}</dl>
      </section>

      <aside className="prototype-notice">
        <strong>Simulação de caráter informativo</strong>
        <p>Os resultados ajudam a comparar o cenário atual com o impacto projetado da nova despesa.</p>
      </aside>

      <form className="analysis-form" onSubmit={submit} noValidate>
        <NewExpenseSection description={formState.expenseDescription} totalAmount={formState.totalAmount} installments={formState.installments} errors={errors} onChange={updateField} />
        {submitError && <ApplicationErrorMessage error={submitError} />}
        <div className="form-footer">
          <p aria-live="polite">{isSubmitting ? 'Sua simulação está sendo processada.' : 'A análise atual permanecerá salva durante a simulação.'}</p>
          <div>
            <ButtonLink to="/analysis/result" variant="secondary">Voltar ao resultado</ButtonLink>
            <button className="button button-primary" type="submit" disabled={isSubmitting}>
              <span className="button-content">{isSubmitting ? <><LoaderCircle className="loading-spinner" size={17} aria-hidden="true" />Simulando...</> : <>Simular nova despesa<ArrowRight className="button-icon" size={17} aria-hidden="true" /></>}</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
