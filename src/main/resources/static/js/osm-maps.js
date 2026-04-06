const OSM_DEFAULT_CENTER = [20.5937, 78.9629];
const OSM_DEFAULT_ZOOM = 5;
const OSM_GEOCODE_DELAY_MS = 1100;
const osmGeocodeCache = new Map();

let osmGeocodeQueue = Promise.resolve();
let osmLastGeocodeAt = 0;

function createOpenStreetMap(elementId, options = {}) {
    if (!window.L) {
        throw new Error("Leaflet is not available.");
    }

    const map = L.map(elementId, {
        zoomControl: true,
        attributionControl: true
    }).setView(options.center || OSM_DEFAULT_CENTER, options.zoom || OSM_DEFAULT_ZOOM);

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    return map;
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
