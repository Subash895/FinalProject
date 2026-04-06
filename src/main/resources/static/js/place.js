const PLACE_BADGES = {
    park: "PK",
    museum: "MU",
    restaurant: "RS",
    hospital: "HS",
    school: "SC",
    mall: "ML",
    temple: "TM",
    beach: "BC",
    default: "PL"
};

const placeState = {
    category: "",
    location: "",
    cities: [],
    map: null,
    markers: [],
    selectedCoordinates: null
};

function getPlaceBadge(category = "") {
    const normalized = String(category || "").toLowerCase();
    for (const [key, badge] of Object.entries(PLACE_BADGES)) {
        if (key !== "default" && normalized.includes(key)) {
            return badge;
        }
    }
    return PLACE_BADGES.default;
}

function esc(value) {
    return String(value || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function getFilterQuery() {
    const params = new URLSearchParams();

    if (placeState.category) {
        params.set("category", placeState.category);
    }
    if (placeState.location) {
        params.set("location", placeState.location);
    }

    const query = params.toString();
    return query ? `?${query}` : "";
}

function updateResultSummary(count) {
    const summary = document.getElementById("resultSummary");
    if (!summary) {
        return;
    }

    const filters = [];
    if (placeState.category) {
        filters.push(`category "${placeState.category}"`);
    }
    if (placeState.location) {
        filters.push(`location "${placeState.location}"`);
    }

    summary.textContent = filters.length === 0
        ? `Showing all places (${count})`
        : `Showing ${count} place(s) for ${filters.join(" and ")}`;
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("placeForm")?.addEventListener("submit", createPlace);
    document.getElementById("filterForm")?.addEventListener("submit", applyFilters);
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
    document.getElementById("loadBtn")?.addEventListener("click", loadPlaces);
    loadCitiesForSelect();
    initPlaceMap();
    loadPlaces();
});

async function createPlace(event) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    button.textContent = "Saving...";

    try {
        const cityId = Number(document.getElementById("cityId").value);
        const city = placeState.cities.find(item => item.id === cityId);
        const locationInput = document.getElementById("location").value.trim();
        const coordinates = await resolvePlaceCoordinates(locationInput, city);

        await apiRequest("/places", "POST", {
            city: cityId ? { id: cityId } : null,
            name: document.getElementById("name").value.trim(),
            category: document.getElementById("category").value.trim(),
            location: locationInput,
            description: document.getElementById("description").value.trim(),
            latitude: coordinates?.lat ?? null,
            longitude: coordinates?.lng ?? null
        });

        form.reset();
        placeState.selectedCoordinates = null;
        showToast("Place added.", "success");
        await loadPlaces();
    } catch {
        showToast("Failed to add place.", "error");
    } finally {
        button.disabled = false;
        button.textContent = "Add Place";
    }
}

function applyFilters(event) {
    event.preventDefault();
    placeState.category = document.getElementById("filterCategory").value.trim();
    placeState.location = document.getElementById("filterLocation").value.trim();
    loadPlaces();
}

function clearFilters() {
    placeState.category = "";
    placeState.location = "";
    document.getElementById("filterCategory").value = "";
    document.getElementById("filterLocation").value = "";
    loadPlaces();
}

async function loadPlaces() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const places = await apiRequest(`/places${getFilterQuery()}`);
        updateResultSummary(places?.length || 0);
        syncPlaceMap(places || []);

        if (!places || places.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">PL</div><p>No places match the current filter.</p></div>';
            return;
        }

        container.className = "place-list";
        container.innerHTML = places.map((place, index) => renderPlaceCard(place, index)).join("");
    } catch {
        updateResultSummary(0);
        container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">NA</div><p>Cannot connect to server.</p></div>';
    }
}

function renderPlaceCard(place, index) {
    return `
      <article class="place-card glass-card" style="animation-delay:${index * 0.05}s">
        <div class="place-top">
          <div class="place-icon-wrap">${getPlaceBadge(place.category)}</div>
          <div class="place-info">
            <div class="place-header-row">
              <h3>${place.name || "Unnamed Place"}</h3>
              <span class="place-cat">${place.category || "General"}</span>
            </div>
            <div class="place-city">City: ${place.city?.name || "Not linked"}</div>
            <div class="place-loc">Location: ${place.location || "Not provided"}</div>
            ${place.description ? `<p class="place-desc">${place.description}</p>` : ""}
            ${place.latitude && place.longitude ? `<p class="place-desc"><a href="https://www.openstreetmap.org/?mlat=${place.latitude}&mlon=${place.longitude}#map=16/${place.latitude}/${place.longitude}" target="_blank" rel="noopener noreferrer">Open in OpenStreetMap</a></p>` : ""}
          </div>
        </div>
        <div class="card-actions">
          <button class="btn btn-secondary btn-sm" onclick="focusPlaceOnMap(${place.id})">Map</button>
          ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editPlace(${place.id}, ${JSON.stringify(place)})'>Edit</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deletePlace(${place.id}, '${esc(place.name)}')">Delete</button>` : ""}
        </div>
      </article>`;
}

function editPlace(id, place) {
    openEditModal({
        title: "Edit Place",
        fields: [
            { key: "name", label: "Place Name", placeholder: "e.g. Cubbon Park" },
            { key: "category", label: "Category", placeholder: "e.g. Park" },
            { key: "location", label: "Location", placeholder: "Search address or landmark" },
            { key: "description", label: "Description", placeholder: "Short place description" }
        ],
        values: {
            name: place.name,
            category: place.category,
            location: place.location,
            description: place.description
        },
        onSave: async (data) => {
            await apiRequest(`/places/${id}`, "PUT", data);
            await loadPlaces();
        }
    });
}

function deletePlace(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => {
            await apiRequest(`/places/${id}`, "DELETE");
            await loadPlaces();
        }
    });
}

async function loadCitiesForSelect() {
    try {
        const cities = await apiRequest("/cities");
        placeState.cities = cities || [];

        const select = document.getElementById("cityId");
        if (!select) {
            return;
        }

        select.innerHTML = `<option value="">Select a city</option>${placeState.cities.map(city =>
            `<option value="${city.id}">${city.name}${city.state ? `, ${city.state}` : ""}${city.country ? `, ${city.country}` : ""}</option>`
        ).join("")}`;
    } catch {
        showToast("Failed to load cities for places.", "error");
    }
}

async function initPlaceMap() {
    try {
        placeState.map = createOpenStreetMap("placeMap", {
            center: OSM_DEFAULT_CENTER,
            zoom: 5
        });
        setPlaceMapStatus("Map ready. Search a location or click a place marker.");
    } catch (error) {
        console.error(error);
        setPlaceMapStatus("OpenStreetMap could not be loaded.");
    }
}

function setPlaceMapStatus(message) {
    const status = document.getElementById("placeMapStatus");
    if (status) {
        status.textContent = message;
    }
}

async function resolvePlaceCoordinates(location, city) {
    if (placeState.selectedCoordinates) {
        return placeState.selectedCoordinates;
    }

    if (!location) {
        return null;
    }

    const address = [location, city?.name, city?.state, city?.country].filter(Boolean).join(", ");
    const coordinates = await geocodeWithOpenStreetMap(address);
    if (!coordinates) {
        return null;
    }

    placeState.selectedCoordinates = { lat: coordinates.lat, lng: coordinates.lng };
    focusMapOnCoordinates(placeState.selectedCoordinates, coordinates.label || address);
    return placeState.selectedCoordinates;
}

function syncPlaceMap(places) {
    if (!placeState.map) {
        return;
    }

    placeState.markers.forEach(marker => marker.remove());
    placeState.markers = [];

    if (!places.length) {
        setPlaceMapStatus("No places available to map.");
        return;
    }

    const bounds = [];
    let count = 0;

    places.forEach(place => {
        if (place.latitude == null || place.longitude == null) {
            return;
        }

        const marker = L.marker([place.latitude, place.longitude], {
            title: place.name
        }).addTo(placeState.map);

        marker.placeId = place.id;
        marker.bindPopup(`
                <div style="min-width:200px">
                    <strong>${place.name}</strong><br>
                    ${place.city?.name ? `${place.city.name}<br>` : ""}
                    ${place.location || ""}
                </div>
            `);

        placeState.markers.push(marker);
        bounds.push([place.latitude, place.longitude]);
        count += 1;
    });

    if (count > 0) {
        placeState.map.fitBounds(bounds, { padding: [24, 24] });
        if (count === 1) {
            placeState.map.setZoom(14);
        }
        setPlaceMapStatus(`${count} mapped place(s) loaded.`);
    } else {
        setPlaceMapStatus("Places loaded, but none have saved coordinates yet.");
    }
}

function focusMapOnCoordinates(coords, label) {
    if (!placeState.map || !coords) {
        return;
    }

    placeState.map.panTo(coords);
    placeState.map.setZoom(14);
    setPlaceMapStatus(`Focused on ${label}.`);
}

function focusPlaceOnMap(placeId) {
    const marker = placeState.markers.find(item => item.placeId === placeId);
    if (!marker) {
        showToast("This place does not have coordinates yet.", "info");
        return;
    }

    placeState.map.panTo(marker.getLatLng());
    placeState.map.setZoom(14);
    marker.openPopup();
}
