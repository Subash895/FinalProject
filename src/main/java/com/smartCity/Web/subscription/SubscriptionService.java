package com.smartCity.Web.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartCity.Web.config.RazorpayProperties;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubscriptionService {
  private static final String PROVIDER = "RAZORPAY";
  private static final ZoneId APP_ZONE = ZoneId.systemDefault();

  private final SubscriptionRepository repo;
  private final UserRepository userRepository;
  private final RazorpayClient razorpayClient;
  private final RazorpayProperties razorpayProperties;
  private final ObjectMapper objectMapper;

  public SubscriptionService(
      SubscriptionRepository repo,
      UserRepository userRepository,
      RazorpayClient razorpayClient,
      RazorpayProperties razorpayProperties,
      ObjectMapper objectMapper) {
    this.repo = repo;
    this.userRepository = userRepository;
    this.razorpayClient = razorpayClient;
    this.razorpayProperties = razorpayProperties;
    this.objectMapper = objectMapper;
  }

  public Subscription save(Subscription entity) {
    populateDefaults(entity);
    return repo.save(entity);
  }

  public List<Subscription> getAll() {
    return repo.findAll();
  }

  public List<Subscription> getByEmail(String email) {
    return repo.findByEmail(email);
  }

  public List<Subscription> getByUserId(Long userId) {
    return repo.findByUserIdOrderByUpdatedAtDesc(userId);
  }

  public Optional<Subscription> getById(Long id) {
    return repo.findById(id);
  }

  public Subscription update(Long id, Subscription entity) {
    Subscription current =
        repo.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found."));
    entity.setId(id);
    if (entity.getProvider() == null) {
      entity.setProvider(current.getProvider());
    }
    if (entity.getProviderPlanId() == null) {
      entity.setProviderPlanId(current.getProviderPlanId());
    }
    if (entity.getProviderOrderId() == null) {
      entity.setProviderOrderId(current.getProviderOrderId());
    }
    if (entity.getProviderSubscriptionId() == null) {
      entity.setProviderSubscriptionId(current.getProviderSubscriptionId());
    }
    if (entity.getProviderPaymentId() == null) {
      entity.setProviderPaymentId(current.getProviderPaymentId());
    }
    if (entity.getStatus() == null) {
      entity.setStatus(current.getStatus());
    }
    if (entity.getPaymentMethod() == null) {
      entity.setPaymentMethod(current.getPaymentMethod());
    }
    if (entity.getCurrency() == null) {
      entity.setCurrency(current.getCurrency());
    }
    if (entity.getNextBillingAt() == null) {
      entity.setNextBillingAt(current.getNextBillingAt());
    }
    if (entity.getFailureCode() == null) {
      entity.setFailureCode(current.getFailureCode());
    }
    if (entity.getFailureReason() == null) {
      entity.setFailureReason(current.getFailureReason());
    }
    populateDefaults(entity);
    return repo.save(entity);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }

  @Transactional
  public SubscriptionDtos.CheckoutResponse createCheckout(String planType, Long userId) {
    if (!razorpayProperties.isConfigured()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Payment is not configured. Add Razorpay environment variables first.");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    PlanDefinition plan = resolvePlan(planType);

    Subscription subscription = new Subscription();
    subscription.setUser(user);
    subscription.setEmail(user.getEmail());
    subscription.setType(plan.type());
    subscription.setPrice(plan.amount());
    subscription.setCurrency(plan.currency());
    subscription.setStatus(SubscriptionStatus.PENDING);
    subscription.setProvider(PROVIDER);
    subscription.setProviderPlanId(null);
    subscription.setProviderSubscriptionId(null);
    subscription.setFailureCode(null);
    subscription.setFailureReason(null);
    subscription = repo.save(subscription);

    Map<String, String> notes = new LinkedHashMap<>();
    notes.put("local_subscription_id", String.valueOf(subscription.getId()));
    notes.put("user_id", String.valueOf(user.getId()));
    notes.put("email", user.getEmail());
    notes.put("plan_type", plan.type());

    RazorpayClient.RazorpayOrder razorpayOrder;
    try {
      razorpayOrder =
          razorpayClient.createOrder(
              toPaise(plan.amount()), plan.currency(), "sub_" + subscription.getId(), notes);
    } catch (ResponseStatusException ex) {
      subscription.setStatus(SubscriptionStatus.FAILED);
      subscription.setFailureReason(ex.getReason());
      repo.save(subscription);
      throw ex;
    }

    subscription.setProviderOrderId(razorpayOrder.id());
    subscription.setLastWebhookEvent("checkout_created");
    repo.save(subscription);

    return new SubscriptionDtos.CheckoutResponse(
        subscription.getId(),
        razorpayProperties.getKeyId(),
        razorpayOrder.id(),
        plan.type(),
        plan.amount(),
        plan.currency(),
        razorpayOrder.status(),
        user.getEmail(),
        user.getName(),
        "Complete payment to activate your plan.");
  }

  @Transactional
  public Subscription confirmCheckout(
      SubscriptionDtos.CheckoutConfirmationRequest request, Long userId) {
    Subscription subscription = getOwnedSubscription(request.localSubscriptionId(), userId);

    if (request.razorpayOrderId() == null
        || request.razorpayOrderId().isBlank()
        || request.razorpayPaymentId() == null
        || request.razorpayPaymentId().isBlank()
        || request.razorpaySignature() == null
        || request.razorpaySignature().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing payment confirmation data.");
    }

    if (subscription.getProviderOrderId() == null
        || !subscription.getProviderOrderId().equals(request.razorpayOrderId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Payment confirmation does not match the checkout record.");
    }

    verifyCheckoutSignature(
        request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature());

    subscription.setProviderPaymentId(request.razorpayPaymentId());
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setPaymentMethod("ONLINE");
    subscription.setLastWebhookEvent("checkout_confirmed");
    LocalDateTime now = LocalDateTime.now();
    subscription.setStartDate(now);
    subscription.setEndDate(now.plusMonths(1));
    subscription.setNextBillingAt(null);
    clearFailure(subscription);
    return repo.save(subscription);
  }

  @Transactional
  public Subscription reportCheckoutFailure(
      SubscriptionDtos.CheckoutFailureRequest request, Long userId) {
    Subscription subscription = getOwnedSubscription(request.localSubscriptionId(), userId);

    if (request.razorpayOrderId() != null
        && !request.razorpayOrderId().isBlank()
        && subscription.getProviderOrderId() != null
        && !subscription.getProviderOrderId().equals(request.razorpayOrderId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Failure report does not match the checkout record.");
    }

    subscription.setStatus(SubscriptionStatus.FAILED);
    subscription.setFailureCode(firstNonBlank(request.code(), request.reason(), "payment_failed"));
    subscription.setFailureReason(
        firstNonBlank(
            request.description(),
            joinFailureDetails(request.source(), request.step(), request.reason()),
            "Payment failed."));
    subscription.setLastWebhookEvent("checkout_failed");
    return repo.save(subscription);
  }

  @Transactional
  public SubscriptionDtos.WebhookResponse processWebhook(String payload, String signature) {
    if (!isValidWebhookSignature(payload, signature)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook signature.");
    }

    try {
      JsonNode root = objectMapper.readTree(payload);
      String event = root.path("event").asText("");
      JsonNode orderNode = root.path("payload").path("order").path("entity");
      JsonNode paymentNode = root.path("payload").path("payment").path("entity");
      String providerOrderId = orderNode.path("id").asText("");

      if (providerOrderId.isBlank()) {
        providerOrderId = paymentNode.path("order_id").asText("");
      }

      if (providerOrderId.isBlank()) {
        return new SubscriptionDtos.WebhookResponse(false, "No provider order id found.");
      }

      final String finalProviderOrderId = providerOrderId;
      Subscription subscription =
          repo.findByProviderOrderId(finalProviderOrderId)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND,
                          "Subscription not found for provider id " + finalProviderOrderId));

      subscription.setLastWebhookEvent(event);

      switch (event) {
        case "payment.authorized" -> {
          subscription.setStatus(SubscriptionStatus.AUTHENTICATED);
          applySuccessfulPayment(subscription, paymentNode);
          clearFailure(subscription);
        }
        case "payment.captured", "order.paid" -> {
          subscription.setStatus(SubscriptionStatus.ACTIVE);
          applySuccessfulPayment(subscription, paymentNode);
          clearFailure(subscription);
        }
        case "payment.failed" -> {
          subscription.setStatus(SubscriptionStatus.FAILED);
          applyPaymentDetails(subscription, paymentNode);
          subscription.setFailureCode(paymentNode.path("error_code").asText(null));
          subscription.setFailureReason(paymentNode.path("error_description").asText("Payment failed."));
        }
        default -> {
          return new SubscriptionDtos.WebhookResponse(
              false, "Ignored event: " + (event.isBlank() ? "unknown" : event));
        }
      }

      repo.save(subscription);
      return new SubscriptionDtos.WebhookResponse(true, "Processed event: " + event);
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to process webhook payload.", ex);
    }
  }

  private void populateDefaults(Subscription entity) {
    populateEmail(entity);
    if (entity.getCurrency() == null || entity.getCurrency().isBlank()) {
      entity.setCurrency("INR");
    }
    if (entity.getProvider() == null || entity.getProvider().isBlank()) {
      entity.setProvider("MANUAL");
    }
    if (entity.getStatus() == null) {
      entity.setStatus(SubscriptionStatus.ACTIVE);
    }
  }

  private void populateEmail(Subscription entity) {
    if ((entity.getEmail() == null || entity.getEmail().isBlank()) && entity.getUser() != null) {
      entity.setEmail(entity.getUser().getEmail());
    }
  }

  private PlanDefinition resolvePlan(String rawPlanType) {
    String planType = rawPlanType == null ? "" : rawPlanType.trim().toUpperCase();
    return switch (planType) {
      case "PRO" -> new PlanDefinition("PRO", 299.0, "INR");
      case "ENTERPRISE" -> new PlanDefinition("ENTERPRISE", 999.0, "INR");
      case "FREE" ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Free plan does not require payment.");
      default ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Unsupported plan type. Choose PRO or ENTERPRISE.");
    };
  }

  private long toPaise(Double amount) {
    return Math.round(amount * 100);
  }

  private boolean isValidWebhookSignature(String payload, String signature) {
    if (signature == null
        || signature.isBlank()
        || razorpayProperties.getWebhookSecret() == null
        || razorpayProperties.getWebhookSecret().isBlank()) {
      return false;
    }

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(
          new SecretKeySpec(
              razorpayProperties.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
              "HmacSHA256"));
      String expected = bytesToHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
      return MessageDigest.isEqual(
          expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to verify webhook signature.", ex);
    }
  }

  private void verifyCheckoutSignature(
      String orderId, String paymentId, String providedSignature) {
    String secret = razorpayProperties.getKeySecret();
    if (secret == null || secret.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Payment secret is not configured.");
    }

    String payload = orderId + "|" + paymentId;
    String expected = hmacSha256Hex(payload, secret);
    if (!MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        providedSignature.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid payment signature.");
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }

  private String hmacSha256Hex(String payload, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return bytesToHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to verify payment signature.", ex);
    }
  }

  private void applyPaymentDetails(Subscription subscription, JsonNode paymentNode) {
    if (paymentNode == null || paymentNode.isMissingNode()) {
      return;
    }

    String paymentId = paymentNode.path("id").asText("");
    if (!paymentId.isBlank()) {
      subscription.setProviderPaymentId(paymentId);
    }

    String method = paymentNode.path("method").asText("");
    if (!method.isBlank()) {
      subscription.setPaymentMethod(method.toUpperCase());
    }
  }

  private void applySuccessfulPayment(Subscription subscription, JsonNode paymentNode) {
    applyPaymentDetails(subscription, paymentNode);
    LocalDateTime paidAt = toLocalDateTime(paymentNode.path("created_at"));
    LocalDateTime startAt = paidAt != null ? paidAt : LocalDateTime.now();
    subscription.setStartDate(startAt);
    subscription.setEndDate(startAt.plusMonths(1));
    subscription.setNextBillingAt(null);
  }

  private void clearFailure(Subscription subscription) {
    subscription.setFailureCode(null);
    subscription.setFailureReason(null);
  }

  private Subscription getOwnedSubscription(Long localSubscriptionId, Long userId) {
    Subscription subscription =
        repo.findById(localSubscriptionId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found."));
    if (subscription.getUser() == null
        || subscription.getUser().getId() == null
        || !subscription.getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription does not belong to user.");
    }
    return subscription;
  }

  private String joinFailureDetails(String source, String step, String reason) {
    StringBuilder builder = new StringBuilder();
    if (source != null && !source.isBlank()) {
      builder.append("source=").append(source);
    }
    if (step != null && !step.isBlank()) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("step=").append(step);
    }
    if (reason != null && !reason.isBlank()) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("reason=").append(reason);
    }
    return builder.toString();
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private LocalDateTime toLocalDateTime(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    long epochSeconds = node.asLong(0L);
    if (epochSeconds <= 0L) {
      return null;
    }
    return Instant.ofEpochSecond(epochSeconds).atZone(APP_ZONE).toLocalDateTime();
  }

  private record PlanDefinition(String type, Double amount, String currency) {}
}
