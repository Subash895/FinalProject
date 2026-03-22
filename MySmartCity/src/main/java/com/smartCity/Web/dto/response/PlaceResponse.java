package com.smartCity.Web.dto.response;

import lombok.Data;

@Data
public class PlaceResponse {

    private Long id;
    private String name;
    private String category;
    private String description;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String address;
    private String website;
    private String operatingHours;

    // Optional (if needed)
    private Long cityId;
}