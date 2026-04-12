package com.smartCity.Web.marketrate;

import java.time.LocalDate;

/**
 * Groups the request and response DTOs used by the Market Rate API.
 */
public final class MarketRateDtos {

  private MarketRateDtos() {}

  public record MarketRateRequest(
      String productName, Double price, String unit, LocalDate priceDate) {}

  public record MarketRateResponse(
      Integer id, String productName, Double price, String unit, LocalDate priceDate) {}
}
