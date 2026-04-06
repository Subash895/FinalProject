/* ============================================================
   SMART CITY — modal.js  (Shared modal & toast — load first)
   ============================================================ */

/* ── Toast ──────────────────────────────────────────────────── */
function showToast(message, type = "success") {
    let container = document.getElementById("toastContainer");
    if (!container) {
        container = document.createElement("div");
        container.id = "toastContainer";
        container.className = "toast-container";
        document.body.appendChild(container);
    }
    const icons = { success: "✅", error: "❌", info: "ℹ️" };
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icons[type] || "ℹ️"}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.animation = "toastOut 0.3s ease forwards";
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/* ── Edit Modal ─────────────────────────────────────────────── */
function openEditModal({ title, fields, values, onSave }) {
    closeAnyModal();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "editModal";

    overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title">✏️ ${title}</div>
        <button class="modal-close" onclick="closeAnyModal()">✕</button>
      </div>
      <div class="modal-body">
        ${fields.map(f => `
          <div class="form-group">
            <label>${f.label}</label>
            <input class="form-control" id="edit_${f.key}"
              type="${f.type || 'text'}"
              placeholder="${f.placeholder || ''}"
              value="${String(values[f.key] || '').replace(/"/g, '&quot;')}">
          </div>`).join("")}
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeAnyModal()">Cancel</button>
        <button class="btn btn-primary" id="editSaveBtn">Save Changes</button>
      </div>
    </div>`;

    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("open"));
    overlay.addEventListener("click", e => { if (e.target === overlay) closeAnyModal(); });

    document.getElementById("editSaveBtn").addEventListener("click", async () => {
        const payload = {};
        fields.forEach(f => { payload[f.key] = document.getElementById(`edit_${f.key}`).value.trim(); });

        const btn = document.getElementById("editSaveBtn");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Saving...';
        try {
            await onSave(payload);
            closeAnyModal();
            showToast("Updated successfully!", "success");
        } catch {
            showToast("Update failed. Try again.", "error");
            btn.disabled = false;
            btn.innerHTML = "Save Changes";
        }
    });
}

/* ── Delete Confirm Modal ───────────────────────────────────── */
function openDeleteModal({ itemName, onConfirm }) {
    closeAnyModal();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "deleteModal";

    overlay.innerHTML = `
    <div class="modal confirm-modal">
      <span class="confirm-icon">🗑️</span>
      <h3>Delete this item?</h3>
      <p>You're about to permanently delete <strong style="color:var(--text-primary)">"${itemName}"</strong>.</p>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeAnyModal()">Cancel</button>
        <button class="btn btn-danger" id="confirmDeleteBtn">Yes, Delete</button>
      </div>
    </div>`;

    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("open"));
    overlay.addEventListener("click", e => { if (e.target === overlay) closeAnyModal(); });

    document.getElementById("confirmDeleteBtn").addEventListener("click", async () => {
        const btn = document.getElementById("confirmDeleteBtn");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Deleting...';
        try {
            await onConfirm();
            closeAnyModal();
            showToast("Deleted successfully.", "info");
        } catch {
            showToast("Delete failed. Try again.", "error");
            btn.disabled = false;
            btn.innerHTML = "Yes, Delete";
        }
    });
}

/* ── Close ──────────────────────────────────────────────────── */
function closeAnyModal() {
    ["editModal", "deleteModal"].forEach(id => {
        const el = document.getElementById(id);
        if (el) { el.classList.remove("open"); setTimeout(() => el.remove(), 250); }
    });
}

document.addEventListener("keydown", e => { if (e.key === "Escape") closeAnyModal(); });

const THEME_STORAGE_KEY = "smartcity.theme";

function getThemePreference() {
    return localStorage.getItem(THEME_STORAGE_KEY) || "dark";
}

function updateThemeToggleIcon() {
    document.querySelectorAll(".nav-theme-toggle .theme-icon").forEach(icon => {
        icon.textContent = getThemePreference() === "light" ? "☀" : "☾";
    });
}

function applyTheme(theme) {
    const nextTheme = theme === "light" ? "light" : "dark";
    document.body.classList.toggle("theme-light", nextTheme === "light");
    document.documentElement.setAttribute("data-theme", nextTheme);
    localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
    updateThemeToggleIcon();
}

function toggleTheme() {
    applyTheme(getThemePreference() === "light" ? "dark" : "light");
}

function initializeTheme() {
    if (!document.body) {
        return;
    }
    applyTheme(getThemePreference());
}

document.addEventListener("DOMContentLoaded", initializeTheme);
