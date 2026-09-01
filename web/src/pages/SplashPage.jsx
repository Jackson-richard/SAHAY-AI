import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Logo from '../components/Logo.jsx'
import { useSahay } from '../store/SahayContext.jsx'

export default function SplashPage() {
  const navigate = useNavigate()
  const { t, isRegistered, isCaseLinked, isConsented } = useSahay()

  useEffect(() => {
    const tmr = setTimeout(() => {
      if (isRegistered && isCaseLinked && isConsented) navigate('/home', { replace: true })
      else if (isRegistered && isCaseLinked) navigate('/consent', { replace: true })
      else if (isRegistered) navigate('/case', { replace: true })
      else navigate('/register', { replace: true })
    }, 1600)
    return () => clearTimeout(tmr)
  }, [isRegistered, isCaseLinked, isConsented, navigate])

  return (
    <main className="page no-nav" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
      <Logo size={128} className="logo-hero" />
      <h1 className="font-display" style={{ color: 'var(--primary)', fontSize: 32, margin: '20px 0 8px' }}>{t('brand_name')}</h1>
      <p className="muted" style={{ fontSize: 18 }}>{t('tagline')}</p>
      <div className="progress-bar" style={{ width: 64, marginTop: 32 }}>
        <div className="progress-fill" style={{ width: '70%' }} />
      </div>
      <p className="muted" style={{ position: 'absolute', bottom: 32, fontSize: 12 }}>{t('footer_tagline')}</p>
    </main>
  )
}
