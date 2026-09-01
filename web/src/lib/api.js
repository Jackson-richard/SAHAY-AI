const API_BASE = import.meta.env.VITE_API_BASE || ''

export async function fetchStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/status`)
    if (!res.ok) throw new Error('status failed')
    return await res.json()
  } catch {
    return { groqConfigured: false, supabaseConfigured: false, fallback: true }
  }
}

export async function requestChatReply({ chatHistory, userMessage, language }) {
  try {
    const res = await fetch(`${API_BASE}/api/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ chatHistory, userMessage, language }),
    })
    if (!res.ok) throw new Error('chat failed')
    return await res.json()
  } catch (e) {
    return { error: e.message, fallback: true }
  }
}

export async function requestAnalysis({ chatLog, previousCheckins, channel, language }) {
  try {
    const res = await fetch(`${API_BASE}/api/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ chatLog, previousCheckins, channel, language }),
    })
    if (!res.ok) throw new Error('analyze failed')
    return await res.json()
  } catch {
    return null
  }
}
