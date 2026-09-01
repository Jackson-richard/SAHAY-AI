export const SIGNAL_DEFINITIONS = {
  fear: { label: 'Fear', weight: 12, keywords: ['fear', 'scared', 'afraid', 'terrified', 'frightened', 'panic', 'worry', 'worried', 'anxious', 'nervous', 'dread'] },
  sleep_difficulty: { label: 'Sleep Difficulty', weight: 10, keywords: ['sleep', 'insomnia', 'nightmare', 'dream', 'wake', 'rest', 'tired', 'exhausted', 'fatigue', 'poorly'] },
  withdrawal: { label: 'Social Withdrawal', weight: 8, keywords: ['alone', 'isolated', 'withdraw', 'avoid', 'hide', 'nobody', 'lonely', "don't want to go", 'staying inside'] },
  hopelessness: { label: 'Hopelessness', weight: 14, keywords: ['hopeless', 'pointless', 'give up', 'no point', 'nothing matters', 'never get better', 'lost cause', 'worthless', 'useless'] },
  feeling_unsafe: { label: 'Feeling Unsafe', weight: 18, keywords: ['unsafe', 'danger', 'threatened', 'threaten', 'attack', 'hurt', 'harm', 'violence', 'hit', 'stalking', 'following'] },
  threat_mentions: { label: 'Threat Mentions', weight: 20, keywords: ['kill', 'die', 'death', 'weapon', 'gun', 'knife', 'murder', 'end my life', 'self-harm', 'suicide'] },
  reduced_engagement: { label: 'Reduced Engagement', weight: 5, keywords: ['fine', 'okay', 'nothing', "don't know", 'whatever', 'same'] },
  physical_symptoms: { label: 'Physical Symptoms', weight: 7, keywords: ['headache', 'stomach', 'pain', 'eating', 'appetite', 'weight', 'shaking', 'trembling', 'nausea', 'dizzy'] },
  case_anxiety: { label: 'Case-related Anxiety', weight: 10, keywords: ['hearing', 'court', 'trial', 'judge', 'lawyer', 'verdict', 'testimony', 'witness', 'case'] },
}

export const CONCERN_LEVELS = {
  Stable: { min: 0, max: 39 },
  Elevated: { min: 40, max: 59 },
  'Increasing Concern': { min: 60, max: 100 },
}

export function concernLevelFromScore(score) {
  if (score < 40) return 'Stable'
  if (score < 60) return 'Elevated'
  return 'Increasing Concern'
}

export function concernLevelKey(score) {
  if (score < 40) return 'stable'
  if (score < 60) return 'elevated'
  return 'increasing'
}

function classifyConcernType(detectedSignals, safetyConcern) {
  if (safetyConcern) return 'Safety'
  const wellbeingKeys = ['fear', 'sleep_difficulty', 'withdrawal', 'hopelessness', 'physical_symptoms', 'case_anxiety']
  if (detectedSignals.some((s) => wellbeingKeys.includes(s.signal))) return 'Wellbeing'
  return 'Other'
}

function supportPathway(concernType, concernLevel) {
  if (concernType === 'Safety' || concernLevel === 'Increasing Concern') {
    return {
      needed: true,
      pathway: 'Safety / high-concern pathway',
      message: 'Signals were routed to authorized support for human review. SAHAY-AI does not independently decide an intervention.',
    }
  }
  if (concernLevel === 'Elevated') {
    return {
      needed: true,
      pathway: 'Wellbeing support pathway',
      message: 'Elevated wellbeing signals were flagged for authorized support review. Humans decide the appropriate response.',
    }
  }
  return { needed: false, pathway: null, message: null }
}

export function analyzeCheckin(chatLog, previousCheckins = []) {
  const userMessages = (chatLog || [])
    .filter((m) => m.role === 'user')
    .map((m) => String(m.text || '').toLowerCase())
  const allText = userMessages.join(' ')
  const detectedSignals = []
  let totalWeight = 0

  Object.entries(SIGNAL_DEFINITIONS).forEach(([signalKey, def]) => {
    const matchedKeywords = def.keywords.filter((kw) => allText.includes(kw))
    if (matchedKeywords.length > 0) {
      detectedSignals.push({
        signal: signalKey,
        label: def.label,
        weight: def.weight,
        matchCount: matchedKeywords.length,
        keywords: matchedKeywords,
      })
      totalWeight += def.weight * Math.min(matchedKeywords.length, 3)
    }
  })

  const lastScore = previousCheckins.length > 0 ? previousCheckins[previousCheckins.length - 1].score : 25
  let baseScore = Math.min(100, 20 + totalWeight)

  if (previousCheckins.length >= 2) {
    const recentScores = previousCheckins.slice(-3).map((c) => c.score)
    const isWorsening = recentScores.every((s, i) => i === 0 || s >= recentScores[i - 1])
    if (isWorsening && detectedSignals.length > 0) {
      baseScore = Math.min(100, baseScore + 5)
      detectedSignals.push({
        signal: 'worsening_trend',
        label: 'Worsening compared with previous check-ins',
        weight: 5,
        matchCount: 1,
        keywords: [],
      })
    }
  }

  if (detectedSignals.length === 0) {
    baseScore = Math.max(15, lastScore - 5)
  }

  const score = Math.max(0, Math.min(100, Math.round(baseScore)))
  const safetyConcern = detectedSignals.some((s) => s.signal === 'feeling_unsafe' || s.signal === 'threat_mentions')
  const concernType = classifyConcernType(detectedSignals, safetyConcern)
  const concernLevel = concernLevelFromScore(score)
  const contributingSignals = detectedSignals
    .sort((a, b) => b.weight - a.weight)
    .slice(0, 5)
    .map((s) => s.label)

  const trend = score > lastScore ? 'rising' : score < lastScore ? 'falling' : 'stable'
  const supportRouting = supportPathway(concernType, concernLevel)

  return {
    score,
    distressScore: score,
    concernType,
    concernLevel,
    concern_level: concernLevelKey(score),
    contributing_signals: contributingSignals,
    detected_signals: detectedSignals,
    safety_concern: safetyConcern,
    previous_score: lastScore,
    trend,
    trend_direction: trend,
    supportRouting,
    analysis_timestamp: new Date().toISOString(),
    disclaimer: 'This is a wellbeing concern indicator, NOT a clinical diagnosis.',
  }
}

export function getLocalFallbackResponse(chatHistory, userMessage, tFn) {
  const msg = String(userMessage || '').toLowerCase()
  const turnCount = (chatHistory || []).filter((m) => m.role === 'user').length
  const tr = tFn || ((k, fallback) => fallback)

  if (turnCount === 0) return tr('ai_response_greeting', 'Hi. How are you feeling today?')
  if (msg.includes('sleep') || msg.includes('nightmare') || msg.includes('tired')) {
    return tr('ai_response_sleep', 'Thank you for sharing that. Sleep difficulties can be really tough. How long has this been affecting you?')
  }
  if (msg.includes('scared') || msg.includes('afraid') || msg.includes('fear') || msg.includes('unsafe')) {
    return tr('ai_response_fear', 'I hear you, and your feelings are valid. Is there something specific that has been making you feel this way recently?')
  }
  if (msg.includes('worry') || msg.includes('anxious') || msg.includes('stress') || msg.includes('nervous')) {
    return tr('ai_response_worry', 'That sounds difficult. What has been on your mind the most?')
  }
  if (msg.includes('hearing') || msg.includes('court') || msg.includes('trial') || msg.includes('case')) {
    return tr('ai_response_case', 'It is natural to feel concerned about your case. How has this been affecting your daily life?')
  }
  if (msg.includes('fine') || msg.includes('okay') || msg.includes('good') || msg.includes('better')) {
    return tr('ai_response_positive', 'I am glad to hear that. Is there anything that has been helping you feel this way?')
  }
  if (msg.includes('alone') || msg.includes('lonely') || msg.includes('isolated')) {
    return tr('ai_response_isolation', 'Feeling alone can be very hard. Do you have someone you trust that you can talk to?')
  }
  if (turnCount >= 4) {
    return tr('ai_response_closing', 'Thank you for sharing with me today. Your wellbeing matters, and your support team is here for you. Is there anything else you would like to mention before we finish?')
  }
  const followUps = [
    'Thank you for sharing that. How has this been affecting your sleep or daily routine?',
    'I appreciate you telling me. Has anything changed since your last check-in?',
    'That sounds challenging. How are you coping day to day?',
    'Thank you. Is there anything else that has been on your mind?',
  ]
  return followUps[turnCount % followUps.length]
}
