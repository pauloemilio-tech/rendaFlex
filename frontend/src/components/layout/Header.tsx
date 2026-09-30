import { Link } from 'react-router-dom'
import { Navigation } from './Navigation'
import { ThemeToggle } from './ThemeToggle'

export function Header() {
  return (
    <header className="app-header">
      <div className="header-content">
        <Link className="brand" to="/" aria-label="RendaFlex — página inicial">
          <img className="brand-logo" src="/assets/rendaFlex.logo1.png" alt="RendaFlex" />
        </Link>
        <div className="header-actions"><Navigation /><ThemeToggle /></div>
      </div>
    </header>
  )
}
