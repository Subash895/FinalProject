const manageState = {
  businessId: null,
  business: null
};

function qs(name) {
  return new URLSearchParams(window.location.search).get(name);
}

function renderVacancies(list) {
  const container = document.getElementById("vacancyList");
  const vacancies = Array.isArray(list) ? list : [];
  if (!vacancies.length) {
    container.innerHTML = '<div class="empty-state glass-card"><p>No vacancies posted yet.</p></div>';
    return;
  }
  container.innerHTML = vacancies.map(v => `
    <article class="vacancy-item">
      <h3>${v.title || "Vacancy"}</h3>
      <p>${v.description || ""}</p>
      <p><strong>Location:</strong> ${v.location || "-"}</p>
      <p><strong>Salary:</strong> ${v.salaryInfo || "-"}</p>
      <p><strong>Contact:</strong> ${v.contactEmail || "-"}</p>
      <p><strong>Requirements:</strong> ${v.requirements || "-"}</p>
      <div class="card-actions">
        <button class="btn btn-edit btn-sm" onclick='editVacancy(${v.id}, ${JSON.stringify(v)})'>Edit</button>
        <button class="btn btn-delete btn-sm" onclick="deleteVacancy(${v.id})">Delete</button>
      </div>
    </article>
  `).join("");
}

function renderGalleryImages(images) {
  const container = document.getElementById("manageBusinessGalleryList");
  const gallery = Array.isArray(images) ? images : [];
  if (!gallery.length) {
    container.innerHTML = '<div class="empty-state glass-card"><p>No gallery images yet.</p></div>';
    return;
  }
  container.innerHTML = gallery.map(image => `
    <article class="manage-gallery-item">
      <img src="${image.imageUrl}" alt="Gallery image">
      <div class="card-actions">
        <button type="button" class="btn btn-delete btn-sm" onclick="deleteGalleryImage(${image.id})">Delete</button>
      </div>
    </article>
  `).join("");
}

async function loadManageBusiness() {
  const businessId = await resolveManageBusinessId();
  if (!businessId) {
    throw new Error("Business id is required.");
  }
  manageState.businessId = businessId;
  manageState.business = await apiRequest(`/businesses/${businessId}`);
  const b = manageState.business;
  document.getElementById("manageBusinessName").textContent = b.name || "Business";
  document.getElementById("manageBusinessNameInput").value = b.name || "";
  document.getElementById("manageBusinessAddressInput").value = b.address || "";
  document.getElementById("manageBusinessDescriptionInput").value = b.description || "";
  const img = document.getElementById("manageBusinessImagePreview");
  if (b.imageUrl) {
    img.src = b.imageUrl;
    img.style.display = "block";
  } else {
    img.style.display = "none";
  }
  const vacancies = await apiRequest(`/businesses/${businessId}/vacancies`);
  renderVacancies(vacancies);
  const gallery = await apiRequest(`/businesses/${businessId}/gallery`).catch(() => []);
  renderGalleryImages(gallery);
}

async function resolveManageBusinessId() {
  const requestedId = Number(qs("businessId"));
  if (requestedId) {
    return requestedId;
  }

  if (!isBusiness()) {
    throw new Error("Business account required.");
  }

  const businesses = await apiRequest("/businesses");
  if (!Array.isArray(businesses) || !businesses.length) {
    throw new Error("No business found for this account. Please add one first.");
  }

  const firstBusinessId = businesses[0]?.id;
  if (!firstBusinessId) {
    throw new Error("Business id is missing.");
  }

  const nextUrl = `business-manage.html?businessId=${encodeURIComponent(firstBusinessId)}`;
  if (window.location.search !== `?businessId=${encodeURIComponent(firstBusinessId)}`) {
    window.history.replaceState({}, "", nextUrl);
  }
  return Number(firstBusinessId);
}

async function saveBusinessProfile(event) {
  event.preventDefault();
  await apiRequest(`/businesses/${manageState.businessId}`, "PUT", {
    name: document.getElementById("manageBusinessNameInput").value.trim(),
    address: document.getElementById("manageBusinessAddressInput").value.trim(),
    description: document.getElementById("manageBusinessDescriptionInput").value.trim()
  });
  showToast("Business profile updated.", "success");
  await loadManageBusiness();
}

async function uploadBusinessImage(event) {
  event.preventDefault();
  const file = document.getElementById("manageBusinessImageInput")?.files?.[0];
  if (!file || !file.type.startsWith("image/")) {
    showToast("Select an image file.", "error");
    return;
  }
  const formData = new FormData();
  formData.append("photo", file);
  const token = localStorage.getItem("token");
  const response = await fetch(`${API_BASE}/businesses/${manageState.businessId}/photo`, {
    method: "PUT",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData
  });
  if (!response.ok) {
    const msg = await response.text().catch(() => "Failed to upload business image.");
    throw new Error(msg || "Failed to upload business image.");
  }
  showToast("Business image updated.", "success");
  document.getElementById("manageBusinessImageInput").value = "";
  await loadManageBusiness();
}

async function createVacancy(event) {
  event.preventDefault();
  await apiRequest(`/businesses/${manageState.businessId}/vacancies`, "POST", {
    title: document.getElementById("vacancyTitle").value.trim(),
    description: document.getElementById("vacancyDescription").value.trim(),
    location: document.getElementById("vacancyLocation").value.trim(),
    requirements: document.getElementById("vacancyRequirements").value.trim(),
    contactEmail: document.getElementById("vacancyContact").value.trim(),
    salaryInfo: document.getElementById("vacancySalary").value.trim(),
    active: true
  });
  showToast("Vacancy posted.", "success");
  event.target.reset();
  await loadManageBusiness();
}

async function uploadGalleryImage(event) {
  event.preventDefault();
  const file = document.getElementById("manageBusinessGalleryInput")?.files?.[0];
  if (!file || !file.type.startsWith("image/")) {
    showToast("Select an image file.", "error");
    return;
  }
  const formData = new FormData();
  formData.append("photo", file);
  const token = localStorage.getItem("token");
  const response = await fetch(`${API_BASE}/businesses/${manageState.businessId}/gallery`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData
  });
  if (!response.ok) {
    const msg = await response.text().catch(() => "Failed to upload gallery image.");
    throw new Error(msg || "Failed to upload gallery image.");
  }
  showToast("Gallery image added.", "success");
  document.getElementById("manageBusinessGalleryInput").value = "";
  await loadManageBusiness();
}

function deleteGalleryImage(imageId) {
  openDeleteModal({
    itemName: "this gallery image",
    onConfirm: async () => {
      await apiRequest(`/businesses/${manageState.businessId}/gallery/${imageId}`, "DELETE");
      await loadManageBusiness();
    }
  });
}

function editVacancy(id, vacancy) {
  openEditModal({
    title: "Edit Vacancy",
    fields: [
      { key: "title", label: "Title" },
      { key: "description", label: "Description" },
      { key: "location", label: "Location" },
      { key: "requirements", label: "Requirements" },
      { key: "contactEmail", label: "Contact Email" },
      { key: "salaryInfo", label: "Salary" }
    ],
    values: vacancy,
    onSave: async (data) => {
      await apiRequest(`/businesses/${manageState.businessId}/vacancies/${id}`, "PUT", {
        ...vacancy,
        ...data,
        active: vacancy.active !== false
      });
      await loadManageBusiness();
    }
  });
}

function deleteVacancy(id) {
  openDeleteModal({
    itemName: "this vacancy",
    onConfirm: async () => {
      await apiRequest(`/businesses/${manageState.businessId}/vacancies/${id}`, "DELETE");
      await loadManageBusiness();
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  if (!isLoggedIn()) {
    window.location.href = "login.html";
    return;
  }
  applyRoleUI();
  document.getElementById("businessProfileForm").addEventListener("submit", saveBusinessProfile);
  document.getElementById("businessImageForm").addEventListener("submit", uploadBusinessImage);
  document.getElementById("businessGalleryForm").addEventListener("submit", uploadGalleryImage);
  document.getElementById("vacancyForm").addEventListener("submit", createVacancy);
  try {
    await loadManageBusiness();
  } catch (error) {
    showToast(error.message || "Failed to load business manager.", "error");
  }
});
