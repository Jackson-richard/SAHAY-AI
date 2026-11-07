const url = "https://amxcyyzbdanofrkoveoo.supabase.co";
const key = "sb_publishable_3UZv39ws8H0V-keHUZIHdA_MZhNPok1";

async function runGroqTests() {
    console.log("=== STARTING GROQ VERIFICATION ===");

    // Recover Session
    let recRes = await fetch(`${url}/rest/v1/rpc/recover_victim_session`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_case_id: "SHY-1002", p_phone: "1234567890" })
    });
    let sessionToken = (await recRes.json()).session_token;
    console.log(`[AUTH] Session Token Obtained`);

    // TEST 1
    console.log("\n[TEST 1] Running chat analysis...");
    let req1 = await fetch(`${url}/functions/v1/chat-analysis`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}`, "Content-Type": "application/json" },
        body: JSON.stringify({
            context: "Test 1",
            message: "Hello world",
            assessment_period_id: "DEFAULT",
            input_type: "CHAT"
        })
    });
    let res1 = await req1.json();
    console.log("[TEST 1] AI Response Length:", res1.reply?.length);
    console.log("[TEST 1] AI Response Valid:", !!res1.reply && !res1.error);

    // TEST 2
    console.log("\n[TEST 2] Running chat analysis again...");
    let req2 = await fetch(`${url}/functions/v1/chat-analysis`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}`, "Content-Type": "application/json" },
        body: JSON.stringify({
            context: "Test 2",
            message: "This is the second test.",
            assessment_period_id: "DEFAULT",
            input_type: "CHAT"
        })
    });
    let res2 = await req2.json();
    console.log("[TEST 2] AI Response Length:", res2.reply?.length);
    console.log("[TEST 2] AI Response Valid:", !!res2.reply && !res2.error);
}

runGroqTests().catch(console.error);
