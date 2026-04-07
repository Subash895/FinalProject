/* ============================================================
   SMART CITY - news.js
   ============================================================ */

function esc(s) { return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'"); }

function newsTimestamp(news) {
    return formatReviewDate(news.createdAt);
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("newsForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Publishing...';
        try {
            await apiRequest("/news", "POST", {
                title: document.getElementById("title").value.trim(),
                content: document.getElementById("content").value.trim()
            });
            e.target.reset();
            showToast("Article published!", "success");
            await loadNews();
        } catch {
            showToast("Failed to publish.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Publish Article";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadNews);
    loadNews();
});

async function loadNews() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';
    try {
        const data = await apiRequest("/news");
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NW</span><p>No articles yet. Publish the first one!</p></div>';
            return;
        }

        const newsWithReviews = await Promise.all(data.map(async item => ({
            ...item,
            reviews: await loadReviews(REVIEW_TARGETS.news, item.id)
        })));

        container.className = "news-list";
        container.innerHTML = newsWithReviews.map((n, i) => `
      <div class="news-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="news-main">
          <h3>${n.title}</h3>
          <div class="news-body">${n.content || ""}</div>
          <div class="news-timestamp">Published ${newsTimestamp(n)}</div>
          <div class="card-actions">
            ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editNews(${n.id}, ${JSON.stringify(n)})'>Edit</button>` : ""}
            ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteNews(${n.id}, '${esc(n.title)}')">Delete</button>` : ""}
          </div>
          ${renderReviewSection(REVIEW_TARGETS.news, n.id, n.reviews || [])}
        </div>
        <div class="news-label">
          <span class="news-tag">City News</span>
        </div>
      </div>`).join("");

        hydrateReviewForms(loadNews);
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NA</span><p>Cannot connect to server.</p></div>';
    }
}

function editNews(id, n) {
    openEditModal({
        title: "Edit Article",
        fields: [
            { key: "title", label: "Headline", placeholder: "Article headline" },
            { key: "content", label: "Content", placeholder: "Story content" }
        ],
        values: { title: n.title, content: n.content },
        onSave: async (data) => { await apiRequest(`/news/${id}`, "PUT", data); await loadNews(); }
    });
}

function deleteNews(id, title) {
    openDeleteModal({
        itemName: title,
        onConfirm: async () => { await apiRequest(`/news/${id}`, "DELETE"); await loadNews(); }
    });
}
