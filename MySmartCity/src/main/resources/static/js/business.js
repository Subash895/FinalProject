// HANDLE FORM SUBMIT
document.getElementById("businessForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const errorDiv = document.getElementById("error");
    errorDiv.innerText = "";

    const data = {
        name: document.getElementById("name").value,
        category: document.getElementById("category").value,
        address: document.getElementById("address").value,
        userId: parseInt(document.getElementById("userId").value)
    };

    try {
        const res = await fetch("http://localhost:8081/api/businesses", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        const result = await res.json();

        if (!res.ok) {
            throw new Error(result.message || "Failed to add business");
        }

        alert(result.message);
        document.getElementById("businessForm").reset();
        loadBusinesses();

    } catch (err) {
        console.error(err);
        errorDiv.innerText = err.message;
    }
});


async function loadBusinesses() {
    try {
        const name = document.getElementById("searchName")?.value || "";
        const category = document.getElementById("searchCategory")?.value || "";

        const url = `http://localhost:8081/api/businesses?name=${encodeURIComponent(name)}&category=${encodeURIComponent(category)}`;

        const res = await fetch(url);
        const result = await res.json();

        console.log("RESULT:", result); // debug

        if (!res.ok) {
            throw new Error(result.message);
        }

        // 🔥 FIX IS HERE
        const data = result.data.items;

        const container = document.getElementById("list");
        container.innerHTML = "";

        if (!data || data.length === 0) {
            container.innerHTML = "No businesses found";
            return;
        }

        data.forEach(b => {
            const div = document.createElement("div");
            div.innerHTML = `<strong>${b.name}</strong> (${b.category})`;
            container.appendChild(div);
        });

    } catch (err) {
        console.error(err);
        document.getElementById("error").innerText = err.message;
    }
}
loadBusinesses();