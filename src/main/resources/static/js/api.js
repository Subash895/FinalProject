/**
 * Shared frontend API helpers for authenticated requests, error handling, and common UI feedback.
 */
/* ============================================================
   SMART CITY â€” api.js  (Shared API utility)
   ============================================================ */

const API_BASE =
    window.SMARTCITY_API_BASE ||
    document.querySelector('meta[name="smartcity-api-base"]')?.content ||
    localStorage.getItem("smartcity.apiBase") ||
    (window.location.origin && window.location.origin !== "null" ?
        `${window.location.origin}/api` :
        "http://localhost:8080/api");

async function apiRequest(endpoint, method = "GET", data = null) {
    const options = {
        method,
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        }
    };

    const token = localStorage.getItem("token");
    if (token) options.headers["Authorization"] = "Bearer " + token;

    if (data) options.body = JSON.stringify(data);

    const res = await fetch(API_BASE + endpoint, options);

    if (!res.ok) {
        let errorMessage = `HTTP ${res.status}: ${res.statusText}`;
        const contentType = res.headers.get("content-type") || "";

        if (contentType.includes("application/json")) {
            try {
                const payload = await res.json();
                errorMessage = payload?.message || payload?.error || errorMessage;
            } catch {}
        } else {
            try {
                const text = await res.text();
                if (text) errorMessage = text;
            } catch {}
        }

        throw new Error(errorMessage);
    }

    // DELETE often returns 204 No Content â€” don't try to parse JSON
    if (res.status === 204 || res.headers.get("content-length") === "0") return null;

    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) return res.json();

    return null;
}

function skeletonLine(width = "100%", extraClass = "") {
    return `<span class="skeleton-line ${extraClass}" style="width:${width}"></span>`;
}

function skeletonCircle(size = "48px") {
    return `<span class="skeleton-circle" style="width:${size};height:${size}"></span>`;
}

function skeletonListMarkup(type = "card", count = 4) {
    const items = Array.from({ length: count }, (_, index) => {
        const delay = `style="animation-delay:${index * 0.04}s"`;

        if (type === "city") {
            return `
                <div class="skeleton-card skeleton-card-row glass-card" ${delay}>
                    ${skeletonCircle("56px")}
                    <div class="skeleton-stack">
                        ${skeletonLine("42%", "skeleton-line-title")}
                        ${skeletonLine("62%")}
                        ${skeletonLine("34%")}
                        <div class="skeleton-actions">
                            ${skeletonLine("72px", "skeleton-pill")}
                            ${skeletonLine("64px", "skeleton-pill")}
                        </div>
                    </div>
                </div>
            `;
        }

        if (type === "forum" || type === "news") {
            return `
                <div class="skeleton-card glass-card" ${delay}>
                    <div class="skeleton-card-head">
                        ${skeletonLine("55%", "skeleton-line-title")}
                        ${skeletonLine("86px", "skeleton-pill")}
                    </div>
                    ${skeletonLine("92%")}
                    ${skeletonLine("78%")}
                    ${skeletonLine("44%")}
                </div>
            `;
        }

        if (type === "subscription") {
            return `
                <div class="skeleton-card skeleton-card-row glass-card" ${delay}>
                    <div class="skeleton-stack">
                        ${skeletonLine("48%", "skeleton-line-title")}
                        ${skeletonLine("32%")}
                    </div>
                    ${skeletonLine("92px", "skeleton-pill")}
                </div>
            `;
        }

        return `
            <div class="skeleton-card glass-card" ${delay}>
                <div class="skeleton-card-head">
                    ${skeletonCircle("46px")}
                    <div class="skeleton-stack">
                        ${skeletonLine("52%", "skeleton-line-title")}
                        ${skeletonLine("72%")}
                    </div>
                </div>
                ${skeletonLine("88%")}
                ${skeletonLine("64%")}
                <div class="skeleton-actions">
                    ${skeletonLine("70px", "skeleton-pill")}
                    ${skeletonLine("70px", "skeleton-pill")}
                </div>
            </div>
        `;
    });

    return `<div class="skeleton-list" aria-label="Loading content">${items.join("")}</div>`;
}

function setListSkeleton(container, type = "card", count = 4) {
    if (!container) {
        return;
    }

    container.classList.add("is-loading");
    container.innerHTML = skeletonListMarkup(type, count);
}

function clearListSkeleton(container) {
    container?.classList.remove("is-loading");
}
