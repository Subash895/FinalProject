package com.smartCity.Web.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.Place;
import com.smartCity.Web.Service.PlaceService;
import com.smartCity.Web.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Place>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        placeService.getAllPlaces(),
                        "Places fetched successfully"
                )
        );
    }
}