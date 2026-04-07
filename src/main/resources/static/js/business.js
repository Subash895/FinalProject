/* ============================================================
   SMART CITY - business.js
   ============================================================ */

function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

function getBusinessSearchQuery() {
    return document.getElementById("businessSearch")?.value.trim() || "";
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("businessForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Adding...';
        try {
            await apiRequest("/businesses", "POST", {
                name: document.getElementById("name").value.trim(),
                address: document.getElementById("address").value.trim(),
                description: document.getElementById("description").value.trim()
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
    document.getElementById("businessSearch")?.addEventListener("input", loadBusinesses);
    loadBusinesses();
});

async function loadBusinesses() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const query = getBusinessSearchQuery();
        const endpoint = query ? `/businesses?q=${encodeURIComponent(query)}` : "/businesses";
        const data = await apiRequest(endpoint);
        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">BS</span><p>${query ? "No businesses match your search." : "No businesses yet."}</p></div>`;
            return;
        }

        const businessesWithReviews = await Promise.all(data.map(async business => ({
            ...business,
            reviews: await loadReviews(REVIEW_TARGETS.business, business.id)
        })));

        container.className = "card-list";
        container.innerHTML = businessesWithReviews.map((b, i) => {
            const canEdit = isAdmin() || (isBusiness() && b.owner && b.owner.id === getUserId());
            const canDelete = isAdmin();

            return `
      <div class="business-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="card-tag">${b.owner?.name || "Business"}</div>
        <h3>${b.name}</h3>
        <div class="biz-meta">
          <span><span class="meta-icon">Address</span>${b.address || "-"}</span>
          <span><span class="meta-icon">About</span>${b.description || "-"}</span>
        </div>
        <div class="card-actions">
          ${canEdit ? `<button class="btn btn-edit btn-sm" onclick='editBusiness(${b.id}, ${JSON.stringify(b)})'>Edit</button>` : ""}
          ${canDelete ? `<button class="btn btn-delete btn-sm" onclick="deleteBusiness(${b.id}, '${esc(b.name)}')">Delete</button>` : ""}
        </div>
        ${renderReviewSection(REVIEW_TARGETS.business, b.id, b.reviews || [])}
      </div>`;
        }).join("");

        hydrateReviewForms(loadBusinesses);
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NA</span><p>Cannot connect to server. Is it running on port 8080?</p></div>';
    }
}

function editBusiness(id, b) {
    openEditModal({
        title: "Edit Business",
        fields: [
            { key: "name", label: "Business Name", placeholder: "e.g. Sunrise Bakery" },
            { key: "address", label: "Address", placeholder: "e.g. 12 Main St" },
            { key: "description", label: "Description", placeholder: "e.g. Fresh bakery, coffee, and snacks" }
        ],
        values: { name: b.name, address: b.address, description: b.description },
        onSave: async (data) => { await apiRequest(`/businesses/${id}`, "PUT", data); await loadBusinesses(); }
    });
}

function deleteBusiness(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => { await apiRequest(`/businesses/${id}`, "DELETE"); await loadBusinesses(); }
    });
}
