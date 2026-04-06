const CITY_EMOJIS = ["CT", "SK", "PT", "HC", "TR", "MT", "BF", "RW"];

function esc(value) {
    return String(value || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

const cityMapState = {
    cities: [],
    map: null,
    markers: new Map()
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("cityForm")?.addEventListener("submit", createCity);
    document.getElementById("loadBtn")?.addEventListener("click", loadCities);
    initCityMap();
    loadCities();
});

async function createCity(event) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    button.textContent = "Saving...";

    try {
        await apiRequest("/cities", "POST", {
            name: document.getElementById("name").value.trim(),
            state: document.getElementById("state").value.trim(),
            country: document.getElementById("country").value.trim()
        });
        form.reset();
        showToast("City added.", "success");
        await loadCities();
    } catch {
        showToast("Failed to add city.", "error");
    } finally {
        button.disabled = false;
        button.textContent = "Add City";
    }
}

async function loadCities() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const cities = await apiRequest("/cities");
        cityMapState.cities = cities || [];

        if (!cities || cities.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">CT</div><p>No cities yet. Add the first one.</p></div>';
            syncCityMap();
            return;
        }

        container.className = "city-list";
        container.innerHTML = cities.map((city, index) => `
            <div class="city-card glass-card" style="animation-delay:${index * 0.05}s">
              <div class="city-avatar">${CITY_EMOJIS[index % CITY_EMOJIS.length]}</div>
              <div class="city-info">
                <h3>${city.name}</h3>
                ${city.state ? `<div class="city-state">${city.state}</div>` : ""}
                <div class="city-country">${city.country || "Country not set"}</div>
              </div>
              <div class="city-actions">
                <button class="btn btn-secondary btn-sm" onclick="focusCityOnMap(${city.id})">Map</button>
                ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editCity(${city.id}, ${JSON.stringify(city)})'>Edit</button>` : ""}
                ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteCity(${city.id}, '${esc(city.name)}')">Delete</button>` : ""}
              </div>
            </div>
        `).join("");

        syncCityMap();
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">NA</div><p>Cannot connect to server.</p></div>';
        setCityMapStatus("City data failed to load.");
    }
}

function editCity(id, city) {
    openEditModal({
        title: "Edit City",
        fields: [
            { key: "name", label: "City Name", placeholder: "e.g. Bengaluru" },
            { key: "state", label: "State", placeholder: "e.g. Karnataka" },
            { key: "country", label: "Country", placeholder: "e.g. India" }
        ],
        values: {
            name: city.name,
            state: city.state,
            country: city.country
        },
        onSave: async (data) => {
            await apiRequest(`/cities/${id}`, "PUT", data);
            await loadCities();
        }
    });
}

function deleteCity(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => {
            await apiRequest(`/cities/${id}`, "DELETE");
            await loadCities();
        }
    });
}

async function initCityMap() {
    try {
        cityMapState.map = createOpenStreetMap("cityMap", {
            center: OSM_DEFAULT_CENTER,
            zoom: 5
        });
        setCityMapStatus("Map ready. Use any city card to focus a marker.");
        syncCityMap();
    } catch (error) {
        console.error(error);
        setCityMapStatus("OpenStreetMap could not be loaded.");
    }
}

function setCityMapStatus(message) {
    const status = document.getElementById("cityMapStatus");
    if (status) {
        status.textContent = message;
    }
}

function cityAddress(city) {
    return [city.name, city.state, city.country].filter(Boolean).join(", ");
}

async function syncCityMap() {
    if (!cityMapState.map) {
        return;
    }

    cityMapState.markers.forEach(marker => marker.remove());
    cityMapState.markers.clear();

    if (cityMapState.cities.length === 0) {
        setCityMapStatus("No cities available to map.");
        return;
    }

    const bounds = [];
    let markerCount = 0;

    for (const city of cityMapState.cities) {
        try {
            const location = await geocodeWithOpenStreetMap(cityAddress(city));
            if (!location) {
                continue;
            }

            const marker = L.marker([location.lat, location.lng], {
                title: city.name
            }).addTo(cityMapState.map);
            marker.bindPopup(cityPopupContent(city));
            cityMapState.markers.set(city.id, marker);
            bounds.push([location.lat, location.lng]);
            markerCount += 1;
        } catch (error) {
            console.warn(`Failed to geocode city ${city.name}`, error);
        }
    }

    if (markerCount > 0) {
        cityMapState.map.fitBounds(bounds, { padding: [24, 24] });
        if (markerCount === 1) {
            cityMapState.map.setZoom(10);
        }
        setCityMapStatus(`${markerCount} city marker(s) loaded.`);
    } else {
        setCityMapStatus("No city markers could be resolved.");
    }
}

function cityPopupContent(city) {
    return `
        <div style="min-width:180px">
            <strong>${city.name}</strong><br>
            ${city.state ? `${city.state}<br>` : ""}
            ${city.country || ""}
        </div>
    `;
}

function focusCityOnMap(cityId) {
    const marker = cityMapState.markers.get(cityId);
    const city = cityMapState.cities.find(item => item.id === cityId);
    if (!marker || !city) {
        showToast("City marker is not ready yet.", "info");
        return;
    }

    cityMapState.map.panTo(marker.getLatLng());
    cityMapState.map.setZoom(11);
    marker.openPopup();
}
