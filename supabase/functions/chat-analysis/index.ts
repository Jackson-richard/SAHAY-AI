import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      throw new Error("Missing Authorization header (JWT required)")
    }

    const { context, message, assessment_period_id, input_type } = await req.json()
    const GROQ_API_KEY = Deno.env.get('GROQ_API_KEY')
    const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
    const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

    if (!GROQ_API_KEY) throw new Error("GROQ_API_KEY missing in Edge Function secrets")

    const session_token = authHeader.replace('Bearer ', '').trim()
    const adminClient = createClient(SUPABASE_URL ?? '', SUPABASE_SERVICE_ROLE_KEY ?? '')

    // Securely derive case ID from the validated session token
    const { data: ownership } = await adminClient
      .from('profiles')
      .select('case_id, preferred_language')
      .eq('id', session_token)
      .single()

    if (!ownership || !ownership.case_id) throw new Error("Unauthorized: Invalid session token")
    const case_id = ownership.case_id
    const userLanguage = ownership.preferred_language || 'English';

    // 2. Transmit string context safely to Groq for analysis
    const contextStr = typeof context === 'string' ? context.substring(0, 500) : "General context";
    const msgStr = typeof message === 'string' ? message.substring(0, 1000) : "Empty format";

    // Explicit Language instruction logic
    const languageInstruction = userLanguage === 'Tamil'
      ? "Respond only in natural Tamil. Do not respond in English unless the user explicitly requests English."
      : `Respond in ${userLanguage}.`;

    const groqRes = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${GROQ_API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: "groq/compound-mini",
        messages: [
          { role: "system", content: `You are an empathetic, calm, supportive, and non-judgmental AI assistant. Do not pretend to be human. Do not give medical or legal diagnosis. Output a valid JSON response exactly like this format, providing BOTH a reply string for the user and structured analysis of the message. ${languageInstruction} JSON Format: { "reply": "Your empathetic response here.", "structured_indicators": { "emotional_distress_level": 5.0, "fear_level": 5.0, "feeling_unsafe": false, "threat_indicators": "none", "urgency": "low", "recommended_support_category": "Psychological wellbeing", "summary": "User is asking general questions." } }` },
          { role: "user", content: `Context: ${contextStr}. Message: ${msgStr}` }
        ],
        temperature: 0.2,
        response_format: { type: "json_object" }
      })
    })

    const data = await groqRes.json()
    if (!data.choices || !data.choices[0]) {
      throw new Error(`Groq API Error: ${JSON.stringify(data)}`);
    }
    const analysisJson = JSON.parse(data.choices[0].message.content)

    const actualInputType = input_type || 'CHAT';

    // Inject it into the unstructured JSONB field since we can't easily run schema migrations
    analysisJson.structured_indicators = analysisJson.structured_indicators || {};
    analysisJson.structured_indicators.input_type = actualInputType;

    const currentWeekId = 'WEEK_' + Math.floor(Date.now() / 1000 / 604800)
    const finalPeriodId = (assessment_period_id && assessment_period_id !== 'DEFAULT') ? assessment_period_id : currentWeekId

    // Insert the chat record securely
    await adminClient.from('chat_messages').insert({
      case_id: case_id,
      assessment_period_id: finalPeriodId,
      sender: 'User',
      message: message,
      ai_analysis_summary: analysisJson.structured_indicators
    })

    // Insert bot reply with the SAME input mode to ensure the UI knows how to handle the reply
    await adminClient.from('chat_messages').insert({
      case_id: case_id,
      assessment_period_id: finalPeriodId,
      sender: 'AI',
      message: analysisJson.reply,
      ai_analysis_summary: { input_type: actualInputType }
    })

    return new Response(JSON.stringify(analysisJson), { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 200 })
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 401 })
  }
})
