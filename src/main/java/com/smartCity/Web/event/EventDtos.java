package com.smartCity.Web.event;

import java.time.LocalDate;

import com.smartCity.Web.city.CityDtos.CityResponse;

public final class EventDtos {

    private EventDtos() {
    }

    public record CityRef(Long id) {
    }

    public record EventRequest(Long cityId, CityRef city, String title, String description, LocalDate eventDate) {
    }

    public record EventResponse(Long id, CityResponse city, String title, String description, LocalDate eventDate) {
    }
}
