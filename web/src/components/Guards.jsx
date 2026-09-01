import { Navigate } from 'react-router-dom'
import { useSahay } from '../store/SahayContext.jsx'

export function RequireOnboarding({ children, need = 'consent' }) {
  const { isRegistered, isCaseLinked, isConsented } = useSahay()
  if (!isRegistered) return <Navigate to="/register" replace />
  if (need !== 'register' && !isCaseLinked) return <Navigate to="/case" replace />
  if (need === 'consent' && !isConsented) return <Navigate to="/consent" replace />
  return children
}

export function RedirectIfComplete({ children }) {
  const { isRegistered, isCaseLinked, isConsented } = useSahay()
  if (isRegistered && isCaseLinked && isConsented) return <Navigate to="/home" replace />
  if (isRegistered && isCaseLinked && !isConsented) return <Navigate to="/consent" replace />
  if (isRegistered && !isCaseLinked) return <Navigate to="/case" replace />
  return children
}
