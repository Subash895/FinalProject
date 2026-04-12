package com.smartCity.Web.city;

/**
 * Groups the request and response DTOs used by the City API.
 */
public final class CityDtos {

  private CityDtos() {}

  public record CityRequest(String name, String state, String country) {}

  public record CityResponse(Long id, String name, String state, String country) {}
}
