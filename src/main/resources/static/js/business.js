/**
 * Client-side behavior for the business page, including event handling and API calls.
 */
/* ============================================================
   SMART CITY - business.js
   ============================================================ */

function esc(s) {
    return String(s || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function getBusinessSearchQuery() {
    return document.getElementById("businessSearch")?.value.trim() || "";
}

function businessImageMarkup(business) {
    if (business?.imageUrl) {
        return `<img class="business-card-image" src="${business.imageUrl}" alt="${esc(business.name || "Business")}">`;
    }
    return `<div class="business-card-image business-card-image-fallback">BS</div>`;
}

const businessState = {
    userLocation: getSavedUserLocation()
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("businessForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Adding...';
        try {
            await apiRequest("/businesses", "POST", {
                name: document.getElementById("name").value.trim(),
                address: document.getElementById("address").value.trim(),
                description: document.getElementById("description").value.trim()
            });
            e.target.reset();
            showToast("Business added!", "success");
            await loadBusinesses();
        } catch {
            showToast("Failed to add business.", "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = "Add Business";
        }
    });

    document.getElementById("businessCurrentLocationBtn")?.addEventListener("click", fillBusinessAddressFromCurrentLocation);
    document.getElementById("loadBtn")?.addEventListener("click", loadBusinesses);
    document.getElementById("businessSearch")?.addEventListener("input", loadBusinesses);
    loadBusinesses();
});

function setBusinessLocationStatus(message) {
    const status = document.getElementById("businessLocationStatus");
    if (status) {
        status.textContent = message;
    }
}

async function fillBusinessAddressFromCurrentLocation() {
    const button = document.getElementById("businessCurrentLocationBtn");
    const addressInput = document.getElementById("address");
    if (!button || !addressInput) {
        return;
    }

    button.disabled = true;
    setBusinessLocationStatus("Getting your current position...");

    try {
        const coords = await getCurrentBrowserLocation();
        saveUserLocation(coords);
        setBusinessLocationStatus("Resolving address from map data...");
        const place = await reverseGeocodeWithOpenStreetMap(coords.lat, coords.lng);
        const address = place?.displayName || "";

        if (!address) {
            throw new Error("No address match found for your current location.");
        }

        addressInput.value = address;
        setBusinessLocationStatus("Current location address applied.");
    } catch (error) {
        setBusinessLocationStatus(error.message || "Failed to use current location.");
        showToast(error.message || "Failed to use current location.", "error");
    } finally {
        button.disabled = false;
    }
}

async function loadBusinesses() {
    const container = document.getElementById("list");
    setListSkeleton(container, "card", 4);

    try {
        if (!businessState.userLocation) {
            businessState.userLocation = await getUserLocation().catch(() => null);
        }

        const query = getBusinessSearchQuery();
        const endpoint = query ? `/businesses?q=${encodeURIComponent(query)}` : "/businesses";
        const data = await apiRequest(endpoint);
        if (!data || data.length === 0) {
            clearListSkeleton(container);
            container.innerHTML = `<div class="empty-state glass-card"><span class="empty-icon">BS</span><p>${query ? "No businesses match your search." : "No businesses yet."}</p></div>`;
            return;
        }

        const businessesWithReviews = await attachReviewsToItems(data, REVIEW_TARGETS.business);
        const businessesWithDetails = await Promise.all(businessesWithReviews.map(async business => {
            const coordinates = business.address ? await geocodeWithOpenStreetMap(business.address).catch(() => null) : null;
            return {
                ...business,
                distanceKm: businessState.userLocation && coordinates ?
                    haversineDistanceKm(businessState.userLocation, coordinates) :
                    null
            };
        }));

        container.className = "card-list";
        clearListSkeleton(container);
        container.innerHTML = businessesWithDetails.map((b, i) => {
            const canEdit = isAdmin() || isBusiness() || (isBusiness() && b.owner && b.owner.id === getUserId());
            const canDelete = isAdmin();
            const cardClick = ` onclick="handleBusinessCardClick(event, ${b.id})"`;

            return `
      <div class="business-card glass-card business-card-manageable" style="animation-delay:${i * 0.05}s"${cardClick}>
        ${businessImageMarkup(b)}
        <div class="card-tag">${b.owner?.name || "Business"}</div>
        <h3><button class="business-card-title-link" type="button" onclick="openBusinessDetailsPage(${b.id})">${b.name}</button></h3>
        <div class="biz-meta">
          <span><span class="meta-icon">Address</span>${b.address || "-"}</span>
          <span><span class="meta-icon">About</span>${b.description || "-"}</span>
        </div>
        ${b.distanceKm != null ? `<div class="business-distance">${formatDistanceKm(b.distanceKm)}</div>` : ""}
        <div class="card-actions">
          ${canEdit ? `<button class="btn btn-secondary btn-sm" title="Upload" onclick="triggerBusinessImagePicker(${b.id})">Upload</button><input id="businessImageInput_${b.id}" class="business-image-input" type="file" accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.svg,.ico,.tif,.tiff,image/*" onchange="handleBusinessImageSelected(${b.id}, event)">` : ""}
          ${canEdit ? `<button class="btn btn-edit btn-sm" onclick='editBusiness(${b.id}, ${JSON.stringify(b)})'>Edit</button>` : ""}
          ${canDelete ? `<button class="btn btn-delete btn-sm" onclick="deleteBusiness(${b.id}, '${esc(b.name)}')">Delete</button>` : ""}
        </div>
        ${renderReviewSection(REVIEW_TARGETS.business, b.id, b.reviews || [])}
      </div>`;
        }).join("");

        hydrateReviewForms(loadBusinesses);
    } catch {
        clearListSkeleton(container);
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">NA</span><p>Cannot connect to server. Is it running on port 8080?</p></div>';
    }
}

function openBusinessDetailsPage(id) {
    window.location.href = `business-view.html?businessId=${encodeURIComponent(id)}`;
}

function handleBusinessCardClick(event, id) {
    if (!event || !id) {
        return;
    }

    const interactiveArea = event.target.closest("button, a, input, textarea, select, label, form, .review-section, .card-actions");
    if (interactiveArea) {
        return;
    }

    openBusinessDetailsPage(id);
}

function editBusiness(id, b) {
    openEditModal({
        title: "Edit Business",
        fields: [{
                key: "name",
                label: "Business Name",
                placeholder: "e.g. Sunrise Bakery"
            },
            {
                key: "address",
                label: "Address",
                placeholder: "e.g. 12 Main St"
            },
            {
                key: "description",
                label: "Description",
                placeholder: "e.g. Fresh bakery, coffee, and snacks"
            }
        ],
        values: {
            name: b.name,
            address: b.address,
            description: b.description
        },
        onSave: async (data) => {
            await apiRequest(`/businesses/${id}`, "PUT", data);
            await loadBusinesses();
        }
    });
}

function deleteBusiness(id, name) {
    openDeleteModal({
        itemName: name,
        onConfirm: async () => {
            await apiRequest(`/businesses/${id}`, "DELETE");
            await loadBusinesses();
        }
    });
}

function triggerBusinessImagePicker(id) {
    const input = document.getElementById(`businessImageInput_${id}`);
    input?.click();
}

async function handleBusinessImageSelected(id, event) {
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
        const response = await fetch(`${API_BASE}/businesses/${Number(id)}/photo`, {
            method: "PUT",
            headers: token ? { Authorization: `Bearer ${token}` } : {},
            body: formData
        });

        if (!response.ok) {
            let message = "Failed to upload business image.";
            try {
                message = await response.text() || message;
            } catch {}
            throw new Error(message);
        }

        showToast("Business image updated.", "success");
        await loadBusinesses();
    } catch (error) {
        showToast(error.message || "Failed to upload business image.", "error");
    } finally {
        event.target.value = "";
    }
}
