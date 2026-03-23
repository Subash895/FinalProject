document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("cityForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("name").value;
        const country = document.getElementById("country").value;

        await apiRequest("/cities", "POST", { name, country });

        loadCities();
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadCities);
});

async function loadCities() {
    const data = await apiRequest("/cities");

    document.getElementById("list").innerHTML =
        data.map(c => `<div class="card">${c.name} - ${c.country}</div>`).join("");
}