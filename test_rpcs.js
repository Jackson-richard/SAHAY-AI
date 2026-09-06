const url = "https://amxcyyzbdanofrkoveoo.supabase.co";
const key = "sb_publishable_3UZv39ws8H0V-keHUZIHdA_MZhNPok1";

async function run() {
    console.log("=== 1. validate_case_existence SHY-1002 ===");
    let res1 = await fetch(`${url}/rest/v1/rpc/validate_case_existence`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_case_id: "SHY-1002" })
    });
    console.log("Status:", res1.status, await res1.text());

    console.log("=== 1b. validate_case_existence SHY-9999 ===");
    let res1b = await fetch(`${url}/rest/v1/rpc/validate_case_existence`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ p_case_id: "SHY-9999" })
    });
    console.log("Status:", res1b.status, await res1b.text());

    // Creating SHY-1002 to ensure it exists so register_victim works
    // Note: The schema.sql doesn't prepopulate SHY-1002 in `cases` table! Wait...
    console.log("=== 2. Admin Login ===");
    let res3 = await fetch(`${url}/auth/v1/token?grant_type=password`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ email: "demo@sahay.gov.in", password: "admin123" })
    });
    console.log("Status:", res3.status);
    let authBody = await res3.json();

    if (authBody.access_token) {
        console.log("Admin Token Received!");
        // Let's manually create SHY-1002 in public.cases so Victim can register
        let dbRes = await fetch(`${url}/rest/v1/cases`, {
            method: "POST", headers: { "apikey": key, "Authorization": "Bearer " + authBody.access_token, "Content-Type": "application/json", "Prefer": "return=representation" },
            body: JSON.stringify({
                case_id: "SHY-1002",
                status: "CREATED", priority: "High", department: "Testing", support_category: "Test"
            })
        });
        console.log("Admin Create Case:", dbRes.status, await dbRes.text());
    }

    console.log("=== 3. register_victim ===");
    let res2 = await fetch(`${url}/rest/v1/rpc/register_victim`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({
            p_case_id: "SHY-1002", p_name: "Test Victim", p_phone: "1234567890",
            p_trusted_phone: "", p_language: "English", p_consent_given: true
        })
    });
    console.log("Status:", res2.status);
    let regBody = await res2.json();
    console.log(regBody);

    console.log("=== 3b. duplicate register_victim ===");
    let res2b = await fetch(`${url}/rest/v1/rpc/register_victim`, {
        method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({
            p_case_id: "SHY-1002", p_name: "Dup Victim", p_phone: "000",
            p_trusted_phone: "", p_language: "English", p_consent_given: true
        })
    });
    console.log("Status:", res2b.status, await res2b.text());

    if (regBody.session_token) {
        console.log("=== 4. get_victim_dashboard ===");
        let dashRes = await fetch(`${url}/rest/v1/rpc/get_victim_dashboard`, {
            method: "POST", headers: { "apikey": key, "Content-Type": "application/json" },
            body: JSON.stringify({ p_session_token: regBody.session_token })
        });
        console.log("Status:", dashRes.status, await dashRes.text());
    }
}
run();
