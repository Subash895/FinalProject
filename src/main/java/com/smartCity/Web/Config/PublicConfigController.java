package com.smartCity.Web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@CrossOrigin("*")
public class PublicConfigController {

  @Value("${app.maps.google-api-key:}")
  private String googleMapsApiKey;

  @Value("${app.payments.razorpay.enabled:false}")
  private boolean razorpayEnabled;

  @Value("${app.payments.razorpay.key-id:}")
  private String razorpayKeyId;

  @GetMapping("/public")
  public PublicConfigResponse getPublicConfig() {
    return new PublicConfigResponse(googleMapsApiKey, razorpayEnabled, razorpayKeyId);
  }

  public record PublicConfigResponse(
      String googleMapsApiKey, boolean razorpayEnabled, String razorpayKeyId) {}
}
