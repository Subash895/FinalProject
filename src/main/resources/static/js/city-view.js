function getCityViewId() {
  return Number(new URLSearchParams(window.location.search).get("cityId"));
}

function escapeViewHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function renderCityHistories(histories) {
  const container = document.getElementById("cityHistoryList");
  const list = Array.isArray(histories) ? histories : [];
  if (!list.length) {
    container.innerHTML = '<div class="empty-state glass-card"><p>No history found for this city.</p></div>';
    return;
  }
  container.innerHTML = list.map(item => `
    <article class="history-item">
      <h3>${escapeViewHtml(item.title || "History")}</h3>
      <p>${escapeViewHtml(item.content || "")}</p>
    </article>
  `).join("");
}

function renderCityGallery(cityId, images) {
  const container = document.getElementById("cityGalleryList");
  const list = Array.isArray(images) ? images : [];
  if (!list.length) {
    container.innerHTML = '<div class="empty-state glass-card"><p>No gallery images yet.</p></div>';
    return;
  }

  const canManage = isAdmin();
  container.innerHTML = list.map(image => `
    <article class="city-gallery-item">
      <img src="${escapeViewHtml(image.imageUrl || "")}" alt="City gallery image">
      ${canManage ? `
        <div class="card-actions">
          <button type="button" class="btn btn-delete btn-sm" onclick="deleteCityGalleryImage(${cityId}, ${image.id})">Delete</button>
        </div>
      ` : ""}
    </article>
  `).join("");
}

function triggerCityGalleryPicker() {
  document.getElementById("cityGalleryInput")?.click();
}

async function handleCityGallerySelected(event) {
  const cityId = getCityViewId();
  const file = event?.target?.files?.[0];
  if (!cityId || !file) {
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
    const response = await fetch(`${API_BASE}/cities/${cityId}/gallery`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData
    });
    if (!response.ok) {
      const message = await response.text().catch(() => "Failed to upload image.");
      throw new Error(message || "Failed to upload image.");
    }
    showToast("Image added to city gallery.", "success");
    await loadCityView();
  } catch (error) {
    showToast(error.message || "Failed to upload image.", "error");
  } finally {
    event.target.value = "";
  }
}

async function deleteCityGalleryImage(cityId, imageId) {
  openDeleteModal({
    itemName: "this gallery image",
    onConfirm: async () => {
      await apiRequest(`/cities/${cityId}/gallery/${imageId}`, "DELETE");
      await loadCityView();
    }
  });
}

async function loadCityView() {
  const cityId = getCityViewId();
  if (!cityId) {
    throw new Error("City id is required.");
  }
  const city = await apiRequest(`/cities/${cityId}`);
  const gallery = await apiRequest(`/cities/${cityId}/gallery`).catch(() => []);
  const histories = await apiRequest(`/cityhistory/city/${cityId}?cityName=${encodeURIComponent(city.name || "")}`).catch(() => []);

  document.getElementById("cityTitle").textContent = city.name || "City";
  document.getElementById("cityMeta").textContent = [city.state, city.country].filter(Boolean).join(", ") || "City details";
  document.getElementById("cityOverview").textContent = `Explore ${city.name || "this city"} and its local history, places, and community highlights.`;

  const image = document.getElementById("cityImage");
  if (city.imageUrl) {
    image.src = city.imageUrl;
    image.style.display = "block";
  } else {
    image.style.display = "none";
  }

  const galleryAdmin = document.getElementById("cityGalleryAdmin");
  if (galleryAdmin) {
    galleryAdmin.style.display = isAdmin() ? "flex" : "none";
  }
  renderCityGallery(cityId, gallery);
  renderCityHistories(histories);
}

document.addEventListener("DOMContentLoaded", async () => {
  applyRoleUI();
  try {
    await loadCityView();
  } catch (error) {
    showToast(error.message || "Failed to load city details.", "error");
  }
});
