package com.smartCity.Web.Controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.Service.PlaceService;
import com.smartCity.Web.dto.response.ApiResponse;
import com.smartCity.Web.dto.response.PagedResponse;
import com.smartCity.Web.dto.response.PlaceResponse;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

	private final PlaceService placeService;

	public PlaceController(PlaceService placeService) {
		this.placeService = placeService;
	}

	@GetMapping("/city/{cityId}")
	public ResponseEntity<ApiResponse<PagedResponse<PlaceResponse>>> getPlacesByCity(@PathVariable Long cityId,
			Pageable pageable) {

		PagedResponse<PlaceResponse> response = placeService.getPlacesByCity(cityId, pageable);

		return ResponseEntity.ok(new ApiResponse<>(true, response, "Places fetched successfully"));
	}
}