/* ============================================================
   SMART CITY — place.js  (Full CRUD)
   ============================================================ */

const PLACE_ICONS = {
    park: "🌳", museum: "🏛️", restaurant: "🍽️", hospital: "🏥",
    school: "🏫", mall: "🛍️", temple: "⛩️", beach: "🏖️", default: "📍"
};

function getPlaceIcon(cat = "") {
    const k = cat.toLowerCase();
    for (const [key, val] of Object.entries(PLACE_ICONS)) {
        if (key !== "default" && k.includes(key)) return val;
    }
    return PLACE_ICONS.default;
}

function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("placeForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Adding...';
        try {
            await apiRequest("/places", "POST", {
                name: document.getElementById("name").value.trim(),
                category: document.getElementById("category").value.trim(),
                location: document.getElementById("location").value.trim()
            });
            e.target.reset();
            showToast("Place added!", "success");
            await loadPlaces();
        } catch {
            showToast("Failed to add place.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Add Place";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadPlaces);
    loadPlaces();
});

async function loadPlaces() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';
    try {
        const data = await apiRequest("/places");
        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">📍</span><p>No places yet. Add the first one!</p></div>`;
            return;
        }
        container.className = "place-list";
        container.innerHTML = data.map((p, i) => `
      <div class="place-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="place-top">
          <div class="place-icon-wrap">${getPlaceIcon(p.category)}</div>
          <div>
            <h3>${p.name}</h3>
            <span class="place-cat">${p.category || "Place"}</span>
          </div>
        </div>
        <div class="place-loc">📍 ${p.location || "—"}</div>
        <div class="card-actions">
          <button class="btn btn-edit btn-sm" onclick='editPlace(${p.id}, ${JSON.stringify(p)})'>✏️ Edit</button>
          <button class="btn btn-delete btn-sm" onclick="deletePlace(${p.id}, '${esc(p.name)}')">🗑️ Delete</button>
        </div>
      </div>`).join("");
    } catch {
        container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">⚠️</span><p>Cannot connect to server.</p></div>`;
    }
}

function editPlace(id, p) {
    openEditModal({
        title: "Edit Place",
        fields: [
            { key: "name", label: "Place Name", placeholder: "e.g. Cubbon Park" },
            { key: "category", label: "Category", placeholder: "e.g. Park" },
            { key: "location", label: "Location", placeholder: "e.g. MG Road, Bengaluru" }
        ],
        values: { name: p.name, category: p.category, location: p.location },
        onSave: async (data) => { await apiRequest(`/places/${id}`, "PUT", data); await loadPlaces(); }
    });
}

function deletePlace(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => { await apiRequest(`/places/${id}`, "DELETE"); await loadPlaces(); }
    });
}