function getBusinessIdFromQuery() {
  return Number(new URLSearchParams(window.location.search).get("businessId"));
}

function escapeBusinessHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function renderGallery(mainImageUrl, galleryImages) {
  const grid = document.getElementById("galleryGrid");
  const extraImages = Array.isArray(galleryImages) ? galleryImages : [];
  const allUrls = [mainImageUrl, ...extraImages.map(image => image.imageUrl)].filter(Boolean);

  if (!allUrls.length) {
    grid.innerHTML = '<div class="empty-state glass-card"><p>No images yet.</p></div>';
    return;
  }

  grid.innerHTML = allUrls.map((url, index) => `
    <article class="gallery-card">
      <img src="${url}" alt="Business image ${index + 1}">
    </article>
  `).join("");
}

function vacancyApplyAction(vacancy) {
  if (vacancy.contactEmail) {
    const subject = encodeURIComponent(`Job Application - ${vacancy.title || "Vacancy"}`);
    const body = encodeURIComponent("Hello, I would like to apply for this job.");
    return `<a class="btn btn-primary btn-sm" href="mailto:${encodeURIComponent(vacancy.contactEmail)}?subject=${subject}&body=${body}">Apply Job</a>`;
  }
  return '<button type="button" class="btn btn-secondary btn-sm" disabled>No Contact Email</button>';
}

function renderVacancies(vacancies) {
  const list = document.getElementById("vacancyPublicList");
  const items = Array.isArray(vacancies) ? vacancies : [];
  if (!items.length) {
    list.innerHTML = '<div class="empty-state glass-card"><p>No active jobs right now.</p></div>';
    return;
  }

  list.innerHTML = items.map(vacancy => `
    <article class="vacancy-card">
      <h3>${escapeBusinessHtml(vacancy.title || "Job Vacancy")}</h3>
      <p>${escapeBusinessHtml(vacancy.description || "")}</p>
      <p><strong>Location:</strong> ${escapeBusinessHtml(vacancy.location || "-")}</p>
      <p><strong>Salary:</strong> ${escapeBusinessHtml(vacancy.salaryInfo || "-")}</p>
      <p><strong>Requirements:</strong> ${escapeBusinessHtml(vacancy.requirements || "-")}</p>
      <div class="card-actions">${vacancyApplyAction(vacancy)}</div>
    </article>
  `).join("");
}

async function loadBusinessView() {
  const businessId = getBusinessIdFromQuery();
  if (!businessId) {
    throw new Error("Business id is required.");
  }

  const business = await apiRequest(`/businesses/${businessId}`);
  const galleries = await apiRequest(`/businesses/${businessId}/gallery`).catch(() => []);
  const vacancies = await apiRequest(`/businesses/${businessId}/vacancies/public`).catch(() => []);

  document.getElementById("businessTitle").textContent = business.name || "Business";
  document.getElementById("businessAddress").textContent = business.address || "-";
  document.getElementById("businessAbout").textContent = business.description || "No description available.";

  const heroImage = document.getElementById("businessHeroImage");
  if (business.imageUrl) {
    heroImage.src = business.imageUrl;
    heroImage.style.display = "block";
  } else {
    heroImage.style.display = "none";
  }

  renderGallery(business.imageUrl, galleries);
  renderVacancies(vacancies);

  const canManage = isLoggedIn() && isBusiness();
  const manageBtn = document.getElementById("manageBusinessBtn");
  if (canManage) {
    manageBtn.style.display = "";
    manageBtn.addEventListener("click", () => {
      window.location.href = `business-manage.html?businessId=${encodeURIComponent(businessId)}`;
    });
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  applyRoleUI();
  try {
    await loadBusinessView();
  } catch (error) {
    showToast(error.message || "Failed to load business page.", "error");
  }
});

