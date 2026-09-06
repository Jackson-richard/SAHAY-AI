const url = "https://amxcyyzbdanofrkoveoo.supabase.co";
const key = "sb_publishable_3UZv39ws8H0V-keHUZIHdA_MZhNPok1";

async function run() {
    console.log("=== Signup ===");
    let res = await fetch(`${url}/auth/v1/signup`, {
        method: "POST",
        headers: { "apikey": key, "Content-Type": "application/json" },
        body: JSON.stringify({ email: "demo@sahay.gov.in", password: "admin123" })
    });
    console.log("Status:", res.status);
    console.log("Body:", await res.text());
}
run();
