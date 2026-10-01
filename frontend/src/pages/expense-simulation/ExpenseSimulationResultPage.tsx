import { useNavigate } from 'react-router-dom'
import { RotateCcw } from 'lucide-react'
import { ButtonLink } from '../../components/common/ButtonLink'
import { SimulationMarginComparison } from '../../components/expense-simulation/SimulationMarginComparison'
import { RecommendationsSection } from '../../components/financial-analysis/RecommendationsSection'
import { useExpenseSimulation } from '../../hooks/useExpenseSimulation'
import { ImpactLevel, type ExpenseSimulationViewModel, type FinancialMetrics, type ImpactLevel as ImpactLevelValue } from '../../types'
import { financialProfileDetails, formatCurrency, formatPercentage, formatProbability } from '../../utils/financialAnalysisPresentation'
import '../../styles/results.css'

const metrics = (current: FinancialMetrics, projected: FinancialMetrics) => [
  { label: 'Renda média', current: current.averageIncome, projected: projected.averageIncome, format: formatCurrency },
  { label: 'Variação da renda', current: current.incomeVariationCoefficientPercentage, projected: projected.incomeVariationCoefficientPercentage, format: formatPercentage },
  { label: 'Nível de endividamento', current: current.debtRatioPercentage, projected: projected.debtRatioPercentage, format: formatPercentage },
  { label: 'Comprometimento fixo', current: current.fixedCommitmentPercentage, projected: projected.fixedCommitmentPercentage, format: formatPercentage },
]

const impactLevelLabels: Record<ImpactLevelValue, string> = {
  [ImpactLevel.LOW]: 'Baixo',
  [ImpactLevel.MODERATE]: 'Moderado',
  [ImpactLevel.HIGH]: 'Alto',
}

export function ExpenseSimulationResultPage() {
  const { result, clearResult }: { result: ExpenseSimulationViewModel | null; clearResult: () => void } = useExpenseSimulation()
  const navigate = useNavigate()

  if (!result) {
    return (
      <section className="empty-result">
        <span className="eyebrow">Resultado indisponível</span>
        <h1>Realize uma simulação primeiro</h1>
        <p>Não há uma simulação disponível nesta sessão.</p>
        <div className="result-actions">
          <ButtonLink to="/expense-simulation">Ir para a simulação</ButtonLink>
          <ButtonLink to="/" variant="secondary">Voltar para o início</ButtonLink>
        </div>
      </section>
    )
  }

  const currentProfile = financialProfileDetails[result.currentProfile]
  const projectedProfile = financialProfileDetails[result.projectedProfile]
  const currentProbabilityPercentage = Math.round(result.currentProbability * 100)
  const projectedProbabilityPercentage = Math.round(result.projectedProbability * 100)
  const changeDescription = result.financialHealthWorsened
    ? `O cenário passa de ${currentProfile.name} para ${projectedProfile.name}. O resultado indica piora e merece atenção.`
    : result.profileChanged
      ? `O cenário passa de ${currentProfile.name} para ${projectedProfile.name}, sem indicação de piora da saúde financeira.`
      : 'A projeção mantém o perfil atual.'
  const startAgain = () => {
    clearResult()
    navigate('/expense-simulation')
  }

  return (
    <div className="result-page result-page--aligned result-page--simulation fade-slide-up">
      <header className="page-heading">
        <span className="eyebrow">Resultado da simulação</span>
        <h1>Comparação dos cenários</h1>
        <p>Veja o possível impacto demonstrativo da despesa informada.</p>
      </header>

      <aside className="demo-notice">
        <strong>Resultado de caráter informativo.</strong>
        <p>Use a comparação dos cenários como apoio antes de assumir uma nova despesa.</p>
      </aside>

      <section className="expense-summary" aria-labelledby="expense-summary-title">
        <div>
          <span>Despesa simulada</span>
          <h2 id="expense-summary-title">{result.simulatedExpense.description}</h2>
        </div>
        <dl>
          <div><dt>Valor total</dt><dd>{formatCurrency(result.simulatedExpense.totalAmount)}</dd></div>
          <div><dt>Parcelas</dt><dd>{result.simulatedExpense.installments}</dd></div>
          <div><dt>Valor mensal</dt><dd>{formatCurrency(result.simulatedExpense.monthlyAmount)}</dd></div>
        </dl>
      </section>

      <SimulationMarginComparison
        currentMetrics={result.currentMetrics}
        projectedMetrics={result.projectedMetrics}
        expenseDescription={result.simulatedExpense.description}
        monthlyInstallmentAmount={result.monthlyInstallmentAmount}
      />

      <section className="scenario-comparison" aria-labelledby="scenario-title">
        <div className="result-section-heading">
          <span className="result-section-kicker">Impacto no perfil</span>
          <h2 id="scenario-title">Perfil atual e projetado</h2>
          <p>A probabilidade acompanha cada perfil retornado pela simulação.</p>
        </div>
        <div className="scenario-grid">
          <article className="scenario-card">
            <span>Cenário atual</span>
            <h3>{currentProfile.name}</h3>
            <strong>{formatProbability(result.currentProbability)}</strong>
            <p>probabilidade do perfil</p>
            <div className="scenario-probability" role="progressbar" aria-label={`Probabilidade do perfil atual: ${currentProbabilityPercentage}%`} aria-valuemin={0} aria-valuemax={100} aria-valuenow={currentProbabilityPercentage}>
              <span style={{ width: `${currentProbabilityPercentage}%` }} />
            </div>
          </article>
          <span className="scenario-connector" aria-hidden="true">→</span>
          <article className="scenario-card projected">
            <span>Cenário projetado</span>
            <h3>{projectedProfile.name}</h3>
            <strong>{formatProbability(result.projectedProbability)}</strong>
            <p>probabilidade do perfil</p>
            <div className="scenario-probability" role="progressbar" aria-label={`Probabilidade do perfil projetado: ${projectedProbabilityPercentage}%`} aria-valuemin={0} aria-valuemax={100} aria-valuenow={projectedProbabilityPercentage}>
              <span style={{ width: `${projectedProbabilityPercentage}%` }} />
            </div>
          </article>
        </div>
        <aside className={`change-summary ${result.financialHealthWorsened ? 'change-worse' : ''}`}>
          <div>
            <strong>{result.profileChanged ? 'Mudança de perfil identificada' : 'Perfil mantido'}</strong>
            <span>{changeDescription}</span>
          </div>
          <span className="impact-level">Impacto: {impactLevelLabels[result.impactLevel]}</span>
        </aside>
      </section>

      <section className="result-section comparison-section" aria-labelledby="comparison-title">
        <div className="result-section-heading">
          <span className="result-section-kicker">Impacto nos indicadores</span>
          <h2 id="comparison-title">Indicadores atuais e projetados</h2>
          <p>A diferença mostra a variação do cenário projetado.</p>
        </div>
        <div className="comparison-grid">
          {metrics(result.currentMetrics, result.projectedMetrics).map((metric) => {
            const difference = metric.projected - metric.current
            const direction = difference > 0 ? 'Aumento' : difference < 0 ? 'Redução' : 'Estável'
            return (
              <article className="comparison-card" key={metric.label}>
                <h3>{metric.label}</h3>
                <dl>
                  <div><dt>Atual</dt><dd>{metric.format(metric.current)}</dd></div>
                  <div><dt>Projetado</dt><dd>{metric.format(metric.projected)}</dd></div>
                  <div><dt>Diferença</dt><dd>{direction}: {metric.format(Math.abs(difference))}</dd></div>
                </dl>
              </article>
            )
          })}
        </div>
      </section>

      <RecommendationsSection recommendations={result.recommendations} />

      <nav className="result-actions" aria-label="Ações do resultado">
        <button className="button button-primary" type="button" onClick={startAgain}>
          <span className="button-content">Fazer nova simulação<RotateCcw className="button-icon" size={17} aria-hidden="true" /></span>
        </button>
        <ButtonLink to="/analysis/result" variant="secondary">Ver análise atual</ButtonLink>
        <ButtonLink to="/" variant="secondary">Voltar para o início</ButtonLink>
      </nav>
    </div>
  )
}
