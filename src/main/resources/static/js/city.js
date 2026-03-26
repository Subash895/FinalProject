/* ============================================================
   SMART CITY — city.js  (Full CRUD)
   ============================================================ */

const CITY_EMOJIS = ["🏙️", "🌆", "🌇", "🌃", "🏛️", "🗼", "🌉", "🏰"];
function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("cityForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Adding...';
        try {
            await apiRequest("/cities", "POST", {
                name:    document.getElementById("name").value.trim(),
                country: document.getElementById("country").value.trim()
            });
            e.target.reset();
            showToast("City added!", "success");
            await loadCities();
        } catch {
            showToast("Failed to add city.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Add City";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadCities);
    loadCities();
});

async function loadCities() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';
    try {
        const data = await apiRequest("/cities");
        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">🏛️</span><p>No cities yet. Add the first one!</p></div>`;
            return;
        }
        container.className = "city-list";
        container.innerHTML = data.map((c, i) => `
      <div class="city-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="city-avatar">${CITY_EMOJIS[i % CITY_EMOJIS.length]}</div>
        <div class="city-info">
          <h3>${c.name}</h3>
          <div class="city-country">📍 ${c.country}</div>
        </div>
        <div class="city-actions">
          <span class="badge badge-cyan">Active</span>
          ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editCity(${c.id}, ${JSON.stringify(c)})'>✏️</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteCity(${c.id}, '${esc(c.name)}')">🗑️</button>` : ""}
        </div>
      </div>`).join("");
    } catch {
        container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">⚠️</span><p>Cannot connect to server.</p></div>`;
    }
}

function editCity(id, c) {
    openEditModal({
        title: "Edit City",
        fields: [
            { key: "name",    label: "City Name", placeholder: "e.g. Bengaluru" },
            { key: "country", label: "Country",   placeholder: "e.g. India" }
        ],
        values: { name: c.name, country: c.country },
        onSave: async (data) => { await apiRequest(`/cities/${id}`, "PUT", data); await loadCities(); }
    });
}

function deleteCity(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => { await apiRequest(`/cities/${id}`, "DELETE"); await loadCities(); }
    });
}
