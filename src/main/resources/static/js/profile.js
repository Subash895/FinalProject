/* ============================================================
   SMART CITY - profile.js
   ============================================================ */

function setProfileMessage(type, message) {
    const errorEl = document.getElementById("profileError");
    const successEl = document.getElementById("profileSuccess");

    errorEl.classList.remove("show");
    successEl.classList.remove("show");

    if (!message) {
        return;
    }

    const target = type === "success" ? successEl : errorEl;
    target.textContent = message;
    target.classList.add("show");
}

function getProfileInitials(user) {
    const rawName = (user.name || "").trim();
    if (!rawName) {
        return (user.email || "U").charAt(0).toUpperCase();
    }

    const parts = rawName.split(/\s+/).filter(Boolean);
    if (parts.length === 1) {
        const word = parts[0];
        const first = word.charAt(0);
        const last = word.length > 1 ? word.charAt(word.length - 1) : "";
        return (first + last).toUpperCase();
    }

    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

function fillProfileSummary(user) {
    document.getElementById("profileAvatar").textContent = getProfileInitials(user);
    document.getElementById("profileRoleBadge").textContent = user.role || "USER";
    document.getElementById("profileName").textContent = user.name || "Unnamed User";
    document.getElementById("profileEmail").textContent = user.email || "-";
    document.getElementById("profileId").textContent = user.id || "-";
    document.getElementById("profileRole").textContent = user.role || "-";
}

function fillProfileForm(user) {
    document.getElementById("profileNameInput").value = user.name || "";
    document.getElementById("profileEmailInput").value = user.email || "";
    document.getElementById("profileRoleInput").value = user.role || "USER";
    document.getElementById("profilePasswordInput").value = "";
}

async function loadProfile() {
    const user = await apiRequest("/users/me");
    updateStoredUser(user);
    applyRoleUI();
    fillProfileSummary(user);
    fillProfileForm(user);
}

async function saveProfile(event) {
    event.preventDefault();
    setProfileMessage();

    const saveBtn = document.getElementById("profileSaveBtn");
    const payload = {
        name: document.getElementById("profileNameInput").value.trim(),
        email: document.getElementById("profileEmailInput").value.trim(),
        role: getUser()?.role || "USER",
        password: document.getElementById("profilePasswordInput").value.trim()
    };

    saveBtn.disabled = true;
    saveBtn.innerHTML = '<span class="spinner"></span> Saving...';

    try {
        const updatedSession = await apiRequest("/users/me", "PUT", payload);
        storeSession(updatedSession);
        applyRoleUI();
        fillProfileSummary(updatedSession);
        fillProfileForm(updatedSession);
        setProfileMessage("success", "Profile updated successfully.");
        if (typeof showToast === "function") {
            showToast("Profile updated successfully.", "success");
        }
    } catch (error) {
        setProfileMessage("error", error.message || "Failed to update profile.");
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = "Save Profile";
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    if (!isLoggedIn()) {
        window.location.href = "login.html";
        return;
    }

    applyRoleUI();
    document.getElementById("profileForm").addEventListener("submit", saveProfile);

    try {
        await loadProfile();
    } catch (error) {
        setProfileMessage("error", error.message || "Unable to load profile.");
    }
});
