/**
 * Client-side behavior for the profile page, including event handling and API calls.
 */
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
    const avatarEl = document.getElementById("profileAvatar");
    if (user.profilePhoto) {
        avatarEl.innerHTML = `<img src="${user.profilePhoto}" alt="${user.name || "User"}">`;
    } else {
        avatarEl.textContent = getProfileInitials(user);
    }
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

function setProfilePasswordVisibility(input, button, visible) {
    input.type = visible ? "text" : "password";
    button.textContent = visible ? "Hide" : "Show";
    button.setAttribute("aria-label", visible ? "Hide password" : "Show password");
    button.setAttribute("aria-pressed", visible ? "true" : "false");
}

function initializeProfilePasswordToggles() {
    const toggleButtons = Array.from(document.querySelectorAll("[data-password-toggle]"));

    toggleButtons.forEach((button) => {
        const input = document.getElementById(button.dataset.target);
        if (!input) {
            return;
        }

        let pinnedVisible = false;

        button.addEventListener("pointerdown", () => {
            if (!pinnedVisible) {
                setProfilePasswordVisibility(input, button, true);
            }
        });

        ["pointerup", "pointerleave", "pointercancel"].forEach((eventName) => {
            button.addEventListener(eventName, () => {
                if (!pinnedVisible) {
                    setProfilePasswordVisibility(input, button, false);
                }
            });
        });

        button.addEventListener("click", () => {
            pinnedVisible = !pinnedVisible;
            setProfilePasswordVisibility(input, button, pinnedVisible);
        });

        input.addEventListener("blur", () => {
            if (!pinnedVisible) {
                setProfilePasswordVisibility(input, button, false);
            }
        });
    });
}

async function loadProfile() {
    document.body.classList.add("profile-loading");
    const user = await apiRequest("/users/me");
    updateStoredUser(user);
    applyRoleUI();
    fillProfileSummary(user);
    fillProfileForm(user);
    document.body.classList.remove("profile-loading");
}

async function uploadProfilePhoto(file) {
    if (!file) {
        return;
    }
    if (!file.type || !file.type.startsWith("image/")) {
        throw new Error("Please select an image file.");
    }

    const formData = new FormData();
    formData.append("photo", file);

    const token = localStorage.getItem("token");
    const response = await fetch(`${API_BASE}/users/me/photo`, {
        method: "PUT",
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: formData
    });

    if (!response.ok) {
        let message = "Failed to upload profile photo.";
        try {
            message = await response.text() || message;
        } catch {}
        throw new Error(message);
    }

    const user = await response.json();
    updateStoredUser(user);
    applyRoleUI();
    fillProfileSummary(user);
    setProfileMessage("success", "Profile photo updated.");
    if (typeof showToast === "function") {
        showToast("Profile photo updated.", "success");
    }
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
    initializeProfilePasswordToggles();
    document.getElementById("profileForm").addEventListener("submit", saveProfile);
    document.getElementById("profilePhotoInput").addEventListener("change", async event => {
        setProfileMessage();
        const file = event.target.files?.[0];
        if (!file) {
            return;
        }
        try {
            await uploadProfilePhoto(file);
        } catch (error) {
            setProfileMessage("error", error.message || "Unable to upload profile photo.");
        } finally {
            event.target.value = "";
        }
    });

    try {
        await loadProfile();
    } catch (error) {
        document.body.classList.remove("profile-loading");
        setProfileMessage("error", error.message || "Unable to load profile.");
    }
});
