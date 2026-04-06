package com.smartCity.Web.cityhistory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cityhistory")
@CrossOrigin("*")
public class CityHistoryController {

	@Autowired
	private CityHistoryService service;

	@PostMapping
	public CityHistory createCityHistory(@RequestBody CityHistory history) {
		return service.createCityHistory(history);
	}

	
	@PutMapping("/{id}")
	public CityHistory update(@PathVariable Long id, @RequestBody CityHistory history) {
		return service.updateCityHistory(id, history);
	}

	@GetMapping
	public List<CityHistory> getAllCityHistory() {
		return service.getAllCityHistory();
	}
}
