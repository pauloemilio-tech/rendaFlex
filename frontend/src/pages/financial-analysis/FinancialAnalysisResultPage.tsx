import { useNavigate } from 'react-router-dom'
import { ArrowRight, RotateCcw, ShieldCheck, TrendingUp, Wallet, AlertTriangle } from 'lucide-react'
import { ButtonLink } from '../../components/common/ButtonLink'
import { CategorySummarySection } from '../../components/financial-analysis/CategorySummarySection'
import { ClassifiedTransactionsSection } from '../../components/financial-analysis/ClassifiedTransactionsSection'
import { FlexRangeSection } from '../../components/financial-analysis/FlexRangeSection'
import { RecommendationsSection } from '../../components/financial-analysis/RecommendationsSection'
import { useFinancialAnalysis } from '../../hooks/useFinancialAnalysis'
import {
  financialProfileDetails,
  formatCurrency,
  formatPercentage,
  formatProbability,
} from '../../utils/financialAnalysisPresentation'
import '../../styles/results.css'

export function FinancialAnalysisResultPage() {
  const { request, result, clearResult } = useFinancialAnalysis()
  const navigate = useNavigate()

  if (!result) {
    return (
      <section className="empty-result">
        <span className="eyebrow">Resultado indisponível</span>
        <h1>Realize uma análise primeiro</h1>
        <p>Não há um resultado disponível nesta sessão. Preencha o formulário para gerar uma análise.</p>
        <div className="result-actions">
          <ButtonLink to="/analysis">Ir para a análise</ButtonLink>
          <ButtonLink to="/" variant="secondary">Voltar para o início</ButtonLink>
        </div>
      </section>
    )
  }

  const profile = financialProfileDetails[result.financialProfile]
  const metrics = [
    { label: 'Renda média', value: formatCurrency(result.metrics.averageIncome), icon: Wallet },
    { label: 'Variação da renda', value: formatPercentage(result.metrics.incomeVariationCoefficientPercentage), icon: TrendingUp },
    { label: 'Nível de endividamento', value: formatPercentage(result.metrics.debtRatioPercentage), icon: AlertTriangle },
    { label: 'Comprometimento fixo', value: formatPercentage(result.metrics.fixedCommitmentPercentage), icon: ShieldCheck },
  ]

  const startAgain = () => {
    clearResult()
    navigate('/analysis')
  }

  return (
    <div className="result-page result-page--aligned result-page--analysis fade-slide-up">
      <header className="page-heading">
        <span className="eyebrow">Resultado da análise</span>
        <h1>Seu perfil financeiro</h1>
        <p>Confira os dados retornados pela análise financeira desta sessão.</p>
      </header>

      <aside className="demo-notice">
        <strong>Resultado de caráter informativo.</strong>
        <p>Use os indicadores como apoio para compreender seu cenário financeiro.</p>
      </aside>

      <section className={`profile-summary profile-${result.financialProfile.toLowerCase()}`} aria-labelledby="profile-title">
        <div>
          <span>Perfil financeiro</span>
          <h2 id="profile-title">{profile.name}</h2>
          <p>{profile.description}</p>
        </div>
        <div className="probability">
          <strong>{formatProbability(result.probability)}</strong>
          <span>probabilidade do perfil</span>
          <div className={`probability-bar profile-${result.financialProfile.toLowerCase()}`} role="progressbar" aria-label="Probabilidade do perfil" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round(result.probability * 100)}>
            <span style={{ width: `${Math.round(result.probability * 100)}%` }} />
          </div>
        </div>
      </section>

      <FlexRangeSection incomeHistory={request?.incomeHistory ?? []} metrics={result.metrics} />

      <section className="metrics-section result-section" aria-labelledby="metrics-title">
        <div className="result-section-heading">
          <span className="result-section-kicker">Leitura de apoio</span>
          <h2 id="metrics-title">Indicadores financeiros</h2>
          <p>Quatro medidas complementares para compreender o cenário analisado.</p>
        </div>
        <div className="metrics-grid">
          {metrics.map((metric) => (
            <article className="metric-card" key={metric.label}>
              <span><metric.icon className="metric-icon" size={18} strokeWidth={1.8} aria-hidden="true" />{metric.label}</span>
              <strong>{metric.value}</strong>
            </article>
          ))}
        </div>
      </section>

      <section className="result-context-group" aria-labelledby="spending-context-title">
        <header className="result-chapter-heading">
          <span className="result-section-kicker">Contexto de gastos</span>
          <h2 id="spending-context-title">Como seus gastos aparecem</h2>
          <p>As categorias e transações complementam a leitura dos indicadores.</p>
        </header>
        <CategorySummarySection categorySummary={result.categorySummary} />
        <ClassifiedTransactionsSection transactions={result.classifiedTransactions} />
      </section>
      <RecommendationsSection recommendations={result.recommendations} />

      <nav className="result-actions" aria-label="Ações do resultado">
        <ButtonLink to="/expense-simulation" icon={<ArrowRight className="button-icon" size={17} aria-hidden="true" />}>Simular nova despesa</ButtonLink>
        <button className="button button-secondary" type="button" onClick={startAgain}><span className="button-content">Fazer nova análise<RotateCcw className="button-icon" size={17} aria-hidden="true" /></span></button>
        <ButtonLink to="/" variant="secondary">Voltar para o início</ButtonLink>
      </nav>
    </div>
  )
}
