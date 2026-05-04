/**
 * Client-side behavior for the news page, including event handling and API calls.
 */
/* ============================================================
   SMART CITY - news.js
   ============================================================ */

function esc(s) {
    return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

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
    setListSkeleton(container, "news", 4);
    try {
        const data = await apiRequest("/news");
        if (!data || data.length === 0) {
            clearListSkeleton(container);
            container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NW</span><p>No articles yet. Publish the first one!</p></div>';
            return;
        }

        const newsWithReviews = await attachReviewsToItems(data, REVIEW_TARGETS.news);

        container.className = "news-list";
        clearListSkeleton(container);
        container.innerHTML = newsWithReviews.map((n, i) => `
      <div class="news-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="news-main">
          ${n.imageUrl ? `<img class="news-image" src="${n.imageUrl}" alt="${n.title || "News"}">` : ""}
          <h3>${n.title}</h3>
          <div class="news-body">${n.content || ""}</div>
          <div class="news-timestamp">Published ${newsTimestamp(n)}</div>
          <div class="card-actions">
            ${isAdmin() ? `<button class="btn btn-secondary btn-sm" title="Upload" onclick="triggerNewsImagePicker(${n.id})">Upload</button><input id="newsImageInput_${n.id}" class="news-image-input" type="file" accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.svg,.ico,.tif,.tiff,image/*" onchange="handleNewsImageSelected(${n.id}, event)">` : ""}
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
        clearListSkeleton(container);
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NA</span><p>Cannot connect to server.</p></div>';
    }
}

function editNews(id, n) {
    openEditModal({
        title: "Edit Article",
        fields: [{
                key: "title",
                label: "Headline",
                placeholder: "Article headline"
            },
            {
                key: "content",
                label: "Content",
                placeholder: "Story content"
            }
        ],
        values: {
            title: n.title,
            content: n.content
        },
        onSave: async (data) => {
            await apiRequest(`/news/${id}`, "PUT", data);
            await loadNews();
        }
    });
}

function deleteNews(id, title) {
    openDeleteModal({
        itemName: title,
        onConfirm: async () => {
            await apiRequest(`/news/${id}`, "DELETE");
            await loadNews();
        }
    });
}

function triggerNewsImagePicker(id) {
    document.getElementById(`newsImageInput_${id}`)?.click();
}

async function handleNewsImageSelected(id, event) {
    const file = event?.target?.files?.[0];
    if (!file) {
        return;
    }
    if (!file.type || !file.type.startsWith("image/")) {
        showToast("Only image files are allowed.", "error");
        event.target.value = "";
        return;
    }
    try {
        const formData = new FormData();
        formData.append("photo", file);
        const token = localStorage.getItem("token");
        const response = await fetch(`${API_BASE}/news/${Number(id)}/photo`, {
            method: "PUT",
            headers: token ? { Authorization: `Bearer ${token}` } : {},
            body: formData
        });
        if (!response.ok) {
            let message = "Failed to upload news image.";
            try {
                message = await response.text() || message;
            } catch {}
            throw new Error(message);
        }
        showToast("News image updated.", "success");
        await loadNews();
    } catch (error) {
        showToast(error.message || "Failed to upload news image.", "error");
    } finally {
        event.target.value = "";
    }
}
