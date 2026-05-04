/**
 * OpenStreetMap helpers used to search locations and render map markers without the Google Maps dependency.
 */
const OSM_DEFAULT_CENTER = [20.5937, 78.9629];
const OSM_DEFAULT_ZOOM = 5;
const OSM_GEOCODE_DELAY_MS = 1100;
const USER_LOCATION_STORAGE_KEY = "smartcity.user.location";
const osmGeocodeCache = new Map();
const osmSearchCache = new Map();
const osmReverseCache = new Map();

let osmGeocodeQueue = Promise.resolve();
let osmLastGeocodeAt = 0;

function createOpenStreetMap(elementId, options = {}) {
    if (!window.L) {
        throw new Error("Leaflet is not available.");
    }

    const map = L.map(elementId, {
        zoomControl: true,
        attributionControl: false
    }).setView(options.center || OSM_DEFAULT_CENTER, options.zoom || OSM_DEFAULT_ZOOM);

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19
    }).addTo(map);

    return map;
}

function createUserLocationIcon(label = "Me") {
    return L.divIcon({
        className: "map-user-location-icon",
        html: `
            <div class="map-user-location-badge">
                <span class="map-user-location-dot"></span>
                <span class="map-user-location-label">${String(label || "Me")}</span>
            </div>
        `,
        iconSize: [74, 28],
        iconAnchor: [14, 14],
        popupAnchor: [0, -14]
    });
}

function showUserLocationMarker(map, coords, options = {}) {
    if (!map || !Number.isFinite(coords?.lat) || !Number.isFinite(coords?.lng)) {
        return null;
    }

    const label = options.label || "Me";
    const marker = options.marker || L.marker([coords.lat, coords.lng], {
        title: options.title || "Your location",
        icon: createUserLocationIcon(label),
        zIndexOffset: 1000
    }).addTo(map);

    marker.setLatLng([coords.lat, coords.lng]);
    marker.setIcon(createUserLocationIcon(label));
    marker.options.title = options.title || "Your location";
    marker.bindPopup(options.popupText || label);

    return marker;
}

function addCurrentLocationControl(map, options = {}) {
    if (!map || !window.L?.Control) {
        return null;
    }

    const control = L.control({
        position: options.position || "topright"
    });

    control.onAdd = function onAdd() {
        const container = L.DomUtil.create("div", "leaflet-bar map-location-control");
        const button = L.DomUtil.create("button", "map-location-control-button", container);

        button.type = "button";
        button.title = options.title || "Show my current location";
        button.setAttribute("aria-label", options.title || "Show my current location");
        button.innerHTML = `
            <span class="map-location-control-crosshair" aria-hidden="true"></span>
        `;

        L.DomEvent.disableClickPropagation(container);
        L.DomEvent.disableScrollPropagation(container);
        L.DomEvent.on(button, "click", async event => {
            L.DomEvent.stop(event);
            if (typeof options.onClick === "function") {
                await options.onClick(button);
            }
        });

        return container;
    };

    control.addTo(map);
    return control;
}

function geocodeWithOpenStreetMap(address) {
    const normalizedAddress = String(address || "").trim();
    if (!normalizedAddress) {
        return Promise.resolve(null);
    }

    const cacheKey = normalizedAddress.toLowerCase();
    if (osmGeocodeCache.has(cacheKey)) {
        return Promise.resolve(osmGeocodeCache.get(cacheKey));
    }

    const storageKey = `smartcity.osm.geocode.${cacheKey}`;
    try {
        const cachedValue = localStorage.getItem(storageKey);
        if (cachedValue) {
            const parsed = JSON.parse(cachedValue);
            osmGeocodeCache.set(cacheKey, parsed);
            return Promise.resolve(parsed);
        }
    } catch (error) {
        console.warn("Failed to read cached geocode.", error);
    }

    osmGeocodeQueue = osmGeocodeQueue.then(async () => {
        const waitTime = Math.max(0, OSM_GEOCODE_DELAY_MS - (Date.now() - osmLastGeocodeAt));
        if (waitTime > 0) {
            await new Promise(resolve => setTimeout(resolve, waitTime));
        }

        const params = new URLSearchParams({
            q: normalizedAddress,
            format: "jsonv2",
            limit: "1"
        });

        const response = await fetch(`https://nominatim.openstreetmap.org/search?${params.toString()}`, {
            headers: {
                "Accept": "application/json"
            }
        });

        osmLastGeocodeAt = Date.now();

        if (!response.ok) {
            throw new Error(`Nominatim geocoding failed: ${response.status}`);
        }

        const results = await response.json();
        const match = Array.isArray(results) ? results[0] : null;
        const coordinates = match ? {
            lat: Number(match.lat),
            lng: Number(match.lon),
            label: match.display_name
        } : null;

        osmGeocodeCache.set(cacheKey, coordinates);
        try {
            localStorage.setItem(storageKey, JSON.stringify(coordinates));
        } catch (error) {
            console.warn("Failed to cache geocode.", error);
        }

        return coordinates;
    });

    return osmGeocodeQueue;
}

function searchWithOpenStreetMap(query, options = {}) {
    const normalizedQuery = String(query || "").trim();
    if (!normalizedQuery) {
        return Promise.resolve([]);
    }

    const limit = Math.max(1, Math.min(Number(options.limit) || 5, 10));
    const cacheKey = `${normalizedQuery.toLowerCase()}::${limit}`;
    if (osmSearchCache.has(cacheKey)) {
        return Promise.resolve(osmSearchCache.get(cacheKey));
    }

    osmGeocodeQueue = osmGeocodeQueue.then(async () => {
        const waitTime = Math.max(0, OSM_GEOCODE_DELAY_MS - (Date.now() - osmLastGeocodeAt));
        if (waitTime > 0) {
            await new Promise(resolve => setTimeout(resolve, waitTime));
        }

        const params = new URLSearchParams({
            q: normalizedQuery,
            format: "jsonv2",
            addressdetails: "1",
            limit: String(limit)
        });

        const response = await fetch(`https://nominatim.openstreetmap.org/search?${params.toString()}`, {
            headers: {
                "Accept": "application/json"
            }
        });

        osmLastGeocodeAt = Date.now();

        if (!response.ok) {
            throw new Error(`Nominatim search failed: ${response.status}`);
        }

        const results = await response.json();
        const matches = (Array.isArray(results) ? results : []).map(item => ({
            lat: Number(item.lat),
            lng: Number(item.lon),
            displayName: item.display_name,
            name: item.name || item.display_name,
            type: item.type || "",
            className: item.class || "",
            address: item.address || {}
        }));

        osmSearchCache.set(cacheKey, matches);
        return matches;
    });

    return osmGeocodeQueue;
}

function getCurrentBrowserLocation(options = {}) {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error("Geolocation is not supported by this browser."));
            return;
        }

        navigator.geolocation.getCurrentPosition(
            position => {
                resolve({
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                });
            },
            error => {
                if (error.code === error.PERMISSION_DENIED) {
                    reject(new Error("Location access was denied."));
                    return;
                }
                if (error.code === error.POSITION_UNAVAILABLE) {
                    reject(new Error("Current location is unavailable."));
                    return;
                }
                if (error.code === error.TIMEOUT) {
                    reject(new Error("Location request timed out."));
                    return;
                }
                reject(new Error("Failed to get current location."));
            },
            {
                enableHighAccuracy: true,
                timeout: Number(options.timeout) || 10000,
                maximumAge: Number(options.maximumAge) || 0
            }
        );
    });
}

function reverseGeocodeWithOpenStreetMap(lat, lng) {
    const normalizedLat = Number(lat);
    const normalizedLng = Number(lng);
    if (!Number.isFinite(normalizedLat) || !Number.isFinite(normalizedLng)) {
        return Promise.resolve(null);
    }

    const cacheKey = `${normalizedLat.toFixed(6)},${normalizedLng.toFixed(6)}`;
    if (osmReverseCache.has(cacheKey)) {
        return Promise.resolve(osmReverseCache.get(cacheKey));
    }

    osmGeocodeQueue = osmGeocodeQueue.then(async () => {
        const waitTime = Math.max(0, OSM_GEOCODE_DELAY_MS - (Date.now() - osmLastGeocodeAt));
        if (waitTime > 0) {
            await new Promise(resolve => setTimeout(resolve, waitTime));
        }

        const params = new URLSearchParams({
            lat: String(normalizedLat),
            lon: String(normalizedLng),
            format: "jsonv2",
            addressdetails: "1"
        });

        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?${params.toString()}`, {
            headers: {
                "Accept": "application/json"
            }
        });

        osmLastGeocodeAt = Date.now();

        if (!response.ok) {
            throw new Error(`Nominatim reverse geocoding failed: ${response.status}`);
        }

        const result = await response.json();
        const match = result ? {
            lat: Number(result.lat ?? normalizedLat),
            lng: Number(result.lon ?? normalizedLng),
            displayName: result.display_name || "",
            name: result.name || result.display_name || "",
            address: result.address || {}
        } : null;

        osmReverseCache.set(cacheKey, match);
        return match;
    });

    return osmGeocodeQueue;
}

function getSavedUserLocation() {
    try {
        const raw = sessionStorage.getItem(USER_LOCATION_STORAGE_KEY) || localStorage.getItem(USER_LOCATION_STORAGE_KEY);
        if (!raw) {
            return null;
        }

        const parsed = JSON.parse(raw);
        if (!Number.isFinite(parsed?.lat) || !Number.isFinite(parsed?.lng)) {
            return null;
        }

        return {
            lat: Number(parsed.lat),
            lng: Number(parsed.lng)
        };
    } catch (error) {
        console.warn("Failed to read saved user location.", error);
        return null;
    }
}

function saveUserLocation(coords) {
    if (!Number.isFinite(coords?.lat) || !Number.isFinite(coords?.lng)) {
        return;
    }

    const payload = JSON.stringify({
        lat: Number(coords.lat),
        lng: Number(coords.lng)
    });

    try {
        sessionStorage.setItem(USER_LOCATION_STORAGE_KEY, payload);
        localStorage.setItem(USER_LOCATION_STORAGE_KEY, payload);
    } catch (error) {
        console.warn("Failed to save user location.", error);
    }
}

async function getUserLocation(options = {}) {
    if (!options.forceRefresh) {
        const saved = getSavedUserLocation();
        if (saved) {
            return saved;
        }
    }

    const coords = await getCurrentBrowserLocation(options);
    saveUserLocation(coords);
    return coords;
}

function haversineDistanceKm(from, to) {
    if (!Number.isFinite(from?.lat) || !Number.isFinite(from?.lng) || !Number.isFinite(to?.lat) || !Number.isFinite(to?.lng)) {
        return null;
    }

    const toRadians = value => (value * Math.PI) / 180;
    const earthRadiusKm = 6371;
    const latDelta = toRadians(to.lat - from.lat);
    const lngDelta = toRadians(to.lng - from.lng);
    const startLat = toRadians(from.lat);
    const endLat = toRadians(to.lat);

    const a = Math.sin(latDelta / 2) ** 2 +
        Math.cos(startLat) * Math.cos(endLat) * Math.sin(lngDelta / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadiusKm * c;
}

function formatDistanceKm(distanceKm) {
    if (!Number.isFinite(distanceKm)) {
        return "Distance unavailable";
    }

    if (distanceKm < 1) {
        return `${Math.round(distanceKm * 1000)} m away`;
    }

    if (distanceKm < 10) {
        return `${distanceKm.toFixed(1)} km away`;
    }

    return `${Math.round(distanceKm)} km away`;
}
