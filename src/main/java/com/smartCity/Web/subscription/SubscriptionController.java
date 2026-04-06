package com.smartCity.Web.subscription;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.subscription.SubscriptionDtos;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin("*")
public class SubscriptionController {
	private final SubscriptionService service;
	private final ApiDtoMapper apiDtoMapper;

	public SubscriptionController(SubscriptionService service, ApiDtoMapper apiDtoMapper) {
		this.service = service;
		this.apiDtoMapper = apiDtoMapper;
	}

	@PostMapping
	public SubscriptionDtos.SubscriptionResponse create(@RequestBody SubscriptionDtos.SubscriptionRequest entity) {
		return apiDtoMapper.toSubscriptionResponse(service.save(apiDtoMapper.toSubscription(entity)));
	}

	@GetMapping
	public List<SubscriptionDtos.SubscriptionResponse> getAll() {
		return service.getAll().stream().map(apiDtoMapper::toSubscriptionResponse).collect(Collectors.toList());
	}

	@GetMapping(params = "email")
	public List<SubscriptionDtos.SubscriptionResponse> getByEmail(@RequestParam String email) {
		return service.getByEmail(email).stream().map(apiDtoMapper::toSubscriptionResponse).collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public org.springframework.http.ResponseEntity<SubscriptionDtos.SubscriptionResponse> getById(@PathVariable Long id) {
		return service.getById(id).map(apiDtoMapper::toSubscriptionResponse).map(org.springframework.http.ResponseEntity::ok)
				.orElse(org.springframework.http.ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public SubscriptionDtos.SubscriptionResponse update(@PathVariable Long id, @RequestBody SubscriptionDtos.SubscriptionRequest entity) {
		return apiDtoMapper.toSubscriptionResponse(service.update(id, apiDtoMapper.toSubscription(entity)));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}

