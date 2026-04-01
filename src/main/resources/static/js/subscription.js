const PLAN_BADGE = { free: "badge-green", pro: "badge-cyan", enterprise: "badge-purple" };

function getPlanBadge(type = "") {
    return PLAN_BADGE[String(type).toLowerCase()] || "badge-cyan";
}

function esc(value) {
    return String(value || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function requireAdminAction() {
    if (isAdmin()) {
        return true;
    }
    showToast("Admin access required.", "error");
    return false;
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("subscriptionForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!requireAdminAction()) {
            return;
        }

        const button = event.target.querySelector("button[type=submit]");
        button.disabled = true;
        button.innerHTML = '<span class="spinner"></span> Activating...';

        try {
            await apiRequest("/subscriptions", "POST", {
                email: document.getElementById("email").value.trim(),
                type: document.getElementById("type").value.trim().toUpperCase()
            });
            event.target.reset();
            showToast("Subscription activated!", "success");
            await loadSubs();
        } catch {
            showToast("Failed to subscribe.", "error");
        } finally {
            button.disabled = false;
            button.innerHTML = "Activate Subscription";
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
            container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">S</span><p>No subscriptions yet.</p></div>';
            return;
        }

        container.className = "sub-list";
        container.innerHTML = data.map((subscription, index) => `
      <div class="sub-card glass-card" style="animation-delay:${index * 0.04}s">
        <div>
          <div class="sub-email">${subscription.email}</div>
          <div class="sub-type">${subscription.type || "Standard"} Plan</div>
        </div>
        <div class="sub-actions">
          <span class="badge ${getPlanBadge(subscription.type)}">${subscription.type || "Active"}</span>
          ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editSub(${subscription.id}, ${JSON.stringify(subscription)})'>Edit</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteSub(${subscription.id}, '${esc(subscription.email)}')">Delete</button>` : ""}
        </div>
      </div>`).join("");
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">!</span><p>Cannot connect to server.</p></div>';
    }
}

function editSub(id, subscription) {
    if (!requireAdminAction()) {
        return;
    }

    openEditModal({
        title: "Edit Subscription",
        fields: [
            { key: "email", label: "Email", placeholder: "email@example.com", type: "email" },
            { key: "type", label: "Plan Type", placeholder: "FREE / PRO / ENTERPRISE" }
        ],
        values: { email: subscription.email, type: subscription.type },
        onSave: async (data) => {
            await apiRequest(`/subscriptions/${id}`, "PUT", data);
            await loadSubs();
        }
    });
}

function deleteSub(id, email) {
    if (!requireAdminAction()) {
        return;
    }

    openDeleteModal({
        itemName: email,
        onConfirm: async () => {
            await apiRequest(`/subscriptions/${id}`, "DELETE");
            await loadSubs();
        }
    });
}
