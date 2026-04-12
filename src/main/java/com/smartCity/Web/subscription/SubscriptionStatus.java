package com.smartCity.Web.subscription;

/**
 * Lists the lifecycle states a subscription can move through during checkout and renewal.
 */
public enum SubscriptionStatus {
  PENDING,
  AUTHENTICATED,
  ACTIVE,
  PAUSED,
  CANCELLED,
  EXPIRED,
  FAILED
}
