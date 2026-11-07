const url = "https://amxcyyzbdanofrkoveoo.supabase.co";
const key = "sb_publishable_3UZv39ws8H0V-keHUZIHdA_MZhNPok1";

async function runTests() {
    console.log("=== STARTING END-TO-END VERIFICATION ===");

    // -----------------------------------------
    // TEST 1 -- AUTHENTICATION (Victim Login/Recovery)
    // -----------------------------------------
    console.log("\n[TEST 1] Testing Victim Registration / Recovery...");
    let authRes = await fetch(`${url}/rest/v1/rpc/register_victim`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({
            p_case_id: "SHY-1002", p_name: "Test Victim", p_phone: "1234567890",
            p_trusted_phone: "", p_language: "English", p_consent_given: true
        })
    });
    let authJson = await authRes.json();
    let sessionToken = authJson.session_token;

    // If already registered, it returns error. Let's recover.
    if (!sessionToken && authJson.error) {
        console.log("Already registered, recovering session...");
        let recRes = await fetch(`${url}/rest/v1/rpc/recover_victim_session`, {
            method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
            body: JSON.stringify({ p_case_id: "SHY-1002", p_phone: "1234567890" })
        });
        let recJson = await recRes.json();
        sessionToken = recJson.session_token;
    }

    console.log(`[TEST 1] Session Token Obtained: ${sessionToken ? 'PASS' : 'FAIL'} (${sessionToken})`);

    // -----------------------------------------
    // TEST 2 -- CHAT PERSISTENCE
    // -----------------------------------------
    console.log("\n[TEST 2] Testing Chat Persistence (Edge Function)...");
    let chatReq = await fetch(`${url}/functions/v1/chat-analysis`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}`, "Content-Type": "application/json" },
        body: JSON.stringify({
            context: "Test context",
            message: "Hello, this is a persistence test.",
            assessment_period_id: "DEFAULT",
            input_type: "CHAT"
        })
    });

    let chatJson = await chatReq.json();
    console.log(`[TEST 2] AI Response:`, chatJson.reply);

    // Using our new secure RPC to fetch chat history!
    let historyReq = await fetch(`${url}/rest/v1/rpc/get_victim_chat_history`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_session_token: sessionToken })
    });
    let history = await historyReq.json();

    console.log(`[TEST 2] History length: ${history.length}. Chat Persistence Verification:`);
    let recentMsgs = history.slice(-2);
    console.log(recentMsgs);
    let test2Pass = recentMsgs.length === 2 && recentMsgs[0].message === "Hello, this is a persistence test." && recentMsgs[1].ai_analysis_summary.input_type === "CHAT";
    console.log(`[TEST 2 RESULT]: ${test2Pass ? 'PASS' : 'FAIL'}`);


    // -----------------------------------------
    // TEST 5 -- DISTRESS ANALYSIS FROM CHAT
    // -----------------------------------------
    console.log("\n[TEST 5] Distress Analysis From Chat...");
    let distressReq = await fetch(`${url}/functions/v1/chat-analysis`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}`, "Content-Type": "application/json" },
        body: JSON.stringify({
            context: "Test context",
            message: "I have been feeling very sad and overwhelmed lately. I am scared about what is happening and I haven't been sleeping properly.",
            assessment_period_id: "DEFAULT",
            input_type: "CHAT"
        })
    });
    let distressJson = await distressReq.json();
    console.log(`[TEST 5] Structured Indicators:`, distressJson.structured_indicators);


    // -----------------------------------------
    // TEST 6 -- DISTRESS ANALYSIS FROM VOICE
    // -----------------------------------------
    console.log("\n[TEST 6] Distress Analysis From Voice...");
    let voiceReq = await fetch(`${url}/functions/v1/chat-analysis`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}`, "Content-Type": "application/json" },
        body: JSON.stringify({
            context: "Test context",
            message: "I am scared and stressed every day and I feel like things are getting worse.",
            assessment_period_id: "DEFAULT",
            input_type: "VOICE"
        })
    });
    let voiceJson = await voiceReq.json();
    console.log(`[TEST 6] Voice Structured Indicators:`, voiceJson.structured_indicators);

    let histReq2 = await fetch(`${url}/rest/v1/rpc/get_victim_chat_history`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_session_token: sessionToken })
    });
    let updatedHistory = await histReq2.json();
    let voiceMsg = updatedHistory[updatedHistory.length - 1];
    console.log(`[TEST 6] Voice persistence verification input_type = ${voiceMsg.ai_analysis_summary.input_type}`);


    // -----------------------------------------
    // TEST 13 -- RLS / SECURITY RPC VALIDATION
    // -----------------------------------------
    console.log("\n[TEST 13] Cross-User RLS & RPC Security check...");
    // Try to fetch another person's history using a fake UUID token
    let fakeToken = "00000000-0000-0000-0000-000000000000";
    let fakeHistoryReq = await fetch(`${url}/rest/v1/rpc/get_victim_chat_history`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_session_token: fakeToken })
    });
    let fakeHistory = await fakeHistoryReq.json();
    console.log(`[TEST 13] Attempted to read with invalid token. Result length: ${fakeHistory.length}. PASS = ${fakeHistory.length === 0}`);

    // Check if anon access to chat_messages via REST is still blocked
    let anonChatReq = await fetch(`${url}/rest/v1/chat_messages`, {
        method: "GET", headers: { "apikey": key, "Authorization": "Bearer " + key }
    });
    let anonChat = await Object.keys((await anonChatReq.json())).length;
    console.log(`[TEST 13] Attempted direct anon read on chat_messages. Result length: ${anonChat}. PASS = ${anonChat === 0}`);

}

runTests().catch(console.error);
