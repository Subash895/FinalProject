/* ============================================================
   SMART CITY — auth.js
   Login & Register logic
   ============================================================ */

async function loginUser(email, password) {
  const res = await apiRequest("/auth/login", "POST", { email, password });

  if (res && res.id) {
    localStorage.setItem("user", JSON.stringify(res));
    if (res.token) localStorage.setItem("token", res.token);
    window.location.href = "business.html";
  } else {
    throw new Error("Login failed");
  }
}

async function registerUser(name, email, password) {
  const res = await apiRequest("/auth/register", "POST", {
    name,
    email,
    password,
    role: "USER"
  });

  if (res && res.id) {
    window.location.href = "login.html";
  } else {
    throw new Error("Registration failed");
  }
}