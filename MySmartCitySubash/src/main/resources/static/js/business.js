/* ============================================================
   SMART CITY — business.js
   Business directory logic
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {

  const form = document.getElementById("businessForm");
  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = form.querySelector("button[type=submit]");
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner"></span> Adding...';

      try {
        await apiRequest("/businesses", "POST", {
          name:     document.getElementById("name").value,
          category: document.getElementById("category").value,
          address:  document.getElementById("address").value
        });
        form.reset();
        await loadBusinesses();
      } catch (err) {
        console.error("Add business failed:", err);
      } finally {
        btn.disabled = false;
        btn.innerHTML = "Add Business";
      }
    });
  }

  document.getElementById("loadBtn")?.addEventListener("click", loadBusinesses);
});

async function loadBusinesses() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/businesses");

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">💼</div>
          <p>No businesses found yet. Be the first to add one!</p>
        </div>`;
      return;
    }

    container.className = "card-list";
    container.innerHTML = data.map((b, i) => `
      <div class="business-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="card-tag">${b.category || "General"}</div>
        <h3>${b.name}</h3>
        <div class="biz-meta">
          <span><span class="meta-icon">🏷️</span>${b.category || "—"}</span>
          <span><span class="meta-icon">📍</span>${b.address || "—"}</span>
        </div>
        <div class="card-actions">
          <button class="btn btn-secondary btn-sm">View Details</button>
        </div>
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load businesses. Is the server running?</p>
      </div>`;
  }
}