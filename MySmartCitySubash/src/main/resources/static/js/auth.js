/* ============================================================
   SMART CITY — auth.js  (Login & Register)
   ============================================================ */

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

async function registerUser(name, email, password) {
    const res = await apiRequest("/auth/register", "POST", { name, email, password, role: "USER" });
    if (res && (res.id || res.token)) {
        window.location.href = "login.html";
    } else {
        throw new Error("Registration failed");
    }
}