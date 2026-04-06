package com.smartCity.Web.city;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.city.CityDtos;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin("*")
public class CityController {
	private final CityService service;
	private final ApiDtoMapper apiDtoMapper;

	public CityController(CityService service, ApiDtoMapper apiDtoMapper) {
		this.service = service;
		this.apiDtoMapper = apiDtoMapper;
	}

	@PostMapping
	public CityDtos.CityResponse create(@RequestBody CityDtos.CityRequest entity) {
		return apiDtoMapper.toCityResponse(service.save(apiDtoMapper.toCity(entity)));
	}

	@GetMapping
	public List<CityDtos.CityResponse> getAll() {
		return service.getAll().stream().map(apiDtoMapper::toCityResponse).collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public org.springframework.http.ResponseEntity<CityDtos.CityResponse> getById(@PathVariable Long id) {
		return service.getById(id).map(apiDtoMapper::toCityResponse).map(org.springframework.http.ResponseEntity::ok)
				.orElse(org.springframework.http.ResponseEntity.notFound().build());
	}

	/*
	@GetMapping("")
	public Optional<City> getById(@RequestParam Long id) {
		return service.getById(id);
	}
*/
	@PutMapping("/{id}")
	public CityDtos.CityResponse update(@PathVariable Long id, @RequestBody CityDtos.CityRequest entity) {
		return apiDtoMapper.toCityResponse(service.update(id, apiDtoMapper.toCity(entity)));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
