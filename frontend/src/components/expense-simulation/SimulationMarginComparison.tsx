import type { FinancialMetrics } from '../../types'
import { formatCurrency } from '../../utils/financialAnalysisPresentation'
import './simulation-margin-comparison.css'

type SimulationMarginComparisonProps = {
  currentMetrics: FinancialMetrics
  projectedMetrics: FinancialMetrics
  expenseDescription: string
  monthlyInstallmentAmount: number
}

function formatSignedCurrency(value: number) {
  if (value === 0) return formatCurrency(0)
  return `${value > 0 ? '+' : '−'} ${formatCurrency(Math.abs(value))}`
}

function formatMarginCurrency(value: number) {
  return value < 0 ? `− ${formatCurrency(Math.abs(value))}` : formatCurrency(value)
}

export function SimulationMarginComparison({
  currentMetrics,
  projectedMetrics,
  expenseDescription,
  monthlyInstallmentAmount,
}: SimulationMarginComparisonProps) {
  const marginDifference = projectedMetrics.availableMargin - currentMetrics.availableMargin
  const currentMarginIsNegative = currentMetrics.availableMargin < 0
  const projectedMarginIsNegative = projectedMetrics.availableMargin < 0
  const referenceIsMaintained = currentMetrics.monthlyReference === projectedMetrics.monthlyReference

  return (
    <section className="simulation-margin" aria-labelledby="simulation-margin-title">
      <header className="simulation-margin__heading">
        <span className="simulation-margin__eyebrow">Efeito da nova despesa</span>
        <h2 id="simulation-margin-title">Como a parcela altera sua margem mensal</h2>
        <p>Acompanhe o caminho entre o cenário atual e a projeção após incluir a nova parcela.</p>
      </header>

      <ol className="simulation-margin__flow">
        <li className={`simulation-margin__stage simulation-margin__stage--current ${currentMarginIsNegative ? 'is-negative' : ''}`}>
          <span className="simulation-margin__label">Margem atual</span>
          <strong>{formatMarginCurrency(currentMetrics.availableMargin)}</strong>
          <p>Disponível antes da despesa simulada.</p>
          {currentMarginIsNegative && (
            <small>Os compromissos atuais já ultrapassam a referência mensal.</small>
          )}
        </li>

        <li className="simulation-margin__stage simulation-margin__stage--installment">
          <span className="simulation-margin__connector" aria-hidden="true">→</span>
          <span className="simulation-margin__label">Nova parcela mensal</span>
          <strong>− {formatCurrency(Math.abs(monthlyInstallmentAmount))}</strong>
          <p>{expenseDescription}</p>
        </li>

        <li className={`simulation-margin__stage simulation-margin__stage--projected ${projectedMarginIsNegative ? 'is-negative' : ''}`}>
          <span className="simulation-margin__connector" aria-hidden="true">→</span>
          <span className="simulation-margin__label">Margem projetada</span>
          <strong>{formatMarginCurrency(projectedMetrics.availableMargin)}</strong>
          <p>Disponível após considerar a nova parcela.</p>
          {projectedMarginIsNegative && (
            <small>Com a nova parcela, os compromissos projetados ultrapassam a referência mensal.</small>
          )}
        </li>
      </ol>

      <div className="simulation-margin__context">
        <div className="simulation-margin__difference">
          <span>Variação observada na margem</span>
          <strong>{formatSignedCurrency(marginDifference)}</strong>
        </div>

        <dl>
          {referenceIsMaintained ? (
            <div>
              <dt>Referência mensal</dt>
              <dd>{formatCurrency(currentMetrics.monthlyReference)}</dd>
            </div>
          ) : (
            <>
              <div>
                <dt>Referência atual</dt>
                <dd>{formatCurrency(currentMetrics.monthlyReference)}</dd>
              </div>
              <div>
                <dt>Referência projetada</dt>
                <dd>{formatCurrency(projectedMetrics.monthlyReference)}</dd>
              </div>
            </>
          )}
          <div>
            <dt>Compromissos atuais</dt>
            <dd>{formatCurrency(currentMetrics.monthlyCommitments)}</dd>
          </div>
          <div>
            <dt>Compromissos projetados</dt>
            <dd>{formatCurrency(projectedMetrics.monthlyCommitments)}</dd>
          </div>
        </dl>
      </div>
    </section>
  )
}
