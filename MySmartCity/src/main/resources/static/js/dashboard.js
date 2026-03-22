// LOAD BUSINESSES
async function loadBusinesses() {
    const container = document.getElementById("businessList");
    container.innerHTML = "Loading businesses...";

    try {
        const res = await fetch("http://localhost:8081/api/businesses");
        const result = await res.json();

        console.log("BUSINESS:", result); // debug

        if (!res.ok) {
            throw new Error(result.message);
        }

        const data = result.data?.items || [];

        container.innerHTML = "";

        if (data.length === 0) {
            container.innerHTML = "No businesses found";
            return;
        }

        data.forEach(b => {
            const div = document.createElement("div");
            div.innerHTML = `<strong>${b.name}</strong> (${b.category})`;
            container.appendChild(div);
        });

    } catch (err) {
        console.error("Business Load Error:", err);
        container.innerHTML = "Error loading businesses";
    }
}


// LOAD PLACES
async function loadPlaces() {
    const container = document.getElementById("placeList");
    container.innerHTML = "Loading places...";

    try {
        const res = await fetch("http://localhost:8081/api/places");
        const result = await res.json();

        console.log("PLACES:", result); // debug

        if (!res.ok) {
            throw new Error(result.message);
        }

        const data = result.data || [];

        container.innerHTML = "";

        if (data.length === 0) {
            container.innerHTML = "No places available";
            return;
        }

        data.forEach(p => {
            const div = document.createElement("div");
            div.innerHTML = `<strong>${p.name}</strong> (${p.category})`;
            container.appendChild(div);
        });

    } catch (err) {
        console.error("Place Load Error:", err);
        container.innerHTML = "Error loading places";
    }
}


// INIT
window.onload = function () {
    loadBusinesses();
    loadPlaces();
};