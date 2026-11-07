import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
    if (req.method === 'OPTIONS') {
        return new Response('ok', { headers: corsHeaders })
    }

    try {
        const GROQ_API_KEY = Deno.env.get('GROQ_API_KEY')
        if (!GROQ_API_KEY) throw new Error("GROQ_API_KEY missing");

        const groqRes = await fetch("https://api.groq.com/openai/v1/models", {
            headers: { "Authorization": `Bearer ${GROQ_API_KEY}` }
        })

        const data = await groqRes.json()
        return new Response(JSON.stringify(data), { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 200 })
    } catch (error) {
        return new Response(JSON.stringify({ error: error.message }), { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 400 })
    }
})
