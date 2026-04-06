let googleMapsApiPromise;
let publicConfigPromise;

async function fetchPublicConfig() {
    if (!publicConfigPromise) {
        publicConfigPromise = fetch(`${API_BASE}/config/public`).then(async (response) => {
            if (!response.ok) {
                throw new Error(`Failed to load public config: ${response.status}`);
            }
            return response.json();
        });
    }

    return publicConfigPromise;
}

async function loadGoogleMapsApi({ libraries = [] } = {}) {
    if (window.google?.maps) {
        return window.google.maps;
    }

    if (!googleMapsApiPromise) {
        googleMapsApiPromise = (async () => {
            const config = await fetchPublicConfig();
            const apiKey = config.googleMapsApiKey;

            if (!apiKey) {
                throw new Error("Google Maps API key is not configured.");
            }

            await new Promise((resolve, reject) => {
                const callbackName = `smartCityMapsInit_${Date.now()}`;
                const params = new URLSearchParams({
                    key: apiKey,
                    loading: "async",
                    callback: callbackName
                });

                if (libraries.length > 0) {
                    params.set("libraries", libraries.join(","));
                }

                window[callbackName] = () => {
                    delete window[callbackName];
                    resolve();
                };

                const script = document.createElement("script");
                script.src = `https://maps.googleapis.com/maps/api/js?${params.toString()}`;
                script.async = true;
                script.defer = true;
                script.onerror = () => {
                    delete window[callbackName];
                    reject(new Error("Failed to load Google Maps API."));
                };
                document.head.appendChild(script);
            });

            return window.google.maps;
        })();
    }

    return googleMapsApiPromise;
}
