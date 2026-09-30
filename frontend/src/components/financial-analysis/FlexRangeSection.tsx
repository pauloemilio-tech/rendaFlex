import { useId } from 'react'
import type { FinancialMetrics, IncomeHistoryItem } from '../../types'
import { formatCurrency, formatPercentage } from '../../utils/financialAnalysisPresentation'
import { buildIncomeRhythm, flexRangeChart } from './flexRangeRhythm'
import './flex-range-section.css'

type FlexRangeSectionProps = {
  incomeHistory: IncomeHistoryItem[]
  metrics: FinancialMetrics
}

function clamp(value: number, minimum: number, maximum: number) {
  return Math.min(Math.max(value, minimum), maximum)
}

function formatMonth(value: string, includeYear = false) {
  const match = /^(\d{4})-(\d{2})$/.exec(value)
  if (!match) return value

  const [, year, month] = match
  return new Intl.DateTimeFormat('pt-BR', {
    month: 'short',
    ...(includeYear ? { year: 'numeric' } : {}),
    timeZone: 'UTC',
  }).format(new Date(Date.UTC(Number(year), Number(month) - 1, 1)))
}

export function FlexRangeSection({ incomeHistory, metrics }: FlexRangeSectionProps) {
  const rawId = useId().replace(/:/g, '')
  const titleId = `flex-range-title-${rawId}`
  const chartTitleId = `flex-range-chart-title-${rawId}`
  const chartDescriptionId = `flex-range-chart-description-${rawId}`
  const gradientId = `flex-range-gradient-${rawId}`
  const { points, path, referenceY } = buildIncomeRhythm(incomeHistory, metrics.monthlyReference)
  const commitmentPercentage = metrics.fixedCommitmentPercentage
  const visibleCommitmentPercentage = clamp(commitmentPercentage, 0, 100)
  const hasNegativeMargin = metrics.availableMargin < 0

  return (
    <section className={`flex-range-section${hasNegativeMargin ? ' has-negative-margin' : ''}`} aria-labelledby={titleId}>
      <header className="flex-range-heading">
        <p>Sua Faixa Flex</p>
        <h2 id={titleId}>Como sua renda e seus compromissos se encontram</h2>
        <p>A Faixa Flex usa seu histórico recente para mostrar o ritmo da renda e quanto da sua referência mensal já está comprometido.</p>
      </header>

      <div className="flex-range-layout">
        <article className="flex-income-rhythm" aria-labelledby={`${titleId}-rhythm`}>
          <div className="flex-range-subheading">
            <div>
              <span>Ritmo recente</span>
              <h3 id={`${titleId}-rhythm`}>Sua renda mês a mês</h3>
            </div>
            <small>{points.length} {points.length === 1 ? 'mês' : 'meses'} informados</small>
          </div>

          {points.length > 0 ? (
            <>
              <figure className="flex-rhythm-figure">
                <svg viewBox={`0 0 ${flexRangeChart.width} 250`} role="img" aria-labelledby={`${chartTitleId} ${chartDescriptionId}`}>
                  <title id={chartTitleId}>Ritmo da renda recente</title>
                  <desc id={chartDescriptionId}>A faixa sobe nos meses de renda relativamente maior e desce nos meses de renda relativamente menor. A linha tracejada representa a referência mensal. Os valores exatos estão listados abaixo.</desc>
                  <defs>
                    <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0" stopColor="#315b8c" />
                      <stop offset=".58" stopColor="#557fad" />
                      <stop offset="1" stopColor="#f2765e" />
                    </linearGradient>
                  </defs>
                  <line className="flex-reference-line" x1={flexRangeChart.startX} x2={flexRangeChart.endX} y1={referenceY} y2={referenceY} />
                  <text className="flex-reference-label" x={flexRangeChart.endX} y={referenceY - 14} textAnchor="end">referência mensal</text>
                  <path className="flex-rhythm-shadow" d={path} />
                  <path className="flex-rhythm-path" d={path} stroke={`url(#${gradientId})`} />
                  {points.map((point) => (
                    <g className="flex-rhythm-point" key={point.month} transform={`translate(${point.x} ${point.y})`}>
                      <circle r="8" />
                      <text y="52" textAnchor="middle">{formatMonth(point.month)}</text>
                    </g>
                  ))}
                </svg>
                <figcaption>Ritmo relativo ao seu próprio histórico; os valores abaixo são os dados reais informados.</figcaption>
              </figure>

              <ol className="flex-income-values" aria-label="Histórico de renda informado">
                {points.map((point) => (
                  <li key={point.month}>
                    <span>{formatMonth(point.month, true)}</span>
                    <strong>{formatCurrency(point.amount)}</strong>
                  </li>
                ))}
              </ol>
            </>
          ) : (
            <p className="flex-range-empty">O histórico de renda desta análise não está disponível na sessão.</p>
          )}
        </article>

        <article className="flex-monthly-picture" aria-labelledby={`${titleId}-picture`}>
          <div className="flex-range-subheading">
            <div>
              <span>Quadro mensal</span>
              <h3 id={`${titleId}-picture`}>A referência de agora</h3>
            </div>
          </div>

          <dl className="flex-monthly-values">
            <div>
              <dt>Referência mensal</dt>
              <dd>{formatCurrency(metrics.monthlyReference)}</dd>
            </div>
            <div>
              <dt>Compromissos mensais</dt>
              <dd>{formatCurrency(metrics.monthlyCommitments)}</dd>
              <small>{formatPercentage(commitmentPercentage)} da referência</small>
            </div>
            <div className="flex-available-margin">
              <dt>Margem disponível</dt>
              <dd>{formatCurrency(metrics.availableMargin)}</dd>
              <small>após os compromissos mensais informados</small>
            </div>
          </dl>

          <div className="flex-composition">
            <div
              className="flex-composition-bar"
              role="img"
              aria-label={`Os compromissos representam ${formatPercentage(commitmentPercentage)} da referência mensal.`}
            >
              <span style={{ width: `${visibleCommitmentPercentage}%` }} />
            </div>
            <div className="flex-composition-legend" aria-hidden="true">
              <span><i className="is-commitment" /> Compromissos</span>
              <span><i className="is-margin" /> Margem</span>
            </div>
          </div>

          {hasNegativeMargin && (
            <p className="flex-margin-warning">Os compromissos informados ultrapassam a referência mensal.</p>
          )}
        </article>
      </div>
    </section>
  )
}
