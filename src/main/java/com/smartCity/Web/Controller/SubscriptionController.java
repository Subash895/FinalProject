package com.smartCity.Web.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.Model.Subscription;
import com.smartCity.Web.Service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin("*")
public class SubscriptionController {
	@Autowired
	private SubscriptionService service;

	@PostMapping
	public Subscription create(@RequestBody Subscription entity) {
		return service.save(entity);
	}

	@GetMapping
	public List<Subscription> getAll() {
		return service.getAll();
	}

	@GetMapping("/{id}")
	public Optional<Subscription> getById(@PathVariable Long id) {
		return service.getById(id);
	}

	@PutMapping("/{id}")
	public Subscription update(@PathVariable Long id, @RequestBody Subscription entity) {
		return service.update(id, entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
