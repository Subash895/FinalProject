/**
 * Client-side behavior for the place page, including event handling and API calls.
 */
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
    selectedCoordinates: null,
    searchResults: [],
    searchToken: 0
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

    summary.textContent = filters.length === 0 ?
        `Showing all places (${count})` :
        `Showing ${count} place(s) for ${filters.join(" and ")}`;
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("placeForm")?.addEventListener("submit", createPlace);
    document.getElementById("filterForm")?.addEventListener("submit", applyFilters);
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
    document.getElementById("placeCurrentLocationBtn")?.addEventListener("click", fillPlaceFromCurrentLocation);
    document.getElementById("loadBtn")?.addEventListener("click", loadPlaces);
    attachPlaceSearchHandlers();
    loadCitiesForSelect();
    initPlaceMap();
    loadPlaces();
});

function setPlaceLocationStatus(message) {
    const status = document.getElementById("placeLocationStatus");
    if (status) {
        status.textContent = message;
    }
}


function attachPlaceSearchHandlers() {
    const locationInput = document.getElementById("location");
    const citySelect = document.getElementById("cityId");
    if (!locationInput) {
        return;
    }

    const scheduleSearch = debounce(() => {
        searchPlaceLocation(locationInput.value.trim());
    }, 350);

    locationInput.addEventListener("input", () => {
        placeState.selectedCoordinates = null;
        scheduleSearch();
    });

    citySelect?.addEventListener("change", () => {
        placeState.selectedCoordinates = null;
        if (locationInput.value.trim()) {
            scheduleSearch();
        }
    });
}

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
            city: cityId ? {
                id: cityId
            } : null,
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
    } catch (error) {
        showToast(error.message || "Failed to add place.", "error");
    } finally {
        button.disabled = false;
        button.textContent = "Add Place";
    }
}

function debounce(callback, delay) {
    let timeoutId = null;
    return (...args) => {
        window.clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => callback(...args), delay);
    };
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
        const placesWithReviews = await Promise.all((places || []).map(async place => ({
            ...place,
            reviews: await loadReviews(REVIEW_TARGETS.place, place.id)
        })));

        updateResultSummary(placesWithReviews?.length || 0);
        syncPlaceMap(placesWithReviews || []);

        if (!placesWithReviews || placesWithReviews.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">PL</div><p>No places match the current filter.</p></div>';
            return;
        }

        container.className = "place-list";
        container.innerHTML = placesWithReviews.map((place, index) => renderPlaceCard(place, index)).join("");
        hydrateReviewForms(loadPlaces);
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
        ${renderReviewSection(REVIEW_TARGETS.place, place.id, place.reviews || [])}
      </article>`;
}

function editPlace(id, place) {
    openEditModal({
        title: "Edit Place",
        fields: [{
                key: "name",
                label: "Place Name",
                placeholder: "e.g. Cubbon Park"
            },
            {
                key: "category",
                label: "Category",
                placeholder: "e.g. Park"
            },
            {
                key: "location",
                label: "Location",
                placeholder: "Search address or landmark"
            },
            {
                key: "description",
                label: "Description",
                placeholder: "Short place description"
            }
        ],
        values: {
            name: place.name,
            category: place.category,
            location: place.location,
            description: place.description
        },
        onSave: async (data) => {
            const coordinates = data.location && data.location !== place.location ?
                await geocodePlaceLocation(data.location, place.city) :
                {
                    lat: place.latitude,
                    lng: place.longitude
                };

            await apiRequest(`/places/${id}`, "PUT", {
                ...data,
                city: place.city ? {
                    id: place.city.id
                } : null,
                latitude: coordinates?.lat ?? null,
                longitude: coordinates?.lng ?? null
            });
            await loadPlaces();
        }
    });
}

async function geocodePlaceLocation(location, city) {
    const address = [location, city?.name, city?.state, city?.country].filter(Boolean).join(", ");
    return geocodeWithOpenStreetMap(address);
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

    const coordinates = await geocodePlaceLocation(location, city);
    if (!coordinates) {
        return null;
    }

    placeState.selectedCoordinates = {
        lat: coordinates.lat,
        lng: coordinates.lng
    };
    focusMapOnCoordinates(placeState.selectedCoordinates, coordinates.label || location);
    return placeState.selectedCoordinates;
}

async function searchPlaceLocation(rawLocation) {
    const resultsContainer = document.getElementById("placeSearchResults");
    if (!resultsContainer) {
        return;
    }

    const cityId = Number(document.getElementById("cityId")?.value);
    const city = placeState.cities.find(item => item.id === cityId);
    const query = [rawLocation, city?.name, city?.state, city?.country].filter(Boolean).join(", ");

    if (!rawLocation || rawLocation.length < 3) {
        placeState.searchResults = [];
        renderPlaceSearchResults();
        setPlaceMapStatus("Type at least 3 letters to search for a location.");
        return;
    }

    const currentToken = ++placeState.searchToken;

    try {
        setPlaceMapStatus(`Searching map for "${rawLocation}"...`);
        const results = await searchWithOpenStreetMap(query, {
            limit: 5
        });

        if (currentToken !== placeState.searchToken) {
            return;
        }

        placeState.searchResults = results || [];
        renderPlaceSearchResults();

        if (!placeState.searchResults.length) {
            setPlaceMapStatus(`No matching location found for "${rawLocation}".`);
            return;
        }

        focusMapOnCoordinates(placeState.searchResults[0], placeState.searchResults[0].displayName || rawLocation);
    } catch (error) {
        console.error(error);
        if (currentToken !== placeState.searchToken) {
            return;
        }
        placeState.searchResults = [];
        renderPlaceSearchResults();
        setPlaceMapStatus("Location search failed.");
    }
}

function renderPlaceSearchResults() {
    const container = document.getElementById("placeSearchResults");
    if (!container) {
        return;
    }

    if (!placeState.searchResults.length) {
        container.hidden = true;
        container.innerHTML = "";
        return;
    }

    container.hidden = false;
    container.innerHTML = placeState.searchResults.map((result, index) => `
        <button type="button" class="map-search-option" onclick="selectPlaceSearchResult(${index})">
            ${result.name || "Suggested location"}
            <small>${result.displayName || ""}</small>
        </button>
    `).join("");
}

function resolveCurrentLocationCityId(address = {}) {
    const cityName = address.city || address.town || address.village || address.municipality || "";
    const state = address.state || address.region || address.county || "";
    const country = address.country || "";

    const matchedCity = placeState.cities.find(city =>
        String(city.name || "").trim().toLowerCase() === cityName.trim().toLowerCase() &&
        String(city.country || "").trim().toLowerCase() === country.trim().toLowerCase() &&
        (!state || String(city.state || "").trim().toLowerCase() === state.trim().toLowerCase())
    );

    return matchedCity?.id || null;
}

async function fillPlaceFromCurrentLocation() {
    const button = document.getElementById("placeCurrentLocationBtn");
    const locationInput = document.getElementById("location");
    const citySelect = document.getElementById("cityId");
    if (!button || !locationInput) {
        return;
    }

    button.disabled = true;
    setPlaceLocationStatus("Getting your current position...");

    try {
        const coords = await getCurrentBrowserLocation();
        saveUserLocation(coords);
        setPlaceLocationStatus("Resolving location from map data...");
        const result = await reverseGeocodeWithOpenStreetMap(coords.lat, coords.lng);
        const label = result?.displayName || `${coords.lat.toFixed(5)}, ${coords.lng.toFixed(5)}`;

        locationInput.value = label;
        placeState.selectedCoordinates = {
            lat: coords.lat,
            lng: coords.lng
        };

        const cityId = resolveCurrentLocationCityId(result?.address || {});
        if (citySelect && cityId) {
            citySelect.value = String(cityId);
        }

        placeState.searchResults = [];
        renderPlaceSearchResults();
        focusMapOnCoordinates(placeState.selectedCoordinates, label);
        setPlaceLocationStatus(cityId ? "Current location applied and matching city selected." : "Current location applied.");
    } catch (error) {
        setPlaceLocationStatus(error.message || "Failed to use current location.");
        showToast(error.message || "Failed to use current location.", "error");
    } finally {
        button.disabled = false;
    }
}


function selectPlaceSearchResult(index) {
    const result = placeState.searchResults[index];
    if (!result) {
        return;
    }

    const locationInput = document.getElementById("location");
    if (locationInput) {
        locationInput.value = result.displayName || result.name || locationInput.value;
    }

    placeState.selectedCoordinates = {
        lat: result.lat,
        lng: result.lng
    };
    placeState.searchResults = [];
    renderPlaceSearchResults();
    focusMapOnCoordinates(placeState.selectedCoordinates, result.displayName || result.name || "selected location");
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
        placeState.map.fitBounds(bounds, {
            padding: [24, 24]
        });
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
