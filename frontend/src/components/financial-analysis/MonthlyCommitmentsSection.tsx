import type { FinancialAnalysisFormErrors } from '../../types'

type Props = {
  monthlyDebtPayments: string
  otherFixedMonthlyExpenses: string
  errors: Pick<FinancialAnalysisFormErrors, 'monthlyDebtPayments' | 'otherFixedMonthlyExpenses'>
  onChange: (field: 'monthlyDebtPayments' | 'otherFixedMonthlyExpenses', value: string) => void
}

export function MonthlyCommitmentsSection({ monthlyDebtPayments, otherFixedMonthlyExpenses, errors, onChange }: Props) {
  return (
    <section className="form-section" aria-labelledby="commitments-title">
      <div className="form-section-heading"><div><span className="step-number">4</span><h2 id="commitments-title">Seus compromissos mensais</h2></div><p>Esses valores ajudam a estimar quanto da sua referência mensal já está comprometido.</p></div>
      <div className="two-column-fields">
        <div className="field-group">
          <label htmlFor="monthlyDebtPayments">Parcelas e dívidas mensais</label>
          <span className="field-help" id="monthlyDebtPayments-help">Informe o total mensal de empréstimos, financiamentos e compras parceladas.</span>
          <div className="money-input"><span aria-hidden="true">R$</span><input id="monthlyDebtPayments" type="number" min="0" step="0.01" inputMode="decimal" placeholder="Ex.: 500,00" value={monthlyDebtPayments} aria-invalid={Boolean(errors.monthlyDebtPayments)} aria-describedby={`monthlyDebtPayments-help${errors.monthlyDebtPayments ? ' monthlyDebtPayments-error' : ''}`} onChange={(event) => onChange('monthlyDebtPayments', event.target.value)} /></div>
          {errors.monthlyDebtPayments && <span className="field-error" id="monthlyDebtPayments-error">{errors.monthlyDebtPayments}</span>}
        </div>
        <div className="field-group">
          <label htmlFor="otherFixedMonthlyExpenses">Outras despesas fixas mensais</label>
          <span className="field-help" id="otherFixedMonthlyExpenses-help">Informe o total mensal de despesas fixas, como aluguel, internet, energia e assinaturas.</span>
          <div className="money-input"><span aria-hidden="true">R$</span><input id="otherFixedMonthlyExpenses" type="number" min="0" step="0.01" inputMode="decimal" placeholder="Ex.: 1.200,00" value={otherFixedMonthlyExpenses} aria-invalid={Boolean(errors.otherFixedMonthlyExpenses)} aria-describedby={`otherFixedMonthlyExpenses-help${errors.otherFixedMonthlyExpenses ? ' otherFixedMonthlyExpenses-error' : ''}`} onChange={(event) => onChange('otherFixedMonthlyExpenses', event.target.value)} /></div>
          {errors.otherFixedMonthlyExpenses && <span className="field-error" id="otherFixedMonthlyExpenses-error">{errors.otherFixedMonthlyExpenses}</span>}
        </div>
      </div>
    </section>
  )
}
