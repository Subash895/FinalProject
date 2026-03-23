// LOGIN
async function loginUser(email, password) {

    const res = await apiRequest("/auth/login", "POST", {
        email,
        password
    });

    if (res && res.id) {
        localStorage.setItem("user", JSON.stringify(res));
        window.location.href = "business.html";
    } else {
        alert("Login failed");
    }
}


// REGISTER
async function registerUser(name, email, password) {

    const res = await apiRequest("/auth/register", "POST", {
        name,
        email,
        password,
        role: "USER"
    });

    if (res && res.id) {
        alert("Registered!");
        window.location.href = "login.html";
    } else {
        alert("Register failed");
    }
}