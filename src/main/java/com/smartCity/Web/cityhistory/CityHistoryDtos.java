package com.smartCity.Web.cityhistory;

import com.smartCity.Web.city.CityDtos.CityResponse;

public final class CityHistoryDtos {

    private CityHistoryDtos() {
    }

    public record CityRef(Long id) {
    }

    public record CityHistoryRequest(Long cityId, CityRef city, String title, String content) {
    }

    public record CityHistoryResponse(Long id, CityResponse city, String title, String content) {
    }
}
