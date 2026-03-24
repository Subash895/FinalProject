/* ============================================================
   SMART CITY — news.js
   News feed logic
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {

  document.getElementById("newsForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Publishing...';

    try {
      await apiRequest("/news", "POST", {
        title:   document.getElementById("title").value,
        content: document.getElementById("content").value
      });
      e.target.reset();
      await loadNews();
    } catch (err) {
      console.error("Publish news failed:", err);
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Publish Article";
    }
  });

  document.getElementById("loadBtn")?.addEventListener("click", loadNews);
});

async function loadNews() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/news");

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">📰</div>
          <p>No news articles yet. Publish the first one!</p>
        </div>`;
      return;
    }

    container.className = "news-list";
    container.innerHTML = data.map((n, i) => `
      <div class="news-card glass-card" style="animation-delay:${i * 0.05}s">
        <div>
          <h3>${n.title}</h3>
          <div class="news-body">${n.content || ""}</div>
          <div class="news-timestamp">📅 Published recently</div>
        </div>
        <div class="news-label">
          <span class="news-tag">City News</span>
        </div>
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load news. Is the server running?</p>
      </div>`;
  }
}