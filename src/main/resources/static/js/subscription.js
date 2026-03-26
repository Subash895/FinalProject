/* ============================================================
   SMART CITY — subscription.js  (Full CRUD)
   ============================================================ */

const PLAN_BADGE = { free: "badge-green", pro: "badge-cyan", enterprise: "badge-purple" };
function getPlanBadge(t = "") { return PLAN_BADGE[t.toLowerCase()] || "badge-cyan"; }
function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("subscriptionForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Activating...';
        try {
            await apiRequest("/subscriptions", "POST", {
                email: document.getElementById("email").value.trim(),
                type: document.getElementById("type").value.trim().toUpperCase()
            });
            e.target.reset();
            showToast("Subscription activated!", "success");
            await loadSubs();
        } catch {
            showToast("Failed to subscribe.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Activate Subscription";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadSubs);
    loadSubs();
});

async function loadSubs() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';
    try {
        const data = await apiRequest("/subscriptions");
        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">⭐</span><p>No subscriptions yet.</p></div>`;
            return;
        }
        container.className = "sub-list";
        container.innerHTML = data.map((s, i) => `
      <div class="sub-card glass-card" style="animation-delay:${i * 0.04}s">
        <div>
          <div class="sub-email">${s.email}</div>
          <div class="sub-type">${s.type || "Standard"} Plan</div>
        </div>
        <div class="sub-actions">
          <span class="badge ${getPlanBadge(s.type)}">${s.type || "Active"}</span>
          <button class="btn btn-edit btn-sm" onclick='editSub(${s.id}, ${JSON.stringify(s)})'>✏️</button>
          <button class="btn btn-delete btn-sm" onclick="deleteSub(${s.id}, '${esc(s.email)}')">🗑️</button>
        </div>
      </div>`).join("");
    } catch {
        container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">⚠️</span><p>Cannot connect to server.</p></div>`;
    }
}

function editSub(id, s) {
    openEditModal({
        title: "Edit Subscription",
        fields: [
            { key: "email", label: "Email", placeholder: "email@example.com", type: "email" },
            { key: "type", label: "Plan Type", placeholder: "FREE / PRO / ENTERPRISE" }
        ],
        values: { email: s.email, type: s.type },
        onSave: async (data) => { await apiRequest(`/subscriptions/${id}`, "PUT", data); await loadSubs(); }
    });
}

function deleteSub(id, email) {
    openDeleteModal({
        itemName: email,
        onConfirm: async () => { await apiRequest(`/subscriptions/${id}`, "DELETE"); await loadSubs(); }
    });
}