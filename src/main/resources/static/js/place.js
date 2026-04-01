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
    location: ""
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
    loadPlaces();
});

async function createPlace(event) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    button.textContent = "Saving...";

    try {
        await apiRequest("/places", "POST", {
            name: document.getElementById("name").value.trim(),
            category: document.getElementById("category").value.trim(),
            location: document.getElementById("location").value.trim()
        });
        form.reset();
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
        const data = await apiRequest(`/places${getFilterQuery()}`);
        updateResultSummary(data?.length || 0);

        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><div class="empty-icon">PL</div><p>No places match the current filter.</p></div>';
            return;
        }

        container.className = "place-list";
        container.innerHTML = data.map((place, index) => renderPlaceCard(place, index)).join("");
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
            <div class="place-loc">Location: ${place.location || "Not provided"}</div>
            ${place.description ? `<p class="place-desc">${place.description}</p>` : ""}
          </div>
        </div>
        <div class="card-actions">
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
            { key: "location", label: "Location", placeholder: "e.g. MG Road, Bengaluru" }
        ],
        values: {
            name: place.name,
            category: place.category,
            location: place.location
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
