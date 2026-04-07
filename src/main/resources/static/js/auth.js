/* ============================================================
   SMART CITY - auth.js  (Login, Register, Session UI)
   ============================================================ */

function getUser() { try { return JSON.parse(localStorage.getItem("user")); } catch { return null; } }
function getToken() { return localStorage.getItem("token"); }
function getRole() { return getUser()?.role || null; }
function isAdmin() { return getRole() === "ADMIN"; }
function isUser() { return ["USER", "ADMIN", "BUSINESS"].includes(getRole()); }
function isBusiness() { return ["BUSINESS", "ADMIN"].includes(getRole()); }
function isNormalUser() { return getRole() === "USER"; }
function getUserId() { return getUser()?.id || null; }
function isLoggedIn() { return !!getUser() && !!getToken(); }

let googleAuthConfigPromise = null;
let googleIdentityScriptPromise = null;

function getGoogleClientIdFallback() {
    return window.SMARTCITY_GOOGLE_CLIENT_ID || "";
}

function getUserInitials(user) {
    const rawName = (user?.name || "").trim();
    if (!rawName) {
        return (user?.email || "U").charAt(0).toUpperCase();
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

function storeSession(authResponse) {
    if (authResponse?.token) {
        localStorage.setItem("token", authResponse.token);
    }

    localStorage.setItem("user", JSON.stringify({
        id: authResponse.id,
        name: authResponse.name,
        email: authResponse.email,
        role: authResponse.role
    }));
}

function updateStoredUser(userResponse) {
    if (!userResponse) {
        return;
    }

    const existing = getUser() || {};
    localStorage.setItem("user", JSON.stringify({
        ...existing,
        id: userResponse.id ?? existing.id,
        name: userResponse.name ?? existing.name,
        email: userResponse.email ?? existing.email,
        role: userResponse.role ?? existing.role
    }));
}

function logout() {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    window.location.href = "login.html";
}

function clearStaleSession() {
    if (getUser() && !getToken()) {
        localStorage.removeItem("user");
    }
}

function applyRoleUI() {
    const user = getUser();
    const navLogin = document.getElementById("navLogin");
    const navLogout = document.getElementById("navLogout");
    const navUser = document.getElementById("navUser");

    if (navLogin) navLogin.style.display = isLoggedIn() ? "none" : "";
    if (navLogout) navLogout.style.display = isLoggedIn() ? "" : "none";

    if (navUser && user) {
        const profileLink = navUser.querySelector("a");
        navUser.style.display = "";
        profileLink.classList.add("nav-profile-link");
        profileLink.href = "profile.html";
        profileLink.style.pointerEvents = "";
        profileLink.innerHTML = `<span class="nav-profile-avatar">${getUserInitials(user)}</span><span>Profile: ${user.name} (${getRole()})</span>`;
    }
}

async function loginUser(email, password) {
    const res = await apiRequest("/auth/login", "POST", { email, password });
    if (res && (res.token || res.id)) {
        storeSession(res);
        window.location.href = "index.html";
        return;
    }

    throw new Error("Login failed");
}

async function registerUser(name, email, password, role) {
    const safeRole = role === "BUSINESS" ? "BUSINESS" : "USER";
    const res = await apiRequest("/auth/register", "POST", { name, email, password, role: safeRole });
    if (res && (res.id || res.token)) {
        window.location.href = "login.html";
        return;
    }

    throw new Error("Registration failed");
}

async function requestPasswordReset(email) {
    return apiRequest("/auth/forgot-password", "POST", { email });
}

async function resetPasswordWithOtp(email, otp, newPassword) {
    return apiRequest("/auth/reset-password", "POST", { email, otp, newPassword });
}

async function loginWithGoogleCredential(credential, role = "USER") {
    const safeRole = role === "BUSINESS" ? "BUSINESS" : "USER";
    const res = await apiRequest("/auth/google", "POST", { credential, role: safeRole });
    if (res && (res.token || res.id)) {
        storeSession(res);
        window.location.href = "index.html";
        return;
    }

    throw new Error("Google authentication failed");
}

async function getGoogleAuthConfig() {
    if (!googleAuthConfigPromise) {
        googleAuthConfigPromise = apiRequest("/auth/google/config")
            .catch(() => {
                const fallbackClientId = getGoogleClientIdFallback();
                return fallbackClientId ? { enabled: true, clientId: fallbackClientId } : { enabled: false, clientId: null };
            });
    }

    return googleAuthConfigPromise;
}

async function loadGoogleIdentityScript() {
    if (window.google?.accounts?.id) {
        return window.google;
    }

    if (!googleIdentityScriptPromise) {
        googleIdentityScriptPromise = new Promise((resolve, reject) => {
            const existingScript = document.querySelector('script[data-google-identity="true"]');
            if (existingScript) {
                existingScript.addEventListener("load", () => resolve(window.google), { once: true });
                existingScript.addEventListener("error", () => reject(new Error("Failed to load Google sign-in.")), { once: true });
                return;
            }

            const script = document.createElement("script");
            script.src = "https://accounts.google.com/gsi/client";
            script.async = true;
            script.defer = true;
            script.dataset.googleIdentity = "true";
            script.onload = () => resolve(window.google);
            script.onerror = () => reject(new Error("Failed to load Google sign-in."));
            document.head.appendChild(script);
        });
    }

    return googleIdentityScriptPromise;
}

async function setupGoogleAuthButton(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return false;
    }

    const config = await getGoogleAuthConfig();
    if (!config?.enabled || !config?.clientId) {
        const wrapper = container.closest(".google-auth-section");
        if (wrapper) {
            wrapper.style.display = "none";
        } else {
            container.style.display = "none";
        }
        return false;
    }

    await loadGoogleIdentityScript();

    const role = typeof options.getRole === "function" ? options.getRole : () => "USER";
    const onError = typeof options.onError === "function" ? options.onError : () => {};
    const mode = options.mode === "signup" ? "signup" : "signin";

    window.google.accounts.id.initialize({
        client_id: config.clientId,
        callback: async (response) => {
            try {
                await loginWithGoogleCredential(response.credential, role());
            } catch (error) {
                onError(error);
            }
        }
    });

    container.innerHTML = "";
    window.google.accounts.id.renderButton(container, {
        theme: "outline",
        size: "large",
        shape: "pill",
        text: options.buttonText || "continue_with",
        width: options.width || 360
    });

    const helperText = document.createElement("p");
    helperText.className = "google-auth-helper";
    helperText.textContent = mode === "signup"
        ? "Your account will be created automatically from your Google profile."
        : "Use your Google account to sign in instantly.";
    container.appendChild(helperText);

    return true;
}

clearStaleSession();
