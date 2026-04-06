package com.smartCity.Web.business;

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

import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessService;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin("*")
public class BusinessController {
	@Autowired
	private BusinessService service;

	@PostMapping
	public Business create(@RequestBody Business entity) {
		return service.save(entity);
	}

	@GetMapping
	public List<Business> getAll() {
		return service.getAll();
	}

	@GetMapping("/{id}")
	public Optional<Business> getById(@PathVariable Long id) {
		return service.getById(id);
	}

	@PutMapping("/{id}")
	public Business update(@PathVariable Long id, @RequestBody Business entity) {
		return service.update(id, entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
