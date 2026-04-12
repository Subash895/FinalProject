package com.smartCity.Web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds Google authentication settings from configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "app.auth")
public class GoogleAuthProperties {

  private String googleClientId;

  public String getGoogleClientId() {
    return googleClientId;
  }

  public void setGoogleClientId(String googleClientId) {
    this.googleClientId = googleClientId;
  }
}
