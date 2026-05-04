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

async function loadCityView() {
  const cityId = getCityViewId();
  if (!cityId) {
    throw new Error("City id is required.");
  }
  const city = await apiRequest(`/cities/${cityId}`);
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

