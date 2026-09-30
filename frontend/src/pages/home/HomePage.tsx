import {
  ArrowUpRight,
  CalendarRange,
  ChartNoAxesCombined,
  CircleDollarSign,
  ListChecks,
  ShieldCheck,
  Sparkles,
  WalletCards,
} from 'lucide-react'
import { ButtonLink } from '../../components/common/ButtonLink'
import { HomeDecisionCanvas } from '../../components/home/HomeDecisionCanvas'
import './home.css'

const readingSignals = [
  {
    icon: ChartNoAxesCombined,
    title: 'Renda em movimento',
    description: 'Compare os últimos meses e enxergue a variação sem reduzir sua realidade a uma média isolada.',
    detail: 'Histórico de 3 a 6 meses',
  },
  {
    icon: CalendarRange,
    title: 'Compromissos no calendário',
    description: 'Veja quanto da referência mensal está comprometido com parcelas e despesas fixas — e qual margem permanece disponível.',
    detail: 'Compromissos e margem',
  },
  {
    icon: WalletCards,
    title: 'Gastos com contexto',
    description: 'As transações recentes são organizadas por categoria para revelar padrões e dar contexto ao seu cenário financeiro.',
    detail: 'Categorias do dia a dia',
  },
]

const journeySteps = [
  {
    title: 'Conte como a renda aconteceu',
    description: 'Adicione de três a seis meses de entradas e os compromissos que acompanham sua rotina.',
  },
  {
    title: 'Enxergue seu ritmo financeiro',
    description: 'A análise observa a variação da renda e relaciona essa referência aos seus compromissos e gastos recentes.',
  },
  {
    title: 'Decida com o cenário à vista',
    description: 'Use a leitura para planejar e, quando precisar, teste o efeito de uma nova despesa.',
  },
]

export function HomePage() {
  return (
    <div className="home-page">
      <section className="home-hero" aria-labelledby="home-title">
        <div className="home-shell home-shell-hero home-hero-grid">
          <div className="home-hero-copy">
            <p className="home-intro"><Sparkles size={17} aria-hidden="true" /> Planejamento para renda variável</p>
            <h1 id="home-title">
              <span className="home-title-primary">Sua <em>renda</em> muda.</span>
              <span className="home-title-secondary">
                <span>Seu planejamento</span>
                <span>pode acompanhar.</span>
              </span>
            </h1>
            <p className="home-hero-lead">Entenda como renda recente, compromissos e gastos afetam seus próximos passos — antes de tomar uma nova decisão.</p>
            <div className="home-hero-actions">
              <ButtonLink to="/analysis" icon={<ArrowUpRight size={18} aria-hidden="true" />}>Começar minha análise</ButtonLink>
              <p><ShieldCheck size={17} aria-hidden="true" /> Sem conta ou conexão bancária. Seus dados ficam apenas nesta sessão.</p>
            </div>
          </div>
          <HomeDecisionCanvas />
        </div>
        <div className="home-transition" aria-hidden="true">
          <div className="home-hero-curve" />
          <div className="home-transition-mascot">
            <img
              className="home-transition-mascot-image"
              src="/assets/rendaFlex.mascote5.png"
              alt=""
            />
          </div>
        </div>
      </section>

      <section className="home-understand" aria-labelledby="understand-title">
        <div className="home-shell home-shell-reading">
          <header className="home-section-heading">
            <p>Uma leitura que respeita a variação</p>
            <h2 id="understand-title">O que muda quando você olha o todo</h2>
          </header>
          <div className="home-signal-layout">
            <article className="home-signal-feature">
              <div className="home-signal-icon"><CircleDollarSign aria-hidden="true" /></div>
              <p>Em vez de perguntar apenas “quanto entrou?”, o RendaFlex relaciona renda, compromissos e gastos recentes. A análise também identifica seu perfil financeiro e traz recomendações de acordo com o cenário.</p>
              <strong>Menos palpite.<br />Mais referência.</strong>
            </article>
            <div className="home-signal-list">
              {readingSignals.map(({ icon: Icon, title, description, detail }) => (
                <article key={title}>
                  <Icon aria-hidden="true" />
                  <div>
                    <h3>{title}</h3>
                    <p>{description}</p>
                  </div>
                  <span>{detail}</span>
                </article>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="home-rhythm" aria-labelledby="rhythm-title">
        <div className="home-shell home-shell-visual home-rhythm-grid">
          <div className="home-rhythm-copy">
            <p>Faixa Flex</p>
            <h2 id="rhythm-title">Uma referência que respira com a sua renda</h2>
            <p>A Faixa Flex transforma meses diferentes em uma leitura contínua. A renda forma o ritmo, os compromissos ocupam parte da referência e os gastos recentes ajudam a dar contexto ao seu comportamento financeiro.</p>
            <dl>
              <div><dt>Entradas</dt><dd>formam o ritmo</dd></div>
              <div><dt>Compromissos</dt><dd>ocupam espaço</dd></div>
              <div><dt>Folga</dt><dd>orienta decisões</dd></div>
            </dl>
          </div>
          <figure className="home-rhythm-figure">
            <div className="rhythm-legend"><span>Exemplo ilustrativo</span><span>jan — abr</span></div>
            <svg viewBox="0 0 760 360" role="img" aria-labelledby="rhythm-figure-title rhythm-figure-desc">
              <title id="rhythm-figure-title">Ritmo financeiro de quatro meses</title>
              <desc id="rhythm-figure-desc">Uma faixa azul varia de espessura ao longo de quatro meses. Uma área coral mostra compromissos ocupando parte da renda.</desc>
              <defs>
                <linearGradient id="wide-rhythm-gradient" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0" stopColor="#6f98c8" />
                  <stop offset="1" stopColor="#315b8c" />
                </linearGradient>
                <linearGradient id="wide-commitment-gradient" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0" stopColor="#f7a08e" />
                  <stop offset="1" stopColor="#f2765e" />
                </linearGradient>
              </defs>
              <path className="rhythm-shadow" d="M18 212 C120 74 210 76 303 174 S478 312 742 120" />
              <path className="rhythm-main" d="M18 194 C120 56 210 58 303 156 S478 294 742 102" />
              <path className="rhythm-commitment" d="M18 214 C120 96 210 104 303 184 S478 318 742 140" />
              {[{ x: 92, y: 116, m: 'jan', v: 'R$ 2.800' }, { x: 270, y: 132, m: 'fev', v: 'R$ 4.100' }, { x: 470, y: 250, m: 'mar', v: 'R$ 3.250' }, { x: 665, y: 142, m: 'abr', v: 'R$ 4.800' }].map((point) => (
                <g className="rhythm-point" key={point.m} transform={`translate(${point.x} ${point.y})`}>
                  <circle r="8" />
                  <text y="-25">{point.v}</text>
                  <text className="rhythm-month" y="28">{point.m}</text>
                </g>
              ))}
            </svg>
            <figcaption><span><i className="is-income" /> Renda recente</span><span><i className="is-commitment" /> Compromissos</span></figcaption>
          </figure>
        </div>
      </section>

      <section className="home-process" aria-labelledby="process-title">
        <div className="home-shell home-shell-journey home-process-grid">
          <header>
            <p>Do dado à decisão</p>
            <h2 id="process-title">Um caminho curto, sem simplificar demais</h2>
          </header>
          <div className="home-process-steps">
            <img
              className="home-process-mascot"
              src="/assets/rendaFlex.mascote6.png"
              alt=""
              aria-hidden="true"
            />
            <ol>
              {journeySteps.map((step, index) => (
                <li key={step.title}>
                  <span>{index + 1}</span>
                  <div><h3>{step.title}</h3><p>{step.description}</p></div>
                </li>
              ))}
            </ol>
          </div>
        </div>
      </section>

      <section className="home-simulation" aria-labelledby="simulation-title">
        <div className="home-shell home-shell-simulation home-simulation-panel">
          <div className="home-simulation-copy">
            <p>Simulação de despesas</p>
            <h2 id="simulation-title">E se uma nova despesa entrar no próximo mês?</h2>
            <p>Compare margem, indicadores e perfil antes e depois de incluir uma nova despesa, com o nível de impacto do cenário projetado.</p>
            <ButtonLink to="/expense-simulation" variant="secondary" icon={<ArrowUpRight size={18} aria-hidden="true" />}>Simular uma despesa</ButtonLink>
          </div>
          <div className="home-simulation-visual" aria-label="Exemplo ilustrativo do impacto de uma nova despesa">
            <div className="simulation-current"><span>Margem atual</span><strong>R$ 1.240</strong><small>após compromissos</small></div>
            <div className="simulation-connector"><span>nova parcela</span><strong>− R$ 320</strong></div>
            <div className="simulation-projected"><span>Margem projetada</span><strong>R$ 920</strong><small>após nova parcela</small></div>
          </div>
        </div>
      </section>

      <section className="home-final" aria-labelledby="final-title">
        <div className="home-shell home-shell-wide home-final-grid">
          <div>
            <ListChecks size={34} aria-hidden="true" />
            <h2 id="final-title">Comece pelos meses que você já viveu.</h2>
          </div>
          <img
            className="home-final-mascot"
            src="/assets/rendaFlex.mascote7.png"
            alt=""
            aria-hidden="true"
          />
          <div>
            <p>Em poucos passos, transforme sua renda recente em uma referência prática para o que vem pela frente.</p>
            <ButtonLink to="/analysis" icon={<ArrowUpRight size={18} aria-hidden="true" />}>Começar minha análise</ButtonLink>
            <small>O RendaFlex apoia sua organização e não substitui consultoria financeira profissional.</small>
          </div>
        </div>
      </section>
    </div>
  )
}
