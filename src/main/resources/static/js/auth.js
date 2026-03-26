/* ============================================================
   SMART CITY — auth.js  (Login & Register)
   ============================================================ */

function getUser()    { try { return JSON.parse(localStorage.getItem("user")); } catch { return null; } }
function getRole()    { return getUser()?.role || null; }
function isAdmin()    { return getRole() === "ADMIN"; }
function isUser()     { return ["USER","ADMIN","BUSINESS"].includes(getRole()); }
function isBusiness() { return ["BUSINESS","ADMIN"].includes(getRole()); }
function getUserId()  { return getUser()?.id || null; }
function isLoggedIn() { return !!getUser(); }

function logout() {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    window.location.href = "login.html";
}

function applyRoleUI() {
    const user = getUser();
    const navLogin  = document.getElementById("navLogin");
    const navLogout = document.getElementById("navLogout");
    const navUser   = document.getElementById("navUser");
    if (navLogin)  navLogin.style.display  = isLoggedIn() ? "none" : "";
    if (navLogout) navLogout.style.display = isLoggedIn() ? "" : "none";
    if (navUser && user) {
        navUser.style.display = "";
        navUser.querySelector("a").textContent = "👤 " + user.name + " (" + getRole() + ")";
    }
}

async function loginUser(email, password) {
    const res = await apiRequest("/auth/login", "POST", { email, password });
    if (res && (res.token || res.id)) {
        if (res.token) localStorage.setItem("token", res.token);
        localStorage.setItem("user", JSON.stringify(res));
        window.location.href = "index.html";
    } else {
        throw new Error("Login failed");
    }
}

async function registerUser(name, email, password, role) {
    const safeRole = (role === "BUSINESS") ? "BUSINESS" : "USER";
    const res = await apiRequest("/auth/register", "POST", { name, email, password, role: safeRole });
    if (res && (res.id || res.token)) {
        window.location.href = "login.html";
    } else {
        throw new Error("Registration failed");
    }
}
