package com.smartCity.Web.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartCity.Web.config.RazorpayProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Wraps the Razorpay HTTP integration used to create and confirm subscription orders.
 */
@Component
public class RazorpayClient {
  private static final String API_BASE = "https://api.razorpay.com/v1";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final RazorpayProperties razorpayProperties;

  public RazorpayClient(ObjectMapper objectMapper, RazorpayProperties razorpayProperties) {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
    this.razorpayProperties = razorpayProperties;
  }

  public RazorpayOrder createOrder(
      long amountInPaise, String currency, String receipt, Map<String, String> notes) {
    ensureConfigured();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("amount", amountInPaise);
    payload.put("currency", currency);
    payload.put("receipt", receipt);
    payload.put("notes", notes);

    JsonNode response = sendJsonRequest("/orders", payload);
    return new RazorpayOrder(
        response.path("id").asText(),
        response.path("status").asText("created"),
        response.path("amount").asLong());
  }

  private JsonNode sendJsonRequest(String path, Map<String, Object> payload) {
    try {
      String body = objectMapper.writeValueAsString(payload);
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(API_BASE + path))
              .header("Authorization", basicAuthHeader())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw buildApiException(response.body(), response.statusCode());
      }

      return objectMapper.readTree(response.body());
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to read payment provider response.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Payment provider request was interrupted.", e);
    }
  }

  private ResponseStatusException buildApiException(String responseBody, int statusCode) {
    String message = "Payment provider request failed.";
    try {
      JsonNode errorNode = objectMapper.readTree(responseBody).path("error");
      if (!errorNode.isMissingNode()) {
        String description = errorNode.path("description").asText("");
        String reason = errorNode.path("reason").asText("");
        String code = errorNode.path("code").asText("");
        StringBuilder builder = new StringBuilder();
        if (!description.isBlank()) {
          builder.append(description);
        }
        if (!reason.isBlank()) {
          if (builder.length() > 0) {
            builder.append(" ");
          }
          builder.append(reason);
        }
        if (!code.isBlank()) {
          if (builder.length() > 0) {
            builder.append(" ");
          }
          builder.append("[").append(code).append("]");
        }
        if (builder.length() > 0) {
          message = builder.toString();
        }
      }
    } catch (IOException ignored) {
    }

    HttpStatus status = statusCode >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
    return new ResponseStatusException(status, message);
  }

  private String basicAuthHeader() {
    String raw = razorpayProperties.getKeyId() + ":" + razorpayProperties.getKeySecret();
    return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private void ensureConfigured() {
    if (!razorpayProperties.isConfigured()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Razorpay is not configured. Set the payment environment variables first.");
    }
  }

  public record RazorpayOrder(String id, String status, long amountInPaise) {}
}
