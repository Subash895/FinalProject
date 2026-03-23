document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("subscriptionForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const type = document.getElementById("type").value;

        await apiRequest("/subscriptions", "POST", { email, type });

        loadSubs();
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadSubs);
});

async function loadSubs() {
    const data = await apiRequest("/subscriptions");

    document.getElementById("list").innerHTML =
        data.map(s => `<div class="card">${s.email}</div>`).join("");
}