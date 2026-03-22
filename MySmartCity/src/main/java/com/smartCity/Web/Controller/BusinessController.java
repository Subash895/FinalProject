package com.smartCity.Web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.Service.BusinessService;
import com.smartCity.Web.dto.request.BusinessRequest;
import com.smartCity.Web.dto.response.ApiResponse;
import com.smartCity.Web.dto.response.BusinessResponse;
import com.smartCity.Web.dto.response.PagedResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

	private final BusinessService businessService;

	public BusinessController(BusinessService businessService) {
		this.businessService = businessService;
	}

	// CREATE
	@PostMapping
	public ResponseEntity<ApiResponse<BusinessResponse>> create(@Valid @RequestBody BusinessRequest request) {

		return ResponseEntity
				.ok(new ApiResponse<>(true, businessService.createBusiness(request), "Business created successfully"));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<BusinessResponse>>> getAll(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			@RequestParam(required = false) String category, @RequestParam(required = false) String name,
			@RequestParam(required = false) String sortBy, @RequestParam(required = false) String order) {

		return ResponseEntity
				.ok(new ApiResponse<>(true, businessService.getBusinesses(page, size, category, name, sortBy, order),
						"Businesses fetched successfully"));
	}
}