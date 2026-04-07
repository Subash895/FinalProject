const REVIEW_TARGETS = {
    business: "BUSINESS",
    place: "PLACE",
    city: "CITY",
    news: "NEWS"
};

async function loadReviews(targetType, targetId) {
    return apiRequest(`/reviews?targetType=${encodeURIComponent(targetType)}&targetId=${encodeURIComponent(targetId)}`);
}

async function submitReview(targetType, targetId, payload) {
    return apiRequest("/reviews", "POST", {
        targetType,
        targetId,
        rating: Number(payload.rating),
        comment: String(payload.comment || "").trim()
    });
}

async function updateReview(reviewId, payload) {
    return apiRequest(`/reviews/${reviewId}`, "PUT", payload);
}

async function removeReview(reviewId) {
    return apiRequest(`/reviews/${reviewId}`, "DELETE");
}

function reviewStars(rating) {
    const safeRating = Math.max(0, Math.min(5, Number(rating) || 0));
    return "★★★★★".slice(0, safeRating) + "☆☆☆☆☆".slice(0, 5 - safeRating);
}

function formatReviewDate(value) {
    if (!value) {
        return "Recently";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "Recently";
    }
    return date.toLocaleString();
}

function canSubmitPublicReview() {
    return isLoggedIn();
}

function canManageReview(review) {
    return isAdmin() || review.user?.id === getUserId();
}

function escapeHtml(value) {
    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function decodeHtml(value) {
    const textarea = document.createElement("textarea");
    textarea.innerHTML = value;
    return textarea.value;
}

function reviewComposer(targetType, targetId) {
    if (!canSubmitPublicReview()) {
        return `<div class="review-note">Login to add a review.</div>`;
    }

    return `
        <div class="review-composer">
            <button
                type="button"
                class="review-toggle"
                data-review-open
                aria-label="Write review"
                title="Write review">
                ★
            </button>
            <form class="review-form review-form-hidden" data-review-form data-target-type="${targetType}" data-target-id="${targetId}">
                <div class="review-form-grid">
                    <label class="review-field">
                        <span>Rating</span>
                        <select class="form-control" data-review-rating required>
                            <option value="5">5 - Excellent</option>
                            <option value="4">4 - Good</option>
                            <option value="3">3 - Average</option>
                            <option value="2">2 - Poor</option>
                            <option value="1">1 - Bad</option>
                        </select>
                    </label>
                    <label class="review-field review-field-wide">
                        <span>Comment</span>
                        <textarea class="form-control review-inline-textarea" data-review-comment placeholder="Share your experience" required></textarea>
                    </label>
                </div>
                <div class="review-form-actions">
                    <button type="button" class="btn btn-secondary btn-sm" data-review-cancel>Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Submit Review</button>
                </div>
            </form>
        </div>
    `;
}

function renderReviewItem(review) {
    return `
        <article class="review-item">
            <div class="review-item-head">
                <div>
                    <strong>${escapeHtml(review.user?.name || "User")}</strong>
                    <div class="review-stars">${reviewStars(review.rating)} <span>${review.rating}/5</span></div>
                </div>
                <time>${formatReviewDate(review.updatedAt || review.createdAt)}</time>
            </div>
            <p>${escapeHtml(review.comment || "")}</p>
            ${canManageReview(review) ? `
                <div class="review-actions">
                    <button
                        type="button"
                        class="btn btn-edit btn-sm"
                        data-review-edit
                        data-review-id="${review.id}"
                        data-review-target-type="${review.targetType}"
                        data-review-target-id="${review.targetId}"
                        data-review-rating="${review.rating}"
                        data-review-comment="${escapeHtml(review.comment || "")}">
                        Edit
                    </button>
                    <button
                        type="button"
                        class="btn btn-delete btn-sm"
                        data-review-delete
                        data-review-id="${review.id}">
                        Delete
                    </button>
                </div>
            ` : ""}
        </article>
    `;
}

function renderReviewSection(targetType, itemId, reviews) {
    const list = Array.isArray(reviews) ? reviews : [];
    const average = list.length
        ? (list.reduce((sum, review) => sum + (review.rating || 0), 0) / list.length).toFixed(1)
        : null;

    return `
        <section class="review-section">
            <div class="review-summary">
                <div>
                    <h4>User Reviews</h4>
                    <p>${list.length ? `${list.length} review(s) • Average ${average}/5` : "No reviews yet."}</p>
                </div>
                <div class="review-summary-side">
                    ${average ? `<div class="review-average">${average}<span>/5</span></div>` : ""}
                    ${reviewComposer(targetType, itemId)}
                </div>
            </div>
            <div class="review-list">
                ${list.length ? list.map(renderReviewItem).join("") : `<div class="review-empty">Be the first user to write a review.</div>`}
            </div>
        </section>
    `;
}

async function hydrateReviewForms(onSaved) {
    const openButtons = Array.from(document.querySelectorAll("[data-review-open]"));
    openButtons.forEach(button => {
        button.addEventListener("click", () => {
            const composer = button.closest(".review-composer");
            const form = composer?.querySelector("[data-review-form]");
            if (!composer || !form) {
                return;
            }

            composer.classList.add("review-composer-open");
            form.classList.remove("review-form-hidden");
            button.classList.add("review-toggle-hidden");
            form.querySelector("[data-review-comment]")?.focus();
        });
    });

    const cancelButtons = Array.from(document.querySelectorAll("[data-review-cancel]"));
    cancelButtons.forEach(button => {
        button.addEventListener("click", () => {
            const form = button.closest("[data-review-form]");
            const composer = button.closest(".review-composer");
            const toggle = composer?.querySelector("[data-review-open]");
            if (!form || !composer || !toggle) {
                return;
            }

            form.classList.add("review-form-hidden");
            form.reset();
            composer.classList.remove("review-composer-open");
            toggle.classList.remove("review-toggle-hidden");
        });
    });

    const forms = Array.from(document.querySelectorAll("[data-review-form]"));
    forms.forEach(form => {
        form.addEventListener("submit", async event => {
            event.preventDefault();
            const button = form.querySelector("button[type=submit]");
            const originalLabel = button.textContent;
            button.disabled = true;
            button.textContent = "Saving...";

            try {
                await submitReview(form.dataset.targetType, Number(form.dataset.targetId), {
                    rating: form.querySelector("[data-review-rating]")?.value,
                    comment: form.querySelector("[data-review-comment]")?.value
                });
                showToast("Review saved.", "success");
                await onSaved();
            } catch (error) {
                console.error(error);
                showToast("Failed to save review.", "error");
            } finally {
                button.disabled = false;
                button.textContent = originalLabel;
            }
        });
    });

    const editButtons = Array.from(document.querySelectorAll("[data-review-edit]"));
    editButtons.forEach(button => {
        button.addEventListener("click", () => {
            openEditModal({
                title: "Edit Review",
                fields: [
                    { key: "rating", label: "Rating", placeholder: "1 to 5", type: "number" },
                    { key: "comment", label: "Comment", placeholder: "Update your comment" }
                ],
                values: {
                    rating: button.dataset.reviewRating,
                    comment: decodeHtml(button.dataset.reviewComment || "")
                },
                onSave: async (data) => {
                    await updateReview(Number(button.dataset.reviewId), {
                        targetType: button.dataset.reviewTargetType,
                        targetId: Number(button.dataset.reviewTargetId),
                        rating: Number(data.rating),
                        comment: data.comment
                    });
                    await onSaved();
                }
            });
        });
    });

    const deleteButtons = Array.from(document.querySelectorAll("[data-review-delete]"));
    deleteButtons.forEach(button => {
        button.addEventListener("click", () => {
            openDeleteModal({
                itemName: "this review",
                onConfirm: async () => {
                    await removeReview(Number(button.dataset.reviewId));
                    await onSaved();
                }
            });
        });
    });
}
