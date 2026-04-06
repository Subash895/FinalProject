package com.smartCity.Web.business;

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
import com.smartCity.Web.business.BusinessDtos;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin("*")
public class BusinessController {
	private final BusinessService service;
	private final ApiDtoMapper apiDtoMapper;

	public BusinessController(BusinessService service, ApiDtoMapper apiDtoMapper) {
		this.service = service;
		this.apiDtoMapper = apiDtoMapper;
	}

	@PostMapping
	public BusinessDtos.BusinessResponse create(@RequestBody BusinessDtos.BusinessRequest entity) {
		return apiDtoMapper.toBusinessResponse(service.save(apiDtoMapper.toBusiness(entity)));
	}

	@GetMapping
	public List<BusinessDtos.BusinessResponse> getAll() {
		return service.getAll().stream().map(apiDtoMapper::toBusinessResponse).collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public org.springframework.http.ResponseEntity<BusinessDtos.BusinessResponse> getById(@PathVariable Long id) {
		return service.getById(id).map(apiDtoMapper::toBusinessResponse).map(org.springframework.http.ResponseEntity::ok)
				.orElse(org.springframework.http.ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public BusinessDtos.BusinessResponse update(@PathVariable Long id, @RequestBody BusinessDtos.BusinessRequest entity) {
		return apiDtoMapper.toBusinessResponse(service.update(id, apiDtoMapper.toBusiness(entity)));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
