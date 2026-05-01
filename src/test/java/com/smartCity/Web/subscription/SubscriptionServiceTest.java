package com.smartCity.Web.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartCity.Web.subscription.RazorpayProperties;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private UserRepository userRepository;

  private RazorpayProperties razorpayProperties;
  private SubscriptionService subscriptionService;
  private FakeRazorpayClient razorpayClient;

  @BeforeEach
  void setUp() {
    razorpayProperties = new RazorpayProperties();
    razorpayProperties.setEnabled(true);
    razorpayProperties.setKeyId("rzp_test_key");
    razorpayProperties.setKeySecret("secret123");
    razorpayProperties.setWebhookSecret("webhook-secret");
    razorpayClient = new FakeRazorpayClient(razorpayProperties);
    subscriptionService =
        new SubscriptionService(
            subscriptionRepository,
            userRepository,
            razorpayClient,
            razorpayProperties,
            new ObjectMapper());
  }

  @Test
  void savePopulatesDefaults() {
    User user = new User("Owner", "owner@example.com", "secret", Role.USER);
    Subscription subscription = new Subscription();
    subscription.setUser(user);

    when(subscriptionRepository.save(subscription)).thenReturn(subscription);

    Subscription saved = subscriptionService.save(subscription);

    assertEquals("owner@example.com", saved.getEmail());
    assertEquals("INR", saved.getCurrency());
    assertEquals("MANUAL", saved.getProvider());
    assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
  }

  @Test
  void createCheckoutRejectsUnsupportedPlan() {
    User user = new User("Owner", "owner@example.com", "secret", Role.USER);
    user.setId(8L);
    when(userRepository.findById(8L)).thenReturn(Optional.of(user));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> subscriptionService.createCheckout("basic", 8L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Unsupported plan type. Choose PRO or ENTERPRISE.", exception.getReason());
  }

  @Test
  void confirmCheckoutActivatesOwnedSubscriptionWithValidSignature() throws Exception {
    User user = new User("Owner", "owner@example.com", "secret", Role.USER);
    user.setId(10L);
    Subscription subscription = new Subscription();
    subscription.setId(12L);
    subscription.setUser(user);
    subscription.setProviderOrderId("order_123");

    when(subscriptionRepository.findById(12L)).thenReturn(Optional.of(subscription));
    when(subscriptionRepository.save(subscription)).thenReturn(subscription);

    String signature = sign("order_123|pay_456", "secret123");
    SubscriptionDtos.CheckoutConfirmationRequest request =
        new SubscriptionDtos.CheckoutConfirmationRequest(12L, "order_123", "pay_456", signature);

    Subscription saved = subscriptionService.confirmCheckout(request, 10L);

    assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
    assertEquals("pay_456", saved.getProviderPaymentId());
    assertEquals("ONLINE", saved.getPaymentMethod());
    assertNull(saved.getFailureCode());
    assertNull(saved.getFailureReason());
  }

  @Test
  void reportCheckoutFailureStoresBestAvailableFailureDetails() {
    User user = new User("Owner", "owner@example.com", "secret", Role.USER);
    user.setId(10L);
    Subscription subscription = new Subscription();
    subscription.setId(22L);
    subscription.setUser(user);
    subscription.setProviderOrderId("order_789");

    when(subscriptionRepository.findById(22L)).thenReturn(Optional.of(subscription));
    when(subscriptionRepository.save(subscription)).thenReturn(subscription);

    SubscriptionDtos.CheckoutFailureRequest request =
        new SubscriptionDtos.CheckoutFailureRequest(
            22L, "order_789", "", "", "gateway", "payment", "declined");

    Subscription saved = subscriptionService.reportCheckoutFailure(request, 10L);

    assertEquals(SubscriptionStatus.FAILED, saved.getStatus());
    assertEquals("declined", saved.getFailureCode());
    assertEquals("source=gateway, step=payment, reason=declined", saved.getFailureReason());
  }

  @Test
  void processWebhookRejectsInvalidSignature() {
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> subscriptionService.processWebhook("{}", "wrong-signature"));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    assertEquals("Invalid webhook signature.", exception.getReason());
  }

  @Test
  void processWebhookMarksCapturedPaymentActive() throws Exception {
    User user = new User("Owner", "owner@example.com", "secret", Role.USER);
    user.setId(10L);
    Subscription subscription = new Subscription();
    subscription.setId(30L);
    subscription.setUser(user);
    subscription.setProviderOrderId("order_webhook");

    when(subscriptionRepository.findByProviderOrderId("order_webhook"))
        .thenReturn(Optional.of(subscription));
    when(subscriptionRepository.save(subscription)).thenReturn(subscription);

    String payload =
        """
        {
          "event": "payment.captured",
          "payload": {
            "order": { "entity": { "id": "order_webhook" } },
            "payment": { "entity": { "id": "pay_123", "method": "upi", "created_at": 1714473000 } }
          }
        }
        """;

    SubscriptionDtos.WebhookResponse response =
        subscriptionService.processWebhook(payload, sign(payload, "webhook-secret"));

    assertEquals(true, response.processed());
    assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    assertEquals("pay_123", subscription.getProviderPaymentId());
    assertEquals("UPI", subscription.getPaymentMethod());
  }

  private String sign(String payload, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    StringBuilder builder = new StringBuilder(digest.length * 2);
    for (byte value : digest) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }

  private static final class FakeRazorpayClient extends RazorpayClient {
    private FakeRazorpayClient(RazorpayProperties razorpayProperties) {
      super(new ObjectMapper(), razorpayProperties);
    }
  }
}
