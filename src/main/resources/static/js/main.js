/**
 * Shared frontend bootstrapping logic used across multiple static pages.
 */

function showToast(message, type = "success") {
    let container = document.getElementById("toastContainer");
    if (!container) {
        container = document.createElement("div");
        container.id = "toastContainer";
        container.className = "toast-container";
        document.body.appendChild(container);
    }

    const icons = {
        success: "OK",
        error: "ERR",
        info: "INFO"
    };

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icons[type] || "INFO"}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = "toastOut 0.3s ease forwards";
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function openEditModal({
    title,
    fields,
    values,
    onSave
}) {
    closeAnyModal();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "editModal";

    overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title">${title}</div>
        <button class="modal-close" onclick="closeAnyModal()">X</button>
      </div>
      <div class="modal-body">
        ${fields.map(f => `
          <div class="form-group">
            <label>${f.label}</label>
            <input class="form-control" id="edit_${f.key}"
              type="${f.type || "text"}"
              placeholder="${f.placeholder || ""}"
              value="${String(values[f.key] || "").replace(/"/g, "&quot;")}">
          </div>`).join("")}
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeAnyModal()">Cancel</button>
        <button class="btn btn-primary" id="editSaveBtn">Save Changes</button>
      </div>
    </div>`;

    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("open"));
    overlay.addEventListener("click", e => {
        if (e.target === overlay) {
            closeAnyModal();
        }
    });

    document.getElementById("editSaveBtn").addEventListener("click", async () => {
        const payload = {};
        fields.forEach(f => {
            payload[f.key] = document.getElementById(`edit_${f.key}`).value.trim();
        });

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

function openDeleteModal({
    itemName,
    onConfirm
}) {
    closeAnyModal();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "deleteModal";

    overlay.innerHTML = `
    <div class="modal confirm-modal">
      <span class="confirm-icon">DEL</span>
      <h3>Delete this item?</h3>
      <p>You're about to permanently delete <strong style="color:var(--text-primary)">"${itemName}"</strong>.</p>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="closeAnyModal()">Cancel</button>
        <button class="btn btn-danger" id="confirmDeleteBtn">Yes, Delete</button>
      </div>
    </div>`;

    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("open"));
    overlay.addEventListener("click", e => {
        if (e.target === overlay) {
            closeAnyModal();
        }
    });

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

function closeAnyModal() {
    ["editModal", "deleteModal", "reviewComposeModal"].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.remove("open");
            setTimeout(() => el.remove(), 250);
        }
    });
}

document.addEventListener("keydown", e => {
    if (e.key === "Escape") {
        closeAnyModal();
    }
});

const THEME_STORAGE_KEY = "smartcity.theme";

function getThemePreference() {
    return localStorage.getItem(THEME_STORAGE_KEY) || "dark";
}

function updateThemeToggleIcon() {
    document.querySelectorAll(".nav-theme-toggle .theme-icon").forEach(icon => {
        icon.textContent = getThemePreference() === "light" ? "\u2600" : "\u263E";
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

const chatState = {
    initialized: false,
    historyLoaded: false,
    sending: false
};

function initializeChatbot() {
    if (chatState.initialized || typeof isLoggedIn !== "function" || typeof apiRequest !== "function") {
        return;
    }

    chatState.initialized = true;

    if (!isLoggedIn()) {
        return;
    }

    const user = typeof getUser === "function" ? getUser() : null;
    if (!user?.id) {
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "chatbot-shell";
    wrapper.innerHTML = `
      <button type="button" class="chatbot-launcher" id="chatbotLauncher" aria-label="Open chatbot">
        <span class="chatbot-launcher-icon">AI</span>
        <span>Assistant</span>
      </button>
      <section class="chatbot-panel" id="chatbotPanel" aria-hidden="true">
        <div class="chatbot-header">
          <div>
            <strong>SmartCity Assistant</strong>
            <p>${escapeChatHtml(user.name || "User")}</p>
          </div>
          <button type="button" class="chatbot-close" id="chatbotClose" aria-label="Close chatbot">X</button>
        </div>
        <div class="chatbot-body" id="chatbotMessages">
          <div class="chatbot-empty" id="chatbotEmpty">
            Ask about cities, places, businesses, news, or forum posts from this project.
          </div>
        </div>
        <form class="chatbot-form" id="chatbotForm">
          <textarea id="chatbotInput" class="chatbot-input" placeholder="Ask about your project data..." rows="3" maxlength="1000"></textarea>
          <div class="chatbot-actions">
            <button type="button" class="btn btn-secondary" id="chatbotClear">Clear History</button>
            <button type="submit" class="btn btn-primary" id="chatbotSend">Send</button>
          </div>
        </form>
      </section>
    `;

    document.body.appendChild(wrapper);

    const launcher = document.getElementById("chatbotLauncher");
    const panel = document.getElementById("chatbotPanel");
    const closeButton = document.getElementById("chatbotClose");
    const clearButton = document.getElementById("chatbotClear");
    const form = document.getElementById("chatbotForm");
    const input = document.getElementById("chatbotInput");

    launcher.addEventListener("click", async () => {
        launcher.classList.add("is-hidden");
        panel.classList.add("open");
        panel.setAttribute("aria-hidden", "false");
        if (!chatState.historyLoaded) {
            await loadChatHistory();
        }
        input.focus();
    });

    closeButton.addEventListener("click", closeChatbot);

    form.addEventListener("submit", async event => {
        event.preventDefault();
        await sendChatMessage();
    });

    clearButton.addEventListener("click", clearChatHistory);

    input.addEventListener("keydown", async event => {
        if (event.key === "Escape") {
            closeChatbot();
            return;
        }
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            await sendChatMessage();
        }
    });

    document.addEventListener("click", event => {
        if (!panel.classList.contains("open")) {
            return;
        }

        const clickedInsidePanel = panel.contains(event.target);
        const clickedLauncher = launcher.contains(event.target);
        if (!clickedInsidePanel && !clickedLauncher) {
            closeChatbot();
        }
    });
}

function closeChatbot() {
    const launcher = document.getElementById("chatbotLauncher");
    const panel = document.getElementById("chatbotPanel");
    if (!panel) {
        return;
    }
    if (launcher) {
        launcher.classList.remove("is-hidden");
    }
    panel.classList.remove("open");
    panel.setAttribute("aria-hidden", "true");
}

function renderChatMessages(messages) {
    const container = document.getElementById("chatbotMessages");
    if (!container) {
        return;
    }

    const safeMessages = Array.isArray(messages) ? messages : [];
    if (!safeMessages.length) {
        container.innerHTML = `<div class="chatbot-empty" id="chatbotEmpty">Ask about cities, places, businesses, news, or forum posts from this project.</div>`;
        return;
    }

    container.innerHTML = safeMessages.map(message => {
        const timestamp = message?.createdAt ? new Date(message.createdAt).toLocaleString() : "";
        const role = message?.role === "assistant" ? "assistant" : "user";
        return `
          <article class="chatbot-message chatbot-message-${role}">
            <div class="chatbot-bubble">
              <p>${escapeChatHtml(message?.content || "")}</p>
              <time>${escapeChatHtml(timestamp)}</time>
            </div>
          </article>
        `;
    }).join("");

    container.scrollTop = container.scrollHeight;
}

function escapeChatHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;")
        .replaceAll("\n", "<br>");
}

async function loadChatHistory() {
    if (!isLoggedIn()) {
        return;
    }

    try {
        const response = await apiRequest("/chat/history");
        chatState.historyLoaded = true;
        renderChatMessages(response?.messages || []);
    } catch (error) {
        showToast(error.message || "Failed to load chatbot history.", "error");
    }
}

async function sendChatMessage() {
    if (chatState.sending || !isLoggedIn()) {
        return;
    }

    const input = document.getElementById("chatbotInput");
    const sendButton = document.getElementById("chatbotSend");
    if (!input || !sendButton) {
        return;
    }

    const message = input.value.trim();
    if (!message) {
        return;
    }

    chatState.sending = true;
    sendButton.disabled = true;
    sendButton.innerHTML = '<span class="spinner"></span> Sending...';

    try {
        const response = await apiRequest("/chat/message", "POST", { message });
        input.value = "";
        chatState.historyLoaded = true;
        renderChatMessages(response?.messages || []);
    } catch (error) {
        showToast(error.message || "Chatbot request failed.", "error");
    } finally {
        chatState.sending = false;
        sendButton.disabled = false;
        sendButton.textContent = "Send";
    }
}

async function clearChatHistory() {
    if (!isLoggedIn()) {
        return;
    }

    const clearButton = document.getElementById("chatbotClear");
    if (!clearButton) {
        return;
    }

    clearButton.disabled = true;
    clearButton.textContent = "Clearing...";

    try {
        const response = await apiRequest("/chat/history", "DELETE");
        chatState.historyLoaded = true;
        renderChatMessages(response?.messages || []);
        showToast("Chat history cleared.", "info");
    } catch (error) {
        showToast(error.message || "Failed to clear chatbot history.", "error");
    } finally {
        clearButton.disabled = false;
        clearButton.textContent = "Clear History";
    }
}

document.addEventListener("DOMContentLoaded", initializeChatbot);
