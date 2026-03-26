/* ============================================================
   SMART CITY — business.js  (Full CRUD)
   ============================================================ */

function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("businessForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Adding...';
        try {
            await apiRequest("/businesses", "POST", {
                name:     document.getElementById("name").value.trim(),
                category: document.getElementById("category").value.trim(),
                address:  document.getElementById("address").value.trim()
            });
            e.target.reset();
            showToast("Business added!", "success");
            await loadBusinesses();
        } catch {
            showToast("Failed to add business.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Add Business";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadBusinesses);
    loadBusinesses();
});

async function loadBusinesses() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';
    try {
        const data = await apiRequest("/businesses");
        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">💼</span><p>No businesses yet. Add one above!</p></div>`;
            return;
        }
        container.className = "card-list";
        container.innerHTML = data.map((b, i) => {
            // Show Edit if: ADMIN, or BUSINESS user who owns this business
            const canEdit = isAdmin() || (isBusiness() && b.owner && b.owner.id === getUserId());
            // Show Delete if: ADMIN only
            const canDelete = isAdmin();
            return `
      <div class="business-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="card-tag">${b.category || "General"}</div>
        <h3>${b.name}</h3>
        <div class="biz-meta">
          <span><span class="meta-icon">🏷️</span>${b.category || "—"}</span>
          <span><span class="meta-icon">📍</span>${b.address || "—"}</span>
        </div>
        <div class="card-actions">
          ${canEdit   ? `<button class="btn btn-edit btn-sm" onclick='editBusiness(${b.id}, ${JSON.stringify(b)})'>✏️ Edit</button>` : ""}
          ${canDelete ? `<button class="btn btn-delete btn-sm" onclick="deleteBusiness(${b.id}, '${esc(b.name)}')">🗑️ Delete</button>` : ""}
        </div>
      </div>`;
        }).join("");
    } catch {
        container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">⚠️</span><p>Cannot connect to server. Is it running on port 8080?</p></div>`;
    }
}

function editBusiness(id, b) {
    openEditModal({
        title: "Edit Business",
        fields: [
            { key: "name",     label: "Business Name", placeholder: "e.g. Sunrise Bakery" },
            { key: "category", label: "Category",      placeholder: "e.g. Food & Beverage" },
            { key: "address",  label: "Address",       placeholder: "e.g. 12 Main St" }
        ],
        values: { name: b.name, category: b.category, address: b.address },
        onSave: async (data) => { await apiRequest(`/businesses/${id}`, "PUT", data); await loadBusinesses(); }
    });
}

function deleteBusiness(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => { await apiRequest(`/businesses/${id}`, "DELETE"); await loadBusinesses(); }
    });
}
