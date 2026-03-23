document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("placeForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("name").value;
        const category = document.getElementById("category").value;
        const location = document.getElementById("location").value;

        await apiRequest("/places", "POST", { name, category, location });

        loadPlaces();
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadPlaces);
});

async function loadPlaces() {
    const data = await apiRequest("/places");

    document.getElementById("list").innerHTML =
        data.map(p => `<div class="card">${p.name} - ${p.category}</div>`).join("");
}