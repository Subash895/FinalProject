/* ============================================================
   SMART CITY — subscription.js
   Subscription management logic
   ============================================================ */

const PLAN_BADGE = {
  free:       "badge-green",
  pro:        "badge-cyan",
  enterprise: "badge-purple"
};

function getPlanBadge(type = "") {
  return PLAN_BADGE[type.toLowerCase()] || "badge-cyan";
}

document.addEventListener("DOMContentLoaded", () => {

  document.getElementById("subscriptionForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Activating...';

    try {
      await apiRequest("/subscriptions", "POST", {
        email: document.getElementById("email").value,
        type:  document.getElementById("type").value
      });
      e.target.reset();
      await loadSubs();
    } catch (err) {
      console.error("Subscribe failed:", err);
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Activate Subscription";
    }
  });

  document.getElementById("loadBtn")?.addEventListener("click", loadSubs);
});

async function loadSubs() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/subscriptions");

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">⭐</div>
          <p>No subscriptions yet.</p>
        </div>`;
      return;
    }

    container.className = "sub-list";
    container.innerHTML = data.map((s, i) => `
      <div class="sub-card glass-card" style="animation-delay:${i * 0.04}s">
        <div>
          <div class="sub-email">${s.email}</div>
          <div class="sub-type">${s.type || "Standard"} Plan</div>
        </div>
        <span class="badge ${getPlanBadge(s.type)}">${s.type || "Active"}</span>
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load subscriptions. Is the server running?</p>
      </div>`;
  }
}