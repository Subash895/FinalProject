package com.smartCity.Web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payments.razorpay")
public class RazorpayProperties {
  private boolean enabled;
  private String keyId;
  private String keySecret;
  private String webhookSecret;
  private String proPlanId;
  private String enterprisePlanId;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public String getKeySecret() {
    return keySecret;
  }

  public void setKeySecret(String keySecret) {
    this.keySecret = keySecret;
  }

  public String getWebhookSecret() {
    return webhookSecret;
  }

  public void setWebhookSecret(String webhookSecret) {
    this.webhookSecret = webhookSecret;
  }

  public String getProPlanId() {
    return proPlanId;
  }

  public void setProPlanId(String proPlanId) {
    this.proPlanId = proPlanId;
  }

  public String getEnterprisePlanId() {
    return enterprisePlanId;
  }

  public void setEnterprisePlanId(String enterprisePlanId) {
    this.enterprisePlanId = enterprisePlanId;
  }

  public boolean isConfigured() {
    return enabled
        && keyId != null
        && !keyId.isBlank()
        && keySecret != null
        && !keySecret.isBlank();
  }
}
