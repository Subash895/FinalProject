package com.smartCity.Web.city;

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

import com.smartCity.Web.city.City;
import com.smartCity.Web.city.CityService;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin("*")
public class CityController {
	@Autowired
	private CityService service;

	@PostMapping
	public City create(@RequestBody City entity) {
		return service.save(entity);
	}

	@GetMapping
	public List<City> getAll() {
		return service.getAll();
	}

	@GetMapping("/{id}")
	public Optional<City> getById(@PathVariable Long id) {
		return service.getById(id);
	}

	/*
	@GetMapping("")
	public Optional<City> getById(@RequestParam Long id) {
		return service.getById(id);
	}
*/
	@PutMapping("/{id}")
	public City update(@PathVariable Long id, @RequestBody City entity) {
		return service.update(id, entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
