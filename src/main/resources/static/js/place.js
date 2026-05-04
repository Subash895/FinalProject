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
    searchToken: 0,
    selectionMarker: null,
    userLocationMarker: null,
    userLocation: getSavedUserLocation()
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
    setListSkeleton(container, "card", 4);

    try {
        if (!placeState.userLocation) {
            placeState.userLocation = await getUserLocation().catch(() => null);
        }

        const places = await apiRequest(`/places${getFilterQuery()}`);
        const placesWithReviews = await attachReviewsToItems(places || [], REVIEW_TARGETS.place);
        const placesWithDetails = placesWithReviews.map(place => ({
            ...place,
            distanceKm: placeState.userLocation && place.latitude != null && place.longitude != null ?
                haversineDistanceKm(placeState.userLocation, {
                    lat: place.latitude,
                    lng: place.longitude
                }) :
                null
        }));

        updateResultSummary(placesWithDetails?.length || 0);
        syncPlaceMap(placesWithDetails || []);

        if (!placesWithDetails || placesWithDetails.length === 0) {
            clearListSkeleton(container);
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">PL</div><p>No places match the current filter.</p></div>';
            return;
        }

        container.className = "place-list";
        clearListSkeleton(container);
        container.innerHTML = placesWithDetails.map((place, index) => renderPlaceCard(place, index)).join("");
        hydrateReviewForms(loadPlaces);
    } catch {
        updateResultSummary(0);
        clearListSkeleton(container);
        container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">NA</div><p>Cannot connect to server.</p></div>';
    }
}

function renderPlaceCard(place, index) {
    return `
      <article class="place-card glass-card place-card-linkable" style="animation-delay:${index * 0.05}s" onclick="handlePlaceCardClick(event, ${place.id})">
        ${place.imageUrl ? `<img class="place-card-image" src="${place.imageUrl}" alt="${place.name || "Place"}">` : ""}
        <div class="place-top">
          <div class="place-icon-wrap">${getPlaceBadge(place.category)}</div>
          <div class="place-info">
            <div class="place-header-row">
              <h3>${place.name || "Unnamed Place"}</h3>
              <span class="place-cat">${place.category || "General"}</span>
            </div>
            <div class="place-city">City: ${place.city?.name || "Not linked"}</div>
            <div class="place-loc">Location: ${place.location || "Not provided"}</div>
            ${place.distanceKm != null ? `<div class="place-distance">${formatDistanceKm(place.distanceKm)}</div>` : ""}
            ${place.description ? `<p class="place-desc">${place.description}</p>` : ""}
            ${place.latitude && place.longitude ? `<p class="place-desc"><a href="https://www.openstreetmap.org/?mlat=${place.latitude}&mlon=${place.longitude}#map=16/${place.latitude}/${place.longitude}" target="_blank" rel="noopener noreferrer">Open in OpenStreetMap</a></p>` : ""}
          </div>
        </div>
        <div class="card-actions">
          <button class="btn btn-secondary btn-sm" onclick="focusPlaceOnMap(${place.id})">Map</button>
          ${isAdmin() ? `<button class="btn btn-secondary btn-sm" title="Upload" onclick="triggerPlaceImagePicker(${place.id})">Upload</button><input id="placeImageInput_${place.id}" class="place-image-input" type="file" accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.svg,.ico,.tif,.tiff,image/*" onchange="handlePlaceImageSelected(${place.id}, event)">` : ""}
          ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editPlace(${place.id}, ${JSON.stringify(place)})'>Edit</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deletePlace(${place.id}, '${esc(place.name)}')">Delete</button>` : ""}
        </div>
        ${renderReviewSection(REVIEW_TARGETS.place, place.id, place.reviews || [])}
      </article>`;
}

function openPlaceDetailsPage(id) {
    window.location.href = `place-view.html?placeId=${encodeURIComponent(id)}`;
}

function handlePlaceCardClick(event, placeId) {
    if (!event || !placeId) {
        return;
    }
    const interactiveArea = event.target.closest("button, a, input, textarea, select, label, form, .review-section, .card-actions");
    if (interactiveArea) {
        return;
    }
    openPlaceDetailsPage(placeId);
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
        placeState.map.on("click", handlePlaceMapClick);
        addCurrentLocationControl(placeState.map, {
            title: "Show my current location",
            onClick: showCurrentLocationOnPlaceMap
        });
        setPlaceMapStatus("Map ready. Click the map to fill a location, search an address, or click a place marker.");
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
        if (!placeState.selectedCoordinates) {
            placeState.map.fitBounds(bounds, {
                padding: [24, 24]
            });
            if (count === 1) {
                placeState.map.setZoom(1);
            }
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
    showPlaceSelectionMarker(coords, label);
    setPlaceMapStatus(`Focused on ${label}.`);
}

function showPlaceSelectionMarker(coords, label) {
    if (!placeState.map || !coords) {
        return;
    }

    if (!placeState.selectionMarker) {
        placeState.selectionMarker = L.marker([coords.lat, coords.lng], {
            title: label || "Selected place"
        }).addTo(placeState.map);
    } else {
        placeState.selectionMarker.setLatLng([coords.lat, coords.lng]);
        placeState.selectionMarker.options.title = label || "Selected place";
    }

    if (label) {
        placeState.selectionMarker.bindPopup(label);
    }
}

async function showCurrentLocationOnPlaceMap() {
    if (!placeState.map) {
        return;
    }

    setPlaceMapStatus("Getting your current location...");

    try {
        const coords = await getCurrentBrowserLocation();
        saveUserLocation(coords);
        placeState.userLocation = coords;
        placeState.userLocationMarker = showUserLocationMarker(placeState.map, coords, {
            marker: placeState.userLocationMarker,
            label: "Me",
            popupText: "Me"
        });
        placeState.map.flyTo([coords.lat, coords.lng], 14, {
            duration: 0.8
        });
        placeState.userLocationMarker?.openPopup();
        setPlaceMapStatus("Showing your current location.");
    } catch (error) {
        showToast(error.message || "Failed to get current location.", "error");
        setPlaceMapStatus(error.message || "Failed to get current location.");
    }
}

async function handlePlaceMapClick(event) {
    const lat = event?.latlng?.lat;
    const lng = event?.latlng?.lng;
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        return;
    }

    const locationInput = document.getElementById("location");
    const citySelect = document.getElementById("cityId");
    if (!locationInput) {
        return;
    }

    setPlaceLocationStatus("Reading place details from the selected map point...");

    try {
        const result = await reverseGeocodeWithOpenStreetMap(lat, lng);
        const label = result?.displayName || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;

        locationInput.value = label;
        placeState.selectedCoordinates = { lat, lng };
        placeState.searchResults = [];
        renderPlaceSearchResults();

        const cityId = resolveCurrentLocationCityId(result?.address || {});
        if (citySelect && cityId) {
            citySelect.value = String(cityId);
        }

        focusMapOnCoordinates({ lat, lng }, label);
        setPlaceLocationStatus(cityId ? "Place and city filled from the selected map point." : "Place filled from the selected map point.");
    } catch (error) {
        setPlaceLocationStatus(error.message || "Failed to read place details from the map point.");
        showToast(error.message || "Failed to read place details from the map point.", "error");
    }
}

async function ensurePlaceMarker(placeId) {
    const place = await apiRequest(`/places/${placeId}`);
    if (!place || !placeState.map) {
        return null;
    }

    const existingMarker = placeState.markers.find(item => item.placeId === placeId);
    if (existingMarker) {
        return existingMarker;
    }

    let coords = null;
    if (place.latitude != null && place.longitude != null) {
        coords = {
            lat: place.latitude,
            lng: place.longitude
        };
    } else {
        coords = await geocodePlaceLocation(place.location, place.city);
    }

    if (!coords) {
        return null;
    }

    const marker = L.marker([coords.lat, coords.lng], {
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
    return marker;
}

async function focusPlaceOnMap(placeId) {
    setPlaceMapStatus("Locating place on the map...");
    const marker = await ensurePlaceMarker(placeId);
    if (!marker) {
        showToast("Place location could not be resolved.", "error");
        setPlaceMapStatus("Place location could not be resolved.");
        return;
    }

    placeState.map.panTo(marker.getLatLng());
    placeState.map.setZoom(14);
    marker.openPopup();
    setPlaceMapStatus("Focused on selected place.");
}

function triggerPlaceImagePicker(id) {
    document.getElementById(`placeImageInput_${id}`)?.click();
}

async function handlePlaceImageSelected(id, event) {
    const file = event?.target?.files?.[0];
    if (!file) {
        return;
    }
    if (!file.type || !file.type.startsWith("image/")) {
        showToast("Only image files are allowed.", "error");
        event.target.value = "";
        return;
    }
    try {
        const formData = new FormData();
        formData.append("photo", file);
        const token = localStorage.getItem("token");
        const response = await fetch(`${API_BASE}/places/${Number(id)}/photo`, {
            method: "PUT",
            headers: token ? { Authorization: `Bearer ${token}` } : {},
            body: formData
        });
        if (!response.ok) {
            let message = "Failed to upload place image.";
            try {
                message = await response.text() || message;
            } catch {}
            throw new Error(message);
        }
        showToast("Place image updated.", "success");
        await loadPlaces();
    } catch (error) {
        showToast(error.message || "Failed to upload place image.", "error");
    } finally {
        event.target.value = "";
    }
}
