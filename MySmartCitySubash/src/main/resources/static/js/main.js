// ================= BUSINESS =================

document.addEventListener("DOMContentLoaded", () => {

    const businessForm = document.getElementById("businessForm");

    if (businessForm) {
        businessForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const name = document.getElementById("businessName").value;
            const category = document.getElementById("businessCategory").value;
            const address = document.getElementById("businessAddress").value;

            if (!name || !category || !address) {
                alert("Fill all fields");
                return;
            }

            await apiService.post("/businesses", { name, category, address });
            alert("Business Added");
            loadBusinesses();
        });
    }

    const loadBtn = document.getElementById("loadBusinessesBtn");

    if (loadBtn) {
        loadBtn.addEventListener("click", loadBusinesses);
    }

});


async function loadBusinesses() {
    const data = await apiService.get("/businesses");

    const container = document.getElementById("businessesContainer");

    if (!data || data.length === 0) {
        container.innerHTML = "<p>No Data</p>";
        return;
    }

    container.innerHTML = data.map(b => `
        <div style="border:1px solid #ccc; padding:10px; margin:10px;">
            <h3>${b.name}</h3>
            <p>${b.category}</p>
            <p>${b.address}</p>
        </div>
    `).join("");
}