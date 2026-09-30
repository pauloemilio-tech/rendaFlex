const incomePoints = [
  { x: 54, y: 182, month: 'jan', value: '2,8 mil' },
  { x: 190, y: 94, month: 'fev', value: '4,1 mil' },
  { x: 326, y: 142, month: 'mar', value: '3,2 mil' },
  { x: 462, y: 68, month: 'abr', value: '4,8 mil' },
]

export function HomeDecisionCanvas() {
  return (
    <aside className="home-decision-canvas" aria-label="Exemplo ilustrativo de uma leitura financeira do RendaFlex">
      <div className="decision-orbit decision-orbit-one" aria-hidden="true" />
      <div className="decision-orbit decision-orbit-two" aria-hidden="true" />
      <div className="decision-canvas-card">
        <header>
          <div><span>Faixa Flex</span><strong>Seu ritmo financeiro</strong></div>
          <small>exemplo ilustrativo</small>
        </header>
        <figure>
          <svg viewBox="0 0 520 245" role="img" aria-labelledby="hero-rhythm-title hero-rhythm-desc">
            <title id="hero-rhythm-title">Renda variável entre janeiro e abril</title>
            <desc id="hero-rhythm-desc">Uma faixa curva fica mais larga ou estreita conforme o valor recebido em cada mês.</desc>
            <defs>
              <linearGradient id="hero-ribbon-gradient" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stopColor="#f5ebdd" />
                <stop offset=".52" stopColor="#f6a18f" />
                <stop offset="1" stopColor="#f2765e" />
              </linearGradient>
            </defs>
            <path className="decision-ribbon-shadow" d="M22 190 C112 204 126 72 208 76 C300 80 308 194 392 174 C442 162 468 104 500 66" />
            <path className="decision-ribbon" d="M22 176 C112 190 126 58 208 62 C300 66 308 180 392 160 C442 148 468 90 500 52" />
            {incomePoints.map((point) => (
              <g className="decision-point" key={point.month} transform={`translate(${point.x} ${point.y})`}>
                <circle r="7" />
                <text className="decision-value" y="-20">{point.value}</text>
                <text className="decision-month" y="27">{point.month}</text>
              </g>
            ))}
          </svg>
        </figure>
        <div className="decision-canvas-footer">
          <div><span>Variação recente</span><strong>acompanhar de perto</strong></div>
          <div><span>Margem estimada</span><strong>R$ 1.240</strong></div>
        </div>
      </div>
      <div className="decision-float decision-float-profile"><span>Leitura</span><strong>Renda oscilante</strong><i /></div>
      <div className="decision-float decision-float-commitment"><span>Compromissos</span><strong>47%</strong><small>da referência mensal</small></div>
    </aside>
  )
}
