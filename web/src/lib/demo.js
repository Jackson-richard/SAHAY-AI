import { generateId } from './ids.js'
import { CASE_STAGES } from './storage.js'

export const DEMO_SCENARIOS = {
  stable: {
    name: 'Stable Wellbeing',
    description: 'Synthetic demo: stable, improving wellbeing patterns',
    profile: {
      fullName: 'Demo User A',
      mobile: '+91 90000 00001',
      age: 28,
      preferredLanguage: 'en',
      trustedPerson: '',
    },
    officialCaseId: 'DEMO-CASE-001',
    checkins: [
      { week: 'W1', score: 35, date: '2026-08-04', concernLevel: 'Stable', concernType: 'Wellbeing', contributing_signals: ['mild anxiety'], channel: 'chat', trend: 'falling' },
      { week: 'W2', score: 30, date: '2026-08-11', concernLevel: 'Stable', concernType: 'Wellbeing', contributing_signals: ['slight worry'], channel: 'chat', trend: 'falling' },
      { week: 'W3', score: 25, date: '2026-08-18', concernLevel: 'Stable', concernType: 'Other', contributing_signals: [], channel: 'chat', trend: 'falling' },
      { week: 'W4', score: 22, date: '2026-08-25', concernLevel: 'Stable', concernType: 'Other', contributing_signals: [], channel: 'voice', trend: 'falling' },
    ],
    case_stage: 'hearing',
  },
  increasing: {
    name: 'Increasing Concern',
    description: 'Synthetic demo: rising distress signals over time',
    profile: {
      fullName: 'Demo User B',
      mobile: '+91 90000 00002',
      age: 34,
      preferredLanguage: 'en',
      trustedPerson: '',
    },
    officialCaseId: 'DEMO-CASE-002',
    checkins: [
      { week: 'W1', score: 28, date: '2026-08-04', concernLevel: 'Stable', concernType: 'Wellbeing', contributing_signals: ['mild anxiety'], channel: 'chat', trend: 'rising' },
      { week: 'W2', score: 35, date: '2026-08-11', concernLevel: 'Stable', concernType: 'Wellbeing', contributing_signals: ['Sleep Difficulty'], channel: 'chat', trend: 'rising' },
      { week: 'W3', score: 47, date: '2026-08-18', concernLevel: 'Elevated', concernType: 'Wellbeing', contributing_signals: ['Fear', 'Sleep Difficulty'], channel: 'chat', trend: 'rising' },
      { week: 'W4', score: 61, date: '2026-08-25', concernLevel: 'Increasing Concern', concernType: 'Wellbeing', contributing_signals: ['Fear', 'Sleep Difficulty', 'Case-related Anxiety'], channel: 'chat', trend: 'rising' },
      { week: 'W5', score: 68, date: '2026-08-30', concernLevel: 'Increasing Concern', concernType: 'Wellbeing', contributing_signals: ['Fear', 'Sleep Difficulty', 'Worsening compared with previous check-ins'], channel: 'chat', trend: 'rising' },
    ],
    case_stage: 'investigation',
  },
  critical: {
    name: 'Concern Requiring Human Review',
    description: 'Synthetic demo: safety signals requiring human review',
    profile: {
      fullName: 'Demo User C',
      mobile: '+91 90000 00003',
      age: 25,
      preferredLanguage: 'en',
      trustedPerson: '',
    },
    officialCaseId: 'DEMO-CASE-003',
    checkins: [
      { week: 'W1', score: 32, date: '2026-08-04', concernLevel: 'Stable', concernType: 'Wellbeing', contributing_signals: ['mild anxiety'], channel: 'chat', trend: 'rising' },
      { week: 'W2', score: 45, date: '2026-08-11', concernLevel: 'Elevated', concernType: 'Wellbeing', contributing_signals: ['Fear', 'Sleep Difficulty'], channel: 'chat', trend: 'rising' },
      { week: 'W3', score: 62, date: '2026-08-18', concernLevel: 'Increasing Concern', concernType: 'Safety', contributing_signals: ['Fear', 'Feeling Unsafe', 'Sleep Difficulty'], channel: 'voice', trend: 'rising' },
      { week: 'W4', score: 78, date: '2026-08-25', concernLevel: 'Increasing Concern', concernType: 'Safety', contributing_signals: ['Feeling Unsafe', 'Threat Mentions', 'Hopelessness', 'Sleep Difficulty'], channel: 'chat', trend: 'rising' },
    ],
    case_stage: 'investigation',
    alerts: [
      { alert_level: 'critical', reason: 'Safety concern detected. Feeling unsafe and threat mentions reported. Concern increased across recent check-ins.', score: 78 },
    ],
  },
}

function buildCase(officialCaseId, profileId, stageId) {
  const stageIndex = CASE_STAGES.findIndex((s) => s.id === stageId)
  return {
    id: generateId(),
    officialCaseId,
    profileId,
    case_status: 'active',
    case_stage: stageId,
    isPrototype: true,
    stages: CASE_STAGES.map((s, i) => ({
      ...s,
      status: i < stageIndex ? 'completed' : i === stageIndex ? 'active' : 'pending',
      date: i <= stageIndex ? '2026-08-01' : null,
    })),
    created_at: new Date().toISOString(),
  }
}

export function buildDemoState(scenarioKey) {
  const scenario = DEMO_SCENARIOS[scenarioKey]
  if (!scenario) return null
  const profileId = generateId()
  const last = scenario.checkins[scenario.checkins.length - 1]
  const checkins = scenario.checkins.map((c) => ({
    ...c,
    id: generateId(),
    profile_id: profileId,
    case_id: scenario.officialCaseId,
    score: c.score,
    concernLevel: c.concernLevel,
    concernType: c.concernType,
    concern_level: c.concernLevel === 'Stable' ? 'stable' : c.concernLevel === 'Elevated' ? 'elevated' : 'increasing',
    signals: c.contributing_signals,
    safety_concern: c.concernType === 'Safety',
    created_at: `${c.date}T10:00:00Z`,
  }))

  const dashboardCases = Object.entries(DEMO_SCENARIOS).map(([key, sc]) => {
    const latest = sc.checkins[sc.checkins.length - 1]
    return {
      id: generateId(),
      official_case_id: sc.officialCaseId,
      name: sc.profile.fullName,
      status: latest.concernLevel === 'Stable' ? 'stable' : latest.concernLevel === 'Elevated' ? 'elevated' : 'increasing',
      lastCheckin: latest.date,
      trend: sc.checkins.map((c) => c.score),
      alertCount: sc.alerts ? sc.alerts.length : 0,
      reviewNeeded: key === 'increasing' || key === 'critical',
      signals: latest.contributing_signals,
      concernType: latest.concernType,
      concernLevel: latest.concernLevel,
      case_stage: sc.case_stage,
      is_demo: true,
    }
  })

  const alerts = (scenario.alerts || []).map((a) => ({
    ...a,
    id: generateId(),
    case_id: scenario.officialCaseId,
    status: 'pending',
    created_at: new Date().toISOString(),
  }))

  return {
    profile: {
      id: profileId,
      fullName: scenario.profile.fullName,
      mobile: scenario.profile.mobile,
      age: scenario.profile.age,
      preferredLanguage: scenario.profile.preferredLanguage,
      isDemo: true,
      demoScenario: scenarioKey,
      created_at: new Date().toISOString(),
    },
    case: buildCase(scenario.officialCaseId, profileId, scenario.case_stage),
    consent: {
      given: true,
      general_consent: true,
      voice_consent: true,
      trusted_contact_consent: false,
      consented_at: new Date().toISOString(),
    },
    preferredLanguage: scenario.profile.preferredLanguage,
    trustedPerson: null,
    checkins,
    distressScore: last.score,
    trend: last.trend,
    concernType: last.concernType,
    concernLevel: last.concernLevel,
    alerts,
    supportRouting: last.concernType === 'Safety' || last.concernLevel !== 'Stable'
      ? {
          needed: true,
          pathway: last.concernType === 'Safety' ? 'Safety / high-concern pathway' : 'Wellbeing support pathway',
          message: 'Synthetic demo: routed to authorized support for human review.',
        }
      : { needed: false, pathway: null, message: null },
    lastAnalysis: null,
    supportRequests: [],
    dashboardCases,
    notifications: [],
    isDemoMode: true,
    onboarding: { registered: true, caseLinked: true, consented: true },
  }
}
