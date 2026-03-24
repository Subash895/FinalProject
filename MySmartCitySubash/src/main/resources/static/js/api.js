/* ============================================================
   SMART CITY — api.js
   Shared API request utility
   ============================================================ */

const API_BASE = "http://localhost:8080/api";

async function apiRequest(endpoint, method = "GET", data = null) {
  const options = {
    method,
    headers: { "Content-Type": "application/json" }
  };

  const token = localStorage.getItem("token");
  if (token) {
    options.headers["Authorization"] = "Bearer " + token;
  }

  if (data) {
    options.body = JSON.stringify(data);
  }

  const res = await fetch(API_BASE + endpoint, options);

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }

  return res.json();
}