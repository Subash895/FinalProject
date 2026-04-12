/**
 * Client-side behavior for the city page, including event handling and API calls.
 */
const CITY_EMOJIS = ["CT", "SK", "PT", "HC", "TR", "MT", "BF", "RW"];

function esc(value) {
    return String(value || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function escapeHtml(value) {
    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

const cityMapState = {
    cities: [],
    map: null,
    markers: new Map(),
    searchResults: [],
    searchToken: 0
};

const cityHistoryState = {
    cityId: null,
    cityName: ""
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("cityForm")?.addEventListener("submit", createCity);
    document.getElementById("cityHistoryForm")?.addEventListener("submit", createCityHistory);
    document.getElementById("cityCurrentLocationBtn")?.addEventListener("click", fillCityFromCurrentLocation);
    document.getElementById("loadBtn")?.addEventListener("click", loadCities);
    document.getElementById("closeHistoryBtn")?.addEventListener("click", closeCityHistory);
    attachCitySearchHandlers();
    initCityMap();
    loadCities();
});

function setCityLocationStatus(message) {
    const status = document.getElementById("cityLocationStatus");
    if (status) {
        status.textContent = message;
    }
}


function debounce(callback, delay) {
    let timeoutId = null;
    return (...args) => {
        window.clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => callback(...args), delay);
    };
}

function attachCitySearchHandlers() {
    const nameInput = document.getElementById("name");
    const stateInput = document.getElementById("state");
    const countryInput = document.getElementById("country");

    if (!nameInput) {
        return;
    }

    const scheduleSearch = debounce(searchCityLocation, 350);
    [nameInput, stateInput, countryInput].forEach(input => {
        input?.addEventListener("input", scheduleSearch);
    });
}

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
    } catch (error) {
        showToast(error.message || "Failed to add city.", "error");
    } finally {
        button.disabled = false;
        button.textContent = "Add City";
    }
}

async function searchCityLocation() {
    const nameInput = document.getElementById("name");
    const stateInput = document.getElementById("state");
    const countryInput = document.getElementById("country");

    if (!nameInput) {
        return;
    }

    const name = nameInput.value.trim();
    const state = stateInput?.value.trim() || "";
    const country = countryInput?.value.trim() || "";
    const query = [name, state, country].filter(Boolean).join(", ");

    if (!name || name.length < 3) {
        cityMapState.searchResults = [];
        renderCitySearchResults();
        setCityMapStatus("Type at least 3 letters to search for a city.");
        return;
    }

    const currentToken = ++cityMapState.searchToken;

    try {
        setCityMapStatus(`Searching map for "${name}"...`);
        const results = await searchWithOpenStreetMap(query, {
            limit: 5
        });

        if (currentToken !== cityMapState.searchToken) {
            return;
        }

        cityMapState.searchResults = results || [];
        renderCitySearchResults();

        if (!cityMapState.searchResults.length) {
            setCityMapStatus(`No city match found for "${name}".`);
            return;
        }

        focusCoordinatesOnCityMap(cityMapState.searchResults[0], cityMapState.searchResults[0].displayName || name);
    } catch (error) {
        console.error(error);
        if (currentToken !== cityMapState.searchToken) {
            return;
        }
        cityMapState.searchResults = [];
        renderCitySearchResults();
        setCityMapStatus("City search failed.");
    }
}

function renderCitySearchResults() {
    const container = document.getElementById("citySearchResults");
    if (!container) {
        return;
    }

    if (!cityMapState.searchResults.length) {
        container.hidden = true;
        container.innerHTML = "";
        return;
    }

    container.hidden = false;
    container.innerHTML = cityMapState.searchResults.map((result, index) => `
        <button type="button" class="map-search-option" onclick="selectCitySearchResult(${index})">
            ${result.name || "Suggested city"}
            <small>${result.displayName || ""}</small>
        </button>
    `).join("");
}

function selectCitySearchResult(index) {
    const result = cityMapState.searchResults[index];
    if (!result) {
        return;
    }

    const fields = extractCityFields(result);
    const nameInput = document.getElementById("name");
    const stateInput = document.getElementById("state");
    const countryInput = document.getElementById("country");

    if (nameInput && fields.name) {
        nameInput.value = fields.name;
    }
    if (stateInput) {
        stateInput.value = fields.state || "";
    }
    if (countryInput && fields.country) {
        countryInput.value = fields.country;
    }

    cityMapState.searchResults = [];
    renderCitySearchResults();
    focusCoordinatesOnCityMap(result, result.displayName || result.name || "selected city");
}

function extractCityFields(result) {
    const address = result?.address || {};
    return {
        name: address.city || address.town || address.village || address.municipality || result?.name || "",
        state: address.state || address.region || address.county || "",
        country: address.country || ""
    };
}

async function fillCityFromCurrentLocation() {
    const button = document.getElementById("cityCurrentLocationBtn");
    if (!button) {
        return;
    }

    button.disabled = true;
    setCityLocationStatus("Getting your current position...");

    try {
        const coords = await getCurrentBrowserLocation();
        saveUserLocation(coords);
        setCityLocationStatus("Resolving city details from map data...");
        const result = await reverseGeocodeWithOpenStreetMap(coords.lat, coords.lng);
        const fields = extractCityFields(result);

        if (!fields.name || !fields.country) {
            throw new Error("Current location did not return enough city details.");
        }

        const nameInput = document.getElementById("name");
        const stateInput = document.getElementById("state");
        const countryInput = document.getElementById("country");

        if (nameInput) {
            nameInput.value = fields.name;
        }
        if (stateInput) {
            stateInput.value = fields.state || "";
        }
        if (countryInput) {
            countryInput.value = fields.country;
        }

        cityMapState.searchResults = [];
        renderCitySearchResults();
        focusCoordinatesOnCityMap(coords, result?.displayName || fields.name);
        setCityLocationStatus("Current city details applied.");
    } catch (error) {
        setCityLocationStatus(error.message || "Failed to use current location.");
        showToast(error.message || "Failed to use current location.", "error");
    } finally {
        button.disabled = false;
    }
}


function syncCityHistoryAdminUI() {
    const citySelect = document.getElementById("historyCityId");
    if (!citySelect) {
        return;
    }

    const currentValue = citySelect.value;
    citySelect.innerHTML = `<option value="">Select a city</option>${cityMapState.cities
        .map(city => `<option value="${city.id}">${escapeHtml(city.name)}</option>`)
        .join("")}`;

    const selectedValue = cityHistoryState.cityId ? String(cityHistoryState.cityId) : currentValue;
    if (selectedValue) {
        citySelect.value = selectedValue;
    }
}

async function createCityHistory(event) {
    event.preventDefault();
    const cityId = Number(document.getElementById("historyCityId")?.value);
    if (!cityId) {
        showToast("Select a city before adding history.", "info");
        return;
    }

    const form = event.target;
    const button = document.getElementById("saveHistoryBtn");
    const selectedCity = cityMapState.cities.find(city => city.id === cityId);
    const payload = {
        cityId,
        title: document.getElementById("historyTitle").value.trim(),
        content: document.getElementById("historyContent").value.trim()
    };

    button.disabled = true;
    button.innerHTML = '<span class="spinner"></span> Saving...';

    try {
        await apiRequest("/cityhistory", "POST", payload);
        form.reset();
        syncCityHistoryAdminUI();
        showToast("City history added.", "success");
        if (selectedCity) {
            await openCityHistory(selectedCity.id, selectedCity.name);
        }
    } catch (error) {
        showToast(error.message || "Failed to add city history.", "error");
    } finally {
        button.disabled = false;
        button.textContent = "Save History";
    }
}

async function loadCities() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const cities = await apiRequest("/cities");
        const citiesWithReviews = await Promise.all((cities || []).map(async city => ({
            ...city,
            reviews: await loadReviews(REVIEW_TARGETS.city, city.id)
        })));
        cityMapState.cities = citiesWithReviews;
        syncCityHistoryAdminUI();

        if (cityHistoryState.cityId) {
            const selectedCity = citiesWithReviews.find(city => city.id === cityHistoryState.cityId);
            if (selectedCity) {
                openCityHistory(selectedCity.id, selectedCity.name);
            } else {
                closeCityHistory();
            }
        }

        if (citiesWithReviews.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">CT</div><p>No cities yet. Add the first one.</p></div>';
            syncCityMap();
            return;
        }

        container.className = "city-list";
        container.innerHTML = citiesWithReviews.map((city, index) => `
            <div class="city-card glass-card" style="animation-delay:${index * 0.05}s">
              <div class="city-avatar">${CITY_EMOJIS[index % CITY_EMOJIS.length]}</div>
              <div class="city-info">
                <h3><button type="button" class="city-link" onclick="openCityHistory(${city.id}, '${esc(city.name)}')">${city.name}</button></h3>
                ${city.state ? `<div class="city-state">${city.state}</div>` : ""}
                <div class="city-country">${city.country || "Country not set"}</div>
                ${renderReviewSection(REVIEW_TARGETS.city, city.id, city.reviews || [])}
              </div>
              <div class="city-actions">
                <button class="btn btn-primary btn-sm" onclick="openCityHistory(${city.id}, '${esc(city.name)}')">History</button>
                <button class="btn btn-secondary btn-sm" onclick="focusCityOnMap(${city.id})">Map</button>
                ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editCity(${city.id}, ${JSON.stringify(city)})'>Edit</button>` : ""}
                ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteCity(${city.id}, '${esc(city.name)}')">Delete</button>` : ""}
              </div>
            </div>
        `).join("");

        syncCityMap();
        hydrateReviewForms(loadCities);
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">NA</div><p>Cannot connect to server.</p></div>';
        setCityMapStatus("City data failed to load.");
    }
}

function editCity(id, city) {
    openEditModal({
        title: "Edit City",
        fields: [{
                key: "name",
                label: "City Name",
                placeholder: "e.g. Bengaluru"
            },
            {
                key: "state",
                label: "State",
                placeholder: "e.g. Karnataka"
            },
            {
                key: "country",
                label: "Country",
                placeholder: "e.g. India"
            }
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
        cityMapState.map.fitBounds(bounds, {
            padding: [24, 24]
        });
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

function focusCoordinatesOnCityMap(coords, label) {
    if (!cityMapState.map || !coords) {
        return;
    }

    cityMapState.map.panTo([coords.lat, coords.lng]);
    cityMapState.map.setZoom(10);
    setCityMapStatus(`Focused on ${label}.`);
}

async function openCityHistory(cityId, cityName) {
    const panel = document.getElementById("cityHistoryPanel");
    const title = document.getElementById("cityHistoryTitle");
    const meta = document.getElementById("cityHistoryMeta");
    const list = document.getElementById("cityHistoryList");

    cityHistoryState.cityId = cityId;
    cityHistoryState.cityName = cityName;
    syncCityHistoryAdminUI();

    panel.hidden = false;
    title.textContent = `${cityName} History`;
    meta.textContent = `Loading history for city ID ${cityId}.`;
    list.innerHTML = '<div class="empty-state glass-card"><span class="spinner"></span></div>';
    panel.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    try {
        const histories = await apiRequest(`/cityhistory/city/${cityId}?cityName=${encodeURIComponent(cityName)}`);
        meta.textContent = `Showing history joined by city ID ${cityId} and city name ${cityName}.`;

        if (!histories || histories.length === 0) {
            list.innerHTML = `
                <div class="empty-state glass-card">
                    <div class="empty-icon">HS</div>
                    <p>No history found for ${cityName}.</p>
                </div>
            `;
            return;
        }

        list.innerHTML = histories.map(history => `
            <article class="city-history-item">
                <h3>${escapeHtml(history.title || "Untitled history")}</h3>
                <p>${escapeHtml(history.content || "No history content available.")}</p>
            </article>
        `).join("");
    } catch {
        meta.textContent = `Could not load history for city ID ${cityId}.`;
        list.innerHTML = `
            <div class="empty-state glass-card">
                <div class="empty-icon">ER</div>
                <p>Failed to load city history.</p>
            </div>
        `;
    }
}

function closeCityHistory() {
    const panel = document.getElementById("cityHistoryPanel");
    const title = document.getElementById("cityHistoryTitle");
    const meta = document.getElementById("cityHistoryMeta");
    const list = document.getElementById("cityHistoryList");

    cityHistoryState.cityId = null;
    cityHistoryState.cityName = "";
    document.getElementById("cityHistoryForm")?.reset();
    syncCityHistoryAdminUI();

    panel.hidden = true;
    title.textContent = "City History";
    meta.textContent = "Select a city to load its history.";
    list.innerHTML = `
        <div class="empty-state glass-card">
            <div class="empty-icon">HS</div>
            <p>Select a city to view its history</p>
        </div>
    `;
}
