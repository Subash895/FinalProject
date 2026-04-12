/**
 * Client-side behavior for the subscription page, including event handling and API calls.
 */
const PLAN_BADGE = {
    free: "badge-green",
    pro: "badge-cyan",
    enterprise: "badge-purple",
    pending: "badge-cyan",
    active: "badge-green",
    authenticated: "badge-cyan",
    failed: "badge-red",
    cancelled: "badge-red",
    paused: "badge-purple",
    expired: "badge-purple"
};

function getPlanBadge(type = "") {
    return PLAN_BADGE[String(type).toLowerCase()] || "badge-cyan";
}

function esc(value) {
    return String(value || "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function requireAdminAction() {
    if (isAdmin()) {
        return true;
    }
    showToast("Admin access required.", "error");
    return false;
}

function setPaymentStatus(message, type = "info") {
    const statusNode = document.getElementById("paymentStatusText");
    if (statusNode) {
        statusNode.textContent = message;
        statusNode.dataset.state = type;
    }
}

function requireLoggedInForPayment() {
    if (isLoggedIn()) {
        return true;
    }
    showToast("Please login before starting a paid subscription.", "error");
    window.location.href = "login.html";
    return false;
}

function formatSubscriptionStatus(subscription) {
    return subscription.status || subscription.type || "UNKNOWN";
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("subscriptionForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!requireAdminAction()) {
            return;
        }

        const button = event.target.querySelector("button[type=submit]");
        button.disabled = true;
        button.innerHTML = '<span class="spinner"></span> Activating...';

        try {
            await apiRequest("/subscriptions", "POST", {
                email: document.getElementById("email").value.trim(),
                type: document.getElementById("type").value.trim().toUpperCase()
            });
            event.target.reset();
            showToast("Subscription activated!", "success");
            await loadSubs();
        } catch {
            showToast("Failed to subscribe.", "error");
        } finally {
            button.disabled = false;
            button.innerHTML = "Activate Subscription";
        }
    });

    document.getElementById("loadBtn")?.addEventListener("click", loadSubs);
    document.querySelectorAll("[data-plan-button]").forEach((button) => {
        button.addEventListener("click", () => startPaidCheckout(button.dataset.planType, button));
    });
    if (!isLoggedIn()) {
        setPaymentStatus("Login is required before Razorpay checkout can open.", "info");
    } else {
        setPaymentStatus("Choose a paid plan to open Razorpay checkout.", "info");
    }
    loadSubs();
});

async function loadSubs() {
    const container = document.getElementById("list");
    container.innerHTML = '<div class="empty-state"><span class="spinner"></span></div>';

    try {
        const endpoint = isAdmin() ? "/subscriptions" : (isLoggedIn() ? "/subscriptions/my" : "/subscriptions");
        const data = await apiRequest(endpoint);
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">S</span><p>No subscriptions yet.</p></div>';
            return;
        }

        container.className = "sub-list";
        container.innerHTML = data.map((subscription, index) => `
      <div class="sub-card glass-card" style="animation-delay:${index * 0.04}s">
        <div>
          <div class="sub-email">${subscription.email}</div>
          <div class="sub-type">${subscription.type || "Standard"} Plan â€¢ ${formatSubscriptionStatus(subscription)}</div>
        </div>
        <div class="sub-actions">
          <span class="badge ${getPlanBadge(subscription.status || subscription.type)}">${formatSubscriptionStatus(subscription)}</span>
          ${isAdmin() ? `<button class="btn btn-edit btn-sm" onclick='editSub(${subscription.id}, ${JSON.stringify(subscription)})'>Edit</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-delete btn-sm" onclick="deleteSub(${subscription.id}, '${esc(subscription.email)}')">Delete</button>` : ""}
        </div>
      </div>`).join("");
    } catch {
        container.innerHTML = '<div class="empty-state glass-card"><span class="empty-icon">!</span><p>Cannot connect to server.</p></div>';
    }
}

async function startPaidCheckout(planType, button) {
    if (!requireLoggedInForPayment()) {
        return;
    }
    if (!window.Razorpay) {
        showToast("Razorpay checkout script did not load.", "error");
        setPaymentStatus("Razorpay checkout script is missing.", "error");
        return;
    }

    const originalLabel = button?.innerHTML || "Subscribe";
    if (button) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner"></span> Opening...';
    }

    try {
        const checkout = await apiRequest("/subscriptions/checkout", "POST", {
            type: planType
        });

        const options = {
            key: checkout.keyId,
            order_id: checkout.orderId,
            name: "MySmartCity",
            description: `${checkout.planType} plan purchase`,
            handler: async (response) => {
                try {
                    await apiRequest("/subscriptions/confirm", "POST", {
                        localSubscriptionId: checkout.localSubscriptionId,
                        razorpayOrderId: response.razorpay_order_id,
                        razorpayPaymentId: response.razorpay_payment_id,
                        razorpaySignature: response.razorpay_signature
                    });
                    setPaymentStatus(`${checkout.planType} subscription activated successfully.`, "success");
                    showToast("Subscription activated.", "success");
                    await loadSubs();
                } catch (error) {
                    setPaymentStatus(error.message || "Payment was authorized but local confirmation failed.", "error");
                    showToast(error.message || "Payment confirmation failed.", "error");
                }
            },
            modal: {
                ondismiss: () => {
                    setPaymentStatus("Payment popup was closed before completion.", "info");
                }
            },
            prefill: {
                name: checkout.customerName,
                email: checkout.customerEmail
            },
            theme: {
                color: "#00d4ff"
            }
        };

        const razorpay = new window.Razorpay(options);
        razorpay.on("payment.failed", async (event) => {
            const details = event.error || {};
            try {
                await apiRequest("/subscriptions/failure", "POST", {
                    localSubscriptionId: checkout.localSubscriptionId,
                    razorpayOrderId: checkout.orderId,
                    code: details.code || null,
                    description: details.description || null,
                    source: details.source || null,
                    step: details.step || null,
                    reason: details.reason || null
                });
            } catch {}
            setPaymentStatus(details.description || "Payment failed. Try again with another method.", "error");
            showToast(details.description || "Payment failed.", "error");
            await loadSubs();
        });

        setPaymentStatus("Razorpay checkout opened. Complete the payment authorization.", "info");
        razorpay.open();
    } catch (error) {
        setPaymentStatus(error.message || "Unable to start Razorpay checkout.", "error");
        showToast(error.message || "Unable to start payment.", "error");
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = originalLabel;
        }
    }
}

function editSub(id, subscription) {
    if (!requireAdminAction()) {
        return;
    }

    openEditModal({
        title: "Edit Subscription",
        fields: [{
                key: "email",
                label: "Email",
                placeholder: "email@example.com",
                type: "email"
            },
            {
                key: "type",
                label: "Plan Type",
                placeholder: "FREE / PRO / ENTERPRISE"
            }
        ],
        values: {
            email: subscription.email,
            type: subscription.type
        },
        onSave: async (data) => {
            await apiRequest(`/subscriptions/${id}`, "PUT", data);
            await loadSubs();
        }
    });
}

function deleteSub(id, email) {
    if (!requireAdminAction()) {
        return;
    }

    openDeleteModal({
        itemName: email,
        onConfirm: async () => {
            await apiRequest(`/subscriptions/${id}`, "DELETE");
            await loadSubs();
        }
    });
}
