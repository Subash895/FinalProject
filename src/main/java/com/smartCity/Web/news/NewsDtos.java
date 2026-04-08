package com.smartCity.Web.news;

import java.time.LocalDateTime;

import com.smartCity.Web.city.CityDtos.CityResponse;

public final class NewsDtos {

  private NewsDtos() {}

  public record CityRef(Long id) {}

  public record NewsRequest(
      Long cityId, CityRef city, String title, String content, LocalDateTime createdAt) {}

  public record NewsResponse(
      Long id, CityResponse city, String title, String content, LocalDateTime createdAt) {}
}
