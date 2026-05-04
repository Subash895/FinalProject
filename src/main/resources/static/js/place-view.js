function getPlaceViewId() {
  return Number(new URLSearchParams(window.location.search).get("placeId"));
}

function escapePlaceViewHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function renderPlaceGallery(placeId, images) {
  const container = document.getElementById("placeGalleryList");
  const list = Array.isArray(images) ? images : [];
  if (!list.length) {
    container.innerHTML = '<div class="empty-state glass-card"><p>No gallery images yet.</p></div>';
    return;
  }

  const canManage = isAdmin();
  container.innerHTML = list.map(image => `
    <article class="place-gallery-item">
      <img src="${escapePlaceViewHtml(image.imageUrl || "")}" alt="Place gallery image">
      ${canManage ? `
        <div class="card-actions">
          <button type="button" class="btn btn-delete btn-sm" onclick="deletePlaceGalleryImage(${placeId}, ${image.id})">Delete</button>
        </div>
      ` : ""}
    </article>
  `).join("");
}

function triggerPlaceGalleryPicker() {
  document.getElementById("placeGalleryInput")?.click();
}

async function handlePlaceGallerySelected(event) {
  const placeId = getPlaceViewId();
  const file = event?.target?.files?.[0];
  if (!placeId || !file) {
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
    const response = await fetch(`${API_BASE}/places/${placeId}/gallery`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData
    });
    if (!response.ok) {
      const message = await response.text().catch(() => "Failed to upload image.");
      throw new Error(message || "Failed to upload image.");
    }
    showToast("Image added to place gallery.", "success");
    await loadPlaceView();
  } catch (error) {
    showToast(error.message || "Failed to upload image.", "error");
  } finally {
    event.target.value = "";
  }
}

async function deletePlaceGalleryImage(placeId, imageId) {
  openDeleteModal({
    itemName: "this gallery image",
    onConfirm: async () => {
      await apiRequest(`/places/${placeId}/gallery/${imageId}`, "DELETE");
      await loadPlaceView();
    }
  });
}

async function loadPlaceView() {
  const placeId = getPlaceViewId();
  if (!placeId) {
    throw new Error("Place id is required.");
  }
  const place = await apiRequest(`/places/${placeId}`);
  const gallery = await apiRequest(`/places/${placeId}/gallery`).catch(() => []);

  document.getElementById("placeTitle").textContent = place.name || "Place";
  document.getElementById("placeMeta").textContent = [
    place.category,
    place.city?.name,
    place.city?.state,
    place.city?.country
  ].filter(Boolean).join(" • ");
  document.getElementById("placeAbout").textContent = place.description || place.location || "No details available.";

  const image = document.getElementById("placeImage");
  if (place.imageUrl) {
    image.src = place.imageUrl;
    image.style.display = "block";
  } else {
    image.style.display = "none";
  }

  const mapLink = document.getElementById("placeMapLink");
  if (place.latitude != null && place.longitude != null) {
    mapLink.innerHTML = `<a href="https://www.openstreetmap.org/?mlat=${place.latitude}&mlon=${place.longitude}#map=16/${place.latitude}/${place.longitude}" target="_blank" rel="noopener noreferrer">Open this place in OpenStreetMap</a>`;
  } else {
    mapLink.innerHTML = "";
  }

  const galleryAdmin = document.getElementById("placeGalleryAdmin");
  if (galleryAdmin) {
    galleryAdmin.style.display = isAdmin() ? "flex" : "none";
  }
  renderPlaceGallery(placeId, gallery);
}

document.addEventListener("DOMContentLoaded", async () => {
  applyRoleUI();
  try {
    await loadPlaceView();
  } catch (error) {
    showToast(error.message || "Failed to load place details.", "error");
  }
});
