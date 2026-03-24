/* ============================================================
   SMART CITY — city.js
   City directory logic
   ============================================================ */

const CITY_EMOJIS = ["🏙️","🌆","🌇","🌃","🏛️","🗼","🌉","🏰"];

document.addEventListener("DOMContentLoaded", () => {

  document.getElementById("cityForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Adding...';

    try {
      await apiRequest("/cities", "POST", {
        name:    document.getElementById("name").value,
        country: document.getElementById("country").value
      });
      e.target.reset();
      await loadCities();
    } catch (err) {
      console.error("Add city failed:", err);
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Add City";
    }
  });

  document.getElementById("loadBtn")?.addEventListener("click", loadCities);
});

async function loadCities() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/cities");

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">🏛️</div>
          <p>No cities found. Add the first one!</p>
        </div>`;
      return;
    }

    container.className = "city-list";
    container.innerHTML = data.map((c, i) => `
      <div class="city-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="city-avatar">${CITY_EMOJIS[i % CITY_EMOJIS.length]}</div>
        <div class="city-info">
          <h3>${c.name}</h3>
          <div class="city-country">${c.country}</div>
        </div>
        <div style="margin-left:auto">
          <span class="badge badge-cyan">Active</span>
        </div>
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load cities. Is the server running?</p>
      </div>`;
  }
}