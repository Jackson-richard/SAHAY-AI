const STORAGE_KEY = 'sahay_app_state'
const LEGACY_KEYS = [
  'sahay_profile',
  'sahay_consent',
  'sahay_checkins',
  'sahay_case',
  'sahay_alerts',
  'sahay_interventions',
  'sahay_trusted_contact',
  'sahay_notifications',
  'sahay_dashboard_cases',
  'sahay_consented',
  'sahay_audit_log',
  'sahay_demo_mode',
  'sahay_lang',
]

export const CASE_STAGES = [
  { id: 'registration', label: 'Registration' },
  { id: 'investigation', label: 'Investigation' },
  { id: 'hearing', label: 'Hearing' },
  { id: 'post_verdict', label: 'Post-Verdict / Rehabilitation' },
]

export function emptyState() {
  return {
    profile: null,
    case: null,
    consent: null,
    preferredLanguage: 'en',
    trustedPerson: null,
    checkins: [],
    distressScore: null,
    trend: null,
    concernType: null,
    concernLevel: null,
    alerts: [],
    supportRouting: null,
    lastAnalysis: null,
    supportRequests: [],
    dashboardCases: [],
    notifications: [],
    isDemoMode: false,
    onboarding: { registered: false, caseLinked: false, consented: false },
  }
}

export function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      return { ...emptyState(), ...JSON.parse(raw) }
    }
  } catch {
    /* ignore */
  }
  return migrateLegacy()
}

export function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

export function clearState() {
  localStorage.removeItem(STORAGE_KEY)
  LEGACY_KEYS.forEach((k) => localStorage.removeItem(k))
}

function parse(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function migrateLegacy() {
  const profile = parse('sahay_profile')
  if (!profile) return emptyState()
  const caseData = parse('sahay_case')
  const consent = parse('sahay_consent')
  const consented = localStorage.getItem('sahay_consented') === 'true'
  const checkins = parse('sahay_checkins') || []
  const last = checkins[checkins.length - 1]
  return {
    ...emptyState(),
    profile,
    case: caseData,
    consent: consent && consented ? { ...consent, given: true } : consent,
    preferredLanguage: localStorage.getItem('sahay_lang') || profile.preferred_language || 'en',
    trustedPerson: parse('sahay_trusted_contact'),
    checkins,
    distressScore: last?.score ?? null,
    trend: last?.trend_direction || last?.trend || null,
    concernType: last?.concernType || last?.concern_type || null,
    concernLevel: last?.concernLevel || last?.concern_level || null,
    alerts: parse('sahay_alerts') || [],
    dashboardCases: parse('sahay_dashboard_cases') || [],
    isDemoMode: localStorage.getItem('sahay_demo_mode') === 'true',
    onboarding: {
      registered: true,
      caseLinked: !!caseData,
      consented: consented && !!consent,
    },
  }
}
