import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { analyzeCheckin, getLocalFallbackResponse } from '../lib/analysis.js'
import { fetchStatus, requestChatReply } from '../lib/api.js'
import { buildDemoState } from '../lib/demo.js'
import { generateId } from '../lib/ids.js'
import { CASE_STAGES, clearState, emptyState, loadState, saveState } from '../lib/storage.js'
import { languageNames, supportedLanguages, t as translate } from '../i18n/index.js'

const SahayContext = createContext(null)

export function SahayProvider({ children }) {
  const [state, setState] = useState(() => loadState())
  const [backend, setBackend] = useState({ groqConfigured: false, supabaseConfigured: false, fallback: true })

  useEffect(() => {
    saveState(state)
  }, [state])

  useEffect(() => {
    fetchStatus().then(setBackend)
  }, [])

  const update = useCallback((patch) => {
    setState((prev) => ({ ...prev, ...(typeof patch === 'function' ? patch(prev) : patch) }))
  }, [])

  const t = useCallback(
    (key, params) => translate(state.preferredLanguage || 'en', key, params),
    [state.preferredLanguage],
  )

  const setLanguage = useCallback((lang) => {
    const next = supportedLanguages.includes(lang) ? lang : 'en'
    update((prev) => ({
      preferredLanguage: next,
      profile: prev.profile ? { ...prev.profile, preferredLanguage: next } : prev.profile,
    }))
    document.documentElement.lang = next
  }, [update])

  const registerProfile = useCallback((fields) => {
    const profile = {
      id: generateId(),
      fullName: fields.fullName.trim(),
      mobile: fields.mobile.trim(),
      age: Number(fields.age),
      preferredLanguage: fields.preferredLanguage,
      isDemo: false,
      created_at: new Date().toISOString(),
    }
    const trustedPerson = fields.trustedPerson?.trim()
      ? { name: fields.trustedPerson.trim(), id: generateId() }
      : null
    update({
      profile,
      trustedPerson,
      preferredLanguage: fields.preferredLanguage,
      onboarding: { registered: true, caseLinked: false, consented: false },
    })
  }, [update])

  const linkCase = useCallback((officialCaseId) => {
    const id = officialCaseId.trim()
    const isPrototype = true
    update((prev) => ({
      case: {
        id: generateId(),
        officialCaseId: id,
        profileId: prev.profile?.id || null,
        case_status: 'active',
        case_stage: 'investigation',
        isPrototype,
        stages: CASE_STAGES.map((s, i) => ({
          ...s,
          status: i === 0 ? 'completed' : i === 1 ? 'active' : 'pending',
          date: i === 0 ? new Date().toISOString().split('T')[0] : null,
        })),
        created_at: new Date().toISOString(),
      },
      profile: prev.profile ? { ...prev.profile, officialCaseId: id } : prev.profile,
      onboarding: { ...prev.onboarding, caseLinked: true },
    }))
  }, [update])

  const giveConsent = useCallback((prefs) => {
    update((prev) => ({
      consent: {
        given: true,
        general_consent: true,
        voice_consent: !!prefs.voice_consent,
        trusted_contact_consent: !!prefs.trusted_contact_consent,
        consented_at: new Date().toISOString(),
      },
      onboarding: { ...prev.onboarding, consented: true },
    }))
  }, [update])

  const withdrawConsent = useCallback(() => {
    update((prev) => ({
      consent: prev.consent
        ? { ...prev.consent, given: false, withdrawn_at: new Date().toISOString() }
        : { given: false, general_consent: false },
      onboarding: { ...prev.onboarding, consented: false },
    }))
  }, [update])

  const processCheckin = useCallback((chatLog, channel = 'chat') => {
    let result = null
    setState((prev) => {
      const analysis = analyzeCheckin(chatLog, prev.checkins)
      const checkin = {
        id: generateId(),
        profile_id: prev.profile?.id || null,
        case_id: prev.case?.officialCaseId || null,
        channel,
        language: prev.preferredLanguage,
        score: analysis.score,
        concernLevel: analysis.concernLevel,
        concernType: analysis.concernType,
        concern_level: analysis.concern_level,
        contributing_signals: analysis.contributing_signals,
        signals: analysis.contributing_signals,
        detected_signals: analysis.detected_signals,
        safety_concern: analysis.safety_concern,
        trend: analysis.trend,
        week: `W${prev.checkins.length + 1}`,
        date: new Date().toISOString().split('T')[0],
        message_count: chatLog.filter((m) => m.role === 'user').length,
        created_at: new Date().toISOString(),
      }

      const alerts = [...prev.alerts]
      if (analysis.concernLevel !== 'Stable' || analysis.safety_concern) {
        alerts.push({
          id: generateId(),
          case_id: prev.case?.officialCaseId || null,
          score: analysis.score,
          alert_level: analysis.safety_concern ? 'critical' : analysis.concern_level,
          reason: analysis.supportRouting?.message || analysis.contributing_signals.join('. '),
          status: 'pending',
          created_at: new Date().toISOString(),
          supportRouting: analysis.supportRouting,
        })
      }

      let dashboardCases = [...prev.dashboardCases]
      if (prev.profile && prev.case) {
        const existing = dashboardCases.find((c) => c.official_case_id === prev.case.officialCaseId)
        const row = existing || {
          id: generateId(),
          official_case_id: prev.case.officialCaseId,
          name: prev.profile.fullName,
          trend: [],
          alertCount: 0,
          reviewNeeded: false,
          is_demo: !!prev.profile.isDemo,
        }
        row.status = analysis.concern_level
        row.lastCheckin = checkin.date
        row.trend = [...(row.trend || []), checkin.score].slice(-6)
        row.signals = analysis.contributing_signals
        row.concernType = analysis.concernType
        row.concernLevel = analysis.concernLevel
        if (analysis.safety_concern || analysis.concernLevel !== 'Stable') {
          row.reviewNeeded = true
          row.alertCount = (row.alertCount || 0) + 1
        }
        if (!existing) dashboardCases.push(row)
      }

      const missed = prev.checkins.length >= 1
      const last = prev.checkins[prev.checkins.length - 1]
      const notifications = [...prev.notifications]
      if (prev.consent?.voice_consent && last) {
        const days = Math.floor((Date.now() - new Date(last.created_at).getTime()) / 86400000)
        if (days >= 3 && missed) {
          notifications.unshift({
            id: generateId(),
            type: 'adaptive_outreach',
            title: 'Missed check-in detected',
            message: 'A consented voice check-in may help reconnect. Voice is a secondary channel.',
            created_at: new Date().toISOString(),
          })
        }
      }

      const next = {
        ...prev,
        checkins: [...prev.checkins, checkin],
        distressScore: analysis.distressScore,
        trend: analysis.trend,
        concernType: analysis.concernType,
        concernLevel: analysis.concernLevel,
        supportRouting: analysis.supportRouting,
        lastAnalysis: { ...analysis, checkin, signals: analysis.contributing_signals },
        alerts,
        dashboardCases,
        notifications,
      }
      result = { success: true, checkin, analysis: next.lastAnalysis }
      return next
    })
    return result
  }, [])

  const getAIResponse = useCallback(async (chatHistory, userMessage) => {
    const tFn = (key, fallback) => translate(state.preferredLanguage || 'en', key) !== key
      ? translate(state.preferredLanguage || 'en', key)
      : fallback
    const remote = await requestChatReply({
      chatHistory,
      userMessage,
      language: state.preferredLanguage,
    })
    if (remote?.text) return { text: remote.text, source: remote.source || 'groq' }
    return {
      text: getLocalFallbackResponse(chatHistory, userMessage, tFn),
      source: 'fallback',
    }
  }, [state.preferredLanguage])

  const loadDemo = useCallback((key) => {
    const demo = buildDemoState(key)
    if (demo) setState(demo)
    return !!demo
  }, [])

  const resetDemo = useCallback(() => {
    clearState()
    setState(emptyState())
  }, [])

  const addSupportRequest = useCallback((req) => {
    update((prev) => ({
      supportRequests: [
        ...prev.supportRequests,
        { id: generateId(), created_at: new Date().toISOString(), status: 'sent', ...req },
      ],
    }))
  }, [update])

  const markReviewed = useCallback((caseId) => {
    update((prev) => ({
      alerts: prev.alerts.map((a) =>
        a.case_id === caseId ? { ...a, status: 'reviewed', reviewed_at: new Date().toISOString() } : a,
      ),
      dashboardCases: prev.dashboardCases.map((c) =>
        c.official_case_id === caseId || c.id === caseId
          ? { ...c, reviewNeeded: false, alertCount: 0 }
          : c,
      ),
    }))
  }, [update])

  const value = useMemo(
    () => ({
      state,
      backend,
      t,
      languageNames,
      supportedLanguages,
      CASE_STAGES,
      setLanguage,
      registerProfile,
      linkCase,
      giveConsent,
      withdrawConsent,
      processCheckin,
      getAIResponse,
      loadDemo,
      resetDemo,
      addSupportRequest,
      markReviewed,
      isRegistered: !!state.onboarding?.registered && !!state.profile,
      isCaseLinked: !!state.onboarding?.caseLinked && !!state.case,
      isConsented: !!state.onboarding?.consented && !!state.consent?.given,
    }),
    [
      state,
      backend,
      t,
      setLanguage,
      registerProfile,
      linkCase,
      giveConsent,
      withdrawConsent,
      processCheckin,
      getAIResponse,
      loadDemo,
      resetDemo,
      addSupportRequest,
      markReviewed,
    ],
  )

  return <SahayContext.Provider value={value}>{children}</SahayContext.Provider>
}

export function useSahay() {
  const ctx = useContext(SahayContext)
  if (!ctx) throw new Error('useSahay must be used within SahayProvider')
  return ctx
}
