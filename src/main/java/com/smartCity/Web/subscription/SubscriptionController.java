package com.smartCity.Web.subscription;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.auth.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.subscription.SubscriptionDtos;

@RestController
@CrossOrigin("*")
public class SubscriptionController {
  private final SubscriptionService service;
  private final ApiDtoMapper apiDtoMapper;

  public SubscriptionController(SubscriptionService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping("/api/subscriptions")
  public SubscriptionDtos.SubscriptionResponse create(
      @RequestBody SubscriptionDtos.SubscriptionRequest entity) {
    return apiDtoMapper.toSubscriptionResponse(service.save(apiDtoMapper.toSubscription(entity)));
  }

  @GetMapping("/api/subscriptions")
  public List<SubscriptionDtos.SubscriptionResponse> getAll() {
    return service.getAll().stream()
        .map(apiDtoMapper::toSubscriptionResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/api/subscriptions", params = "email")
  public List<SubscriptionDtos.SubscriptionResponse> getByEmail(@RequestParam String email) {
    return service.getByEmail(email).stream()
        .map(apiDtoMapper::toSubscriptionResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/api/subscriptions/my")
  public List<SubscriptionDtos.SubscriptionResponse> getMySubscriptions(
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return service.getByUserId(principal.id()).stream()
        .map(apiDtoMapper::toSubscriptionResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/api/subscriptions/{id}")
  public ResponseEntity<SubscriptionDtos.SubscriptionResponse> getById(
      @PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toSubscriptionResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/api/subscriptions/{id}")
  public SubscriptionDtos.SubscriptionResponse update(
      @PathVariable Long id, @RequestBody SubscriptionDtos.SubscriptionRequest entity) {
    return apiDtoMapper.toSubscriptionResponse(
        service.update(id, apiDtoMapper.toSubscription(entity)));
  }

  @DeleteMapping("/api/subscriptions/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PostMapping("/api/subscriptions/checkout")
  public SubscriptionDtos.CheckoutResponse createCheckout(
      @RequestBody SubscriptionDtos.CheckoutRequest request,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return service.createCheckout(request.type(), principal.id());
  }

  @PostMapping("/api/subscriptions/confirm")
  public SubscriptionDtos.SubscriptionResponse confirmCheckout(
      @RequestBody SubscriptionDtos.CheckoutConfirmationRequest request,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return apiDtoMapper.toSubscriptionResponse(service.confirmCheckout(request, principal.id()));
  }

  @PostMapping("/api/subscriptions/failure")
  public SubscriptionDtos.SubscriptionResponse reportCheckoutFailure(
      @RequestBody SubscriptionDtos.CheckoutFailureRequest request,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return apiDtoMapper.toSubscriptionResponse(service.reportCheckoutFailure(request, principal.id()));
  }

  @PostMapping("/api/payments/webhook")
  public SubscriptionDtos.WebhookResponse handleWebhook(
      @RequestBody String payload,
      @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature) {
    return service.processWebhook(payload, signature);
  }
}
