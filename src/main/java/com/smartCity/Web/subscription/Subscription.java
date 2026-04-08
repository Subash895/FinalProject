package com.smartCity.Web.subscription;

import java.time.LocalDateTime;

import com.smartCity.Web.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "subscriptions")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "email")
  private String email;

  private String type;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Double price;
  private String currency;

  @Enumerated(EnumType.STRING)
  private SubscriptionStatus status;

  private String provider;

  @Column(length = 128)
  private String providerPlanId;

  @Column(length = 128, unique = true)
  private String providerOrderId;

  @Column(length = 128, unique = true)
  private String providerSubscriptionId;

  @Column(length = 128)
  private String providerPaymentId;

  @Column(length = 64)
  private String paymentMethod;

  private LocalDateTime nextBillingAt;

  @Column(length = 128)
  private String failureCode;

  @Column(length = 1000)
  private String failureReason;

  @Column(length = 128)
  private String lastWebhookEvent;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Subscription() {}

  public Subscription(
      User user, String email, String type, LocalDateTime start, LocalDateTime end, Double price) {
    this.user = user;
    this.email = email;
    this.type = type;
    this.startDate = start;
    this.endDate = end;
    this.price = price;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public SubscriptionStatus getStatus() {
    return status;
  }

  public void setStatus(SubscriptionStatus status) {
    this.status = status;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getProviderPlanId() {
    return providerPlanId;
  }

  public void setProviderPlanId(String providerPlanId) {
    this.providerPlanId = providerPlanId;
  }

  public String getProviderOrderId() {
    return providerOrderId;
  }

  public void setProviderOrderId(String providerOrderId) {
    this.providerOrderId = providerOrderId;
  }

  public String getProviderSubscriptionId() {
    return providerSubscriptionId;
  }

  public void setProviderSubscriptionId(String providerSubscriptionId) {
    this.providerSubscriptionId = providerSubscriptionId;
  }

  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public void setProviderPaymentId(String providerPaymentId) {
    this.providerPaymentId = providerPaymentId;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public LocalDateTime getNextBillingAt() {
    return nextBillingAt;
  }

  public void setNextBillingAt(LocalDateTime nextBillingAt) {
    this.nextBillingAt = nextBillingAt;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public void setFailureCode(String failureCode) {
    this.failureCode = failureCode;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public String getLastWebhookEvent() {
    return lastWebhookEvent;
  }

  public void setLastWebhookEvent(String lastWebhookEvent) {
    this.lastWebhookEvent = lastWebhookEvent;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    if (status == null) {
      status = SubscriptionStatus.PENDING;
    }
    if (currency == null || currency.isBlank()) {
      currency = "INR";
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
    if (currency == null || currency.isBlank()) {
      currency = "INR";
    }
  }
}
