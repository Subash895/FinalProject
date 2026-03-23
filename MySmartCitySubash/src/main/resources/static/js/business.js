document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("businessForm");

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const name = document.getElementById("name").value;
            const category = document.getElementById("category").value;
            const address = document.getElementById("address").value;

            await apiRequest("/businesses", "POST", {
                name,
                category,
                address
            });

            loadBusinesses();
        });
    }

    document.getElementById("loadBtn")?.addEventListener("click", loadBusinesses);
});


async function loadBusinesses() {
    const data = await apiRequest("/businesses");

    const container = document.getElementById("list");

    container.innerHTML = data.map(b => `
        <div class="card">
            <h3>${b.name}</h3>
            <p>${b.category}</p>
            <p>${b.address}</p>
        </div>
    `).join("");
}