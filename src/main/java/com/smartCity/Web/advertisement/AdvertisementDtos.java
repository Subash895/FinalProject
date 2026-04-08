package com.smartCity.Web.advertisement;

import java.time.LocalDateTime;

import com.smartCity.Web.business.BusinessDtos.BusinessResponse;

public final class AdvertisementDtos {

  private AdvertisementDtos() {}

  public record BusinessRef(Long id) {}

  public record AdvertisementRequest(
      Long businessId,
      BusinessRef business,
      String title,
      String content,
      Double cost,
      LocalDateTime startDate,
      LocalDateTime endDate) {}

  public record AdvertisementResponse(
      Long id,
      BusinessResponse business,
      String title,
      String content,
      Double cost,
      LocalDateTime startDate,
      LocalDateTime endDate) {}
}
