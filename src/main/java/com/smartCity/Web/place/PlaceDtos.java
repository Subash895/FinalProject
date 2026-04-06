package com.smartCity.Web.place;

import com.smartCity.Web.city.CityDtos.CityResponse;

public final class PlaceDtos {

    private PlaceDtos() {
    }

    public record CityRef(Long id) {
    }

    public record PlaceRequest(Long cityId, CityRef city, String name, String description, String category,
            String location, Double latitude, Double longitude) {
    }

    public record PlaceResponse(Long id, CityResponse city, String name, String description, String category,
            String location, Double latitude, Double longitude) {
    }
}
