package com.smartCity.Web.place;

import com.smartCity.Web.city.CityDtos.CityResponse;

/**
 * Groups the request and response DTOs used by the Place API.
 */
public final class PlaceDtos {

  private PlaceDtos() {}

  public record CityRef(Long id) {}

  public record PlaceRequest(
      Long cityId,
      CityRef city,
      String name,
      String description,
      String category,
      String location,
      Double latitude,
      Double longitude) {}

  public record PlaceResponse(
      Long id,
      CityResponse city,
      String name,
      String description,
      String category,
      String location,
      Double latitude,
      Double longitude,
      String imageUrl) {}
}
