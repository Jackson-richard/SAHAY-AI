import { translations } from './translations.js'

export const supportedLanguages = ['en', 'ta', 'hi', 'te', 'kn', 'ml']

export const languageNames = {
  en: 'English',
  ta: 'தமிழ் (Tamil)',
  hi: 'हिन्दी (Hindi)',
  te: 'తెలుగు (Telugu)',
  kn: 'ಕನ್ನಡ (Kannada)',
  ml: 'മലയാളം (Malayalam)',
}

const extras = {
  en: {
    field_full_name: 'Full name',
    field_mobile: 'Mobile number',
    field_age: 'Age',
    field_language: 'Preferred language',
    field_trusted_person: 'Trusted person (optional)',
    field_trusted_person_help: 'Someone you choose to notify if you later enable trusted-contact consent.',
    placeholder_full_name: 'Enter your full name',
    placeholder_mobile: '+91 XXXXX XXXXX',
    placeholder_age: 'Enter your age',
    placeholder_trusted: 'Name (optional)',
    register_title: 'Create your profile',
    register_subtitle: 'SAHAY-AI monitors wellbeing throughout your existing complaint or case journey. Participation is voluntary.',
    btn_continue_profile: 'Continue',
    case_step_title: 'Existing Complaint / Case ID',
    case_step_helper: 'Enter the complaint/case ID provided during registration.',
    case_prototype_notice: 'Prototype / demo data: SAHAY-AI is not connected to an official court or police case system in this build. Enter the ID you already have. Do not invent a government or court Case ID.',
    placeholder_case_id: 'Existing complaint / case ID',
    btn_connect_case: 'Continue',
    case_connected_title: 'Case connected',
    case_connected_desc: 'SAHAY-AI will use this existing complaint/case connection to monitor wellbeing. AI identifies risk signals. Authorized support decides the response.',
    demo_mode_available: 'Prototype demo setup',
    demo_mode_desc: 'Load clearly labelled synthetic demo data. These are not real victim identities and not official Case IDs.',
    scenario_stable: 'Scenario 1: Stable (synthetic)',
    scenario_increasing: 'Scenario 2: Increasing Concern (synthetic)',
    scenario_critical: 'Scenario 3: Safety concern requiring review (synthetic)',
    prototype_badge: 'Prototype / demo data',
    distress_score: 'Dynamic Distress Score',
    concern_type: 'Concern Type',
    concern_level: 'Concern Level',
    trend_label: 'Trend',
    support_routing_title: 'Authorized support pathway',
    support_routing_desc: 'A concern was flagged for authorized human review. SAHAY-AI does not decide an intervention.',
    withdraw_consent: 'Withdraw consent',
    withdraw_consent_desc: 'You can stop wellbeing monitoring. You will be asked to consent again before check-ins resume.',
    consent_status: 'Consent status',
    consent_active: 'Active',
    consent_withdrawn: 'Withdrawn / not given',
    reset_demo_data: 'Reset demo data',
    reset_demo_confirm: 'This clears local prototype data and returns you to registration. Continue?',
    finish_checkin: 'Finish check-in',
    groq_fallback_banner: 'Development fallback: Groq is not configured. Supportive replies are local, not from a live model.',
    groq_live_banner: 'Conversational replies are using the configured Groq backend.',
    supabase_local: 'Database: local prototype storage (Supabase is not connected).',
    supabase_ready: 'Database: Supabase persistence is configured on the server.',
    start_voice_checkin: 'Start Voice Check-in',
    adaptive_voice_note: 'If repeated app check-ins are missed, a consented voice check-in may be used to reconnect. Voice is a secondary channel and is not the sole basis for a high-risk decision.',
    not_medical: 'This is a wellbeing concern indicator, not a medical diagnosis.',
    assigned_support_generic: 'Assigned authorized support (prototype)',
    type_wellbeing: 'Wellbeing',
    type_safety: 'Safety',
    type_other: 'Other',
    level_stable: 'Stable',
    level_elevated: 'Elevated',
    level_increasing: 'Increasing Concern',
    trend_rising: 'Rising',
    trend_falling: 'Falling',
    trend_stable: 'Stable',
    view_my_journey: 'View My Journey',
    case_stage: 'Case stage',
  },
}

export function t(lang, key, params) {
  const pack = translations[lang] || translations.en
  let text =
    extras[lang]?.[key] ||
    pack?.[key] ||
    extras.en[key] ||
    translations.en?.[key] ||
    key
  if (params && typeof text === 'string') {
    Object.entries(params).forEach(([p, v]) => {
      text = text.replace(new RegExp(`\\{${p}\\}`, 'g'), v)
    })
  }
  return text
}
