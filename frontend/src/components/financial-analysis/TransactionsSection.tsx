import type { TransactionErrors, TransactionFormItem } from '../../types'
import { TransactionItemFields } from './TransactionItemFields'

type Props = { items: TransactionFormItem[]; errors: TransactionErrors; onAdd: () => void; onChange: (id: string, field: 'description' | 'amount' | 'date' | 'type', value: string) => void; onRemove: (id: string) => void }

export function TransactionsSection({ items, errors, onAdd, onChange, onRemove }: Props) {
  return <section className="form-section" aria-labelledby="transactions-title">
    <div className="form-section-heading"><div><span className="step-number">3</span><h2 id="transactions-title">Seus gastos recentes</h2></div><p>Registre receitas e despesas do mês mais recente para dar contexto à análise e à categorização.</p></div>
    <div className="transaction-list">{items.map((item, index) => <TransactionItemFields key={item.id} item={item} index={index} canRemove={items.length > 1} errors={errors[item.id]} onChange={onChange} onRemove={onRemove} />)}</div>
    <div className="transaction-note">As despesas serão classificadas futuramente pelo sistema. Nenhuma categoria é atribuída neste formulário.</div>
    <div className="section-actions"><button className="button button-secondary" type="button" onClick={onAdd}>Adicionar transação</button><span>{items.length} {items.length === 1 ? 'transação informada' : 'transações informadas'}</span></div>
  </section>
}
