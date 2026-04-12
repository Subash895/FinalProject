package com.smartCity.Web.subscription;

import java.time.LocalDateTime;

import com.smartCity.Web.user.UserDtos.UserResponse;

/**
 * Groups the request and response DTOs used by the Subscription API.
 */
public final class SubscriptionDtos {

  private SubscriptionDtos() {}

  public record UserRef(Long id) {}

  public record SubscriptionRequest(
      Long userId,
      UserRef user,
      String email,
      String type,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Double price,
      String currency,
      SubscriptionStatus status) {}

  public record SubscriptionResponse(
      Long id,
      UserResponse user,
      String email,
      String type,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Double price,
      String currency,
      SubscriptionStatus status,
      String provider,
      String providerOrderId,
      String providerSubscriptionId,
      String providerPaymentId,
      String paymentMethod,
      String failureReason,
      LocalDateTime nextBillingAt,
      LocalDateTime updatedAt) {}

  public record CheckoutRequest(String type) {}

  public record CheckoutResponse(
      Long localSubscriptionId,
      String keyId,
      String orderId,
      String planType,
      Double price,
      String currency,
      String status,
      String customerEmail,
      String customerName,
      String message) {}

  public record CheckoutConfirmationRequest(
      Long localSubscriptionId,
      String razorpayOrderId,
      String razorpayPaymentId,
      String razorpaySignature) {}

  public record CheckoutFailureRequest(
      Long localSubscriptionId,
      String razorpayOrderId,
      String code,
      String description,
      String source,
      String step,
      String reason) {}

  public record WebhookResponse(boolean processed, String message) {}
}
