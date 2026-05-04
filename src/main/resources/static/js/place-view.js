function getPlaceViewId() {
  return Number(new URLSearchParams(window.location.search).get("placeId"));
}

async function loadPlaceView() {
  const placeId = getPlaceViewId();
  if (!placeId) {
    throw new Error("Place id is required.");
  }
  const place = await apiRequest(`/places/${placeId}`);

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
}

document.addEventListener("DOMContentLoaded", async () => {
  applyRoleUI();
  try {
    await loadPlaceView();
  } catch (error) {
    showToast(error.message || "Failed to load place details.", "error");
  }
});

