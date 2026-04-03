/* ============================================================
   SMART CITY — api.js  (Shared API utility)
   ============================================================ */

const API_BASE =
    window.SMARTCITY_API_BASE ||
    document.querySelector('meta[name="smartcity-api-base"]')?.content ||
    localStorage.getItem("smartcity.apiBase") ||
    (window.location.origin && window.location.origin !== "null"
        ? `${window.location.origin}/api`
        : "http://localhost:8080/api");

async function apiRequest(endpoint, method = "GET", data = null) {
    const options = {
        method,
        headers: { "Content-Type": "application/json" }
    };

    const token = localStorage.getItem("token");
    if (token) options.headers["Authorization"] = "Bearer " + token;

    if (data) options.body = JSON.stringify(data);

    const res = await fetch(API_BASE + endpoint, options);

    if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);

    // DELETE often returns 204 No Content — don't try to parse JSON
    if (res.status === 204 || res.headers.get("content-length") === "0") return null;

    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) return res.json();

    return null;
}
