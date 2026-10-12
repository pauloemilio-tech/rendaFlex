import type { IncomeHistoryErrors, IncomeHistoryFormItem } from '../../types'
import { IncomeHistoryItemFields } from './IncomeHistoryItemFields'

type Props = { items: IncomeHistoryFormItem[]; errors: IncomeHistoryErrors; onAdd: () => void; onChange: (id: string, field: 'month' | 'amount', value: string) => void; onRemove: (id: string) => void }
export function IncomeHistorySection({ items, errors, onAdd, onChange, onRemove }: Props) {
  return <section className="form-section" aria-labelledby="income-history-title"><div className="form-section-heading"><div><span className="step-number">1</span><h2 id="income-history-title">Sua renda recente</h2></div><p>Informe de 3 a 6 meses. Use o valor total recebido em cada mês.</p></div><div className="income-list">{items.map((item, index) => <IncomeHistoryItemFields key={item.id} item={item} index={index} canRemove={items.length > 3} errors={errors[item.id]} onChange={onChange} onRemove={onRemove} />)}</div><div className="section-actions"><button className="button button-secondary" type="button" disabled={items.length >= 6} onClick={onAdd}>Adicionar outro mês</button></div></section>
}
