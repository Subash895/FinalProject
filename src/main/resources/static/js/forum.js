/**
 * Client-side behavior for the forum page, including event handling and API calls.
 */

function esc(s) {
    return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("forumForm")?.addEventListener("submit", async e => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Posting...';

        try {
            await apiRequest("/forumposts", "POST", {
                title: document.getElementById("title").value.trim(),
                content: document.getElementById("content").value.trim()
            });
            e.target.reset();
            showToast("Post published!", "success");
            await loadPosts();
        } catch {
            showToast("Failed to post.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Post to Forum";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadPosts);
    loadPosts();
});

async function loadPosts() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const data = await apiRequest("/forumposts");
        const countEl = document.getElementById("postCount");
        if (countEl) {
            countEl.textContent = data?.length ?? 0;
        }

        if (!data || data.length === 0) {
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">FM</span><p>No posts yet. Start the conversation!</p></div>`;
            return;
        }

        container.className = "forum-list";
        container.innerHTML = data.map((p, i) => {
            const canEdit = isAdmin();
            const canDelete = isAdmin();
            return `
      <div class="forum-post-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="post-header">
          <h3>${p.title}</h3>
          <span class="badge badge-purple">Discussion</span>
        </div>
        <div class="post-content">${p.content}</div>
        <div class="post-footer">
          <span>Community</span>
          <span class="sep"></span>
          <span>Recently posted</span>
          <div class="post-btns">
            ${canEdit ? `<button class="btn btn-edit btn-sm" onclick='editPost(${p.id}, ${JSON.stringify(p)})'>Edit</button>` : ""}
            ${canDelete ? `<button class="btn btn-delete btn-sm" onclick="deletePost(${p.id}, '${esc(p.title)}')">Delete</button>` : ""}
          </div>
        </div>
      </div>`;
        }).join("");
    } catch {
        container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">ER</span><p>Cannot connect to server.</p></div>`;
    }
}

function editPost(id, p) {
    openEditModal({
        title: "Edit Post",
        fields: [{
                key: "title",
                label: "Title",
                placeholder: "Post title"
            },
            {
                key: "content",
                label: "Content",
                placeholder: "Post content"
            }
        ],
        values: {
            title: p.title,
            content: p.content
        },
        onSave: async data => {
            await apiRequest(`/forumposts/${id}`, "PUT", data);
            await loadPosts();
        }
    });
}

function deletePost(id, title) {
    openDeleteModal({
        itemName: title,
        onConfirm: async () => {
            await apiRequest(`/forumposts/${id}`, "DELETE");
            await loadPosts();
        }
    });
}
