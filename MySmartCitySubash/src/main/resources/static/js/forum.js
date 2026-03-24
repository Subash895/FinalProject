/* ============================================================
   SMART CITY — forum.js
   Community forum logic
   ============================================================ */

document.addEventListener("DOMContentLoaded", function () {

  const form = document.getElementById("forumForm");
  if (!form) return;

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    const btn = form.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Posting...';

    try {
      await apiRequest("/forumposts", "POST", {
        title:   document.getElementById("title").value,
        content: document.getElementById("content").value
      });
      form.reset();
      await loadPosts();
    } catch (err) {
      console.error("Post failed:", err);
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Post to Forum";
    }
  });

  document.getElementById("loadBtn")?.addEventListener("click", loadPosts);
});

async function loadPosts() {
  const container = document.getElementById("list");
  container.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

  try {
    const data = await apiRequest("/forumposts");

    // Update post count
    const countEl = document.getElementById("postCount");
    if (countEl) countEl.textContent = data?.length || 0;

    if (!data || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state glass-card">
          <div class="empty-icon">💬</div>
          <p>No posts yet. Start the conversation!</p>
        </div>`;
      return;
    }

    container.className = "forum-list";
    container.innerHTML = data.map((p, i) => `
      <div class="forum-post-card glass-card" style="animation-delay:${i * 0.05}s">
        <div class="post-header">
          <h3>${p.title}</h3>
          <span class="badge badge-purple">Discussion</span>
        </div>
        <div class="post-content">${p.content}</div>
        <div class="post-footer">
          <span>💬 Community</span>
          <span class="sep"></span>
          <span>📅 Recently posted</span>
        </div>
      </div>
    `).join("");

  } catch (err) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <div class="empty-icon">⚠️</div>
        <p>Failed to load posts. Is the server running?</p>
      </div>`;
  }
}