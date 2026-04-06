package com.smartCity.Web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@CrossOrigin("*")
public class PublicConfigController {

	@Value("${app.maps.google-api-key:}")
	private String googleMapsApiKey;

	@GetMapping("/public")
	public PublicConfigResponse getPublicConfig() {
		return new PublicConfigResponse(googleMapsApiKey);
	}

	public record PublicConfigResponse(String googleMapsApiKey) {
	}
}
