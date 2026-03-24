/* ============================================================
   SMART CITY — place.js
   Places directory logic
   ============================================================ */

const PLACE_ICONS = {
  park:       "🌳",
  museum:     "🏛️",
  restaurant: "🍽️",
  hospital:   "🏥",
  school:     "🏫",
  mall:       "🛍️",
  temple:     "⛩️",
  beach:      "🏖️",
  default:    "📍"
};

function getPlaceIcon(category = "") {
  const key = category.toLowerCase();
  for (const [k, v] of Object.entries(PLACE_ICONS)) {
    if (key.includes(k)) return v;
  }
  return PLACE_ICONS.default;
}

document.addEventListener("DOMContentLoaded", () => {

  document.getElementById("placeForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Adding...';

    try {
      await apiRequest("/places", "POST", {
        name:     document.getElementById("name").value,
        category: document.getElementById("category").value,
        location: document.getElementById("location").value
      });
      e.target.reset();
      await loadPlaces();
    } catch (err) {
      console.error("Add place failed:", err);
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Add Place";
    }
  });

  document.getElementById("loadBtn")?.addEventListener("click", loadPlaces);
});

async function loadPlaces() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/places");

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">📍</div>
          <p>No places found. Add the first one!</p>
        </div>`;
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
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load places. Is the server running?</p>
      </div>`;
  }
}