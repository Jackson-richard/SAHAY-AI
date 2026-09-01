import { NavLink } from 'react-router-dom'
import { useSahay } from '../store/SahayContext.jsx'
import { BrandLink } from './Logo.jsx'
import LanguageSelect from './LanguageSelect.jsx'

export function AppHeader({ back, title, onBack, showLang = true, extra }) {
  return (
    <header className="app-header">
      {back ? (
        <button className="icon-btn" onClick={onBack} aria-label="Back">←</button>
      ) : (
        <div style={{ width: 48 }} />
      )}
      {title ? <span className="brand">{title}</span> : <BrandLink />}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        {extra}
        {showLang && <LanguageSelect />}
      </div>
    </header>
  )
}

const tabs = [
  { to: '/home', key: 'nav_home', icon: '⌂' },
  { to: '/chat', key: 'nav_chat', icon: '💬' },
  { to: '/journey', key: 'nav_journey', icon: '📈' },
  { to: '/support', key: 'nav_support', icon: '☎' },
  { to: '/profile', key: 'nav_profile', icon: '☺' },
]

export function BottomNav() {
  const { t } = useSahay()
  return (
    <nav className="app-nav" aria-label="Main">
      {tabs.map((tab) => (
        <NavLink
          key={tab.to}
          to={tab.to}
          className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
        >
          <span aria-hidden="true">{tab.icon}</span>
          <span>{t(tab.key)}</span>
        </NavLink>
      ))}
    </nav>
  )
}

export function AppShell({ children, back, onBack, title, extra, noNav = false }) {
  return (
    <>
      <AppHeader back={back} onBack={onBack} title={title} extra={extra} />
      <div className={noNav ? 'page no-nav' : 'page'}>{children}</div>
      {!noNav && <BottomNav />}
    </>
  )
}
