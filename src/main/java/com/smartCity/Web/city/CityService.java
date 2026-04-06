package com.smartCity.Web.city;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.city.City;
import com.smartCity.Web.city.CityRepository;

@Service
public class CityService {
	@Autowired
	private CityRepository repo;

	public City save(City entity) {
		return repo.save(entity);
	}

	public List<City> getAll() {
		return repo.findAll();
	}

	public Optional<City> getById(Long id) {
		return repo.findById(id);
	}

	public City update(Long id, City entity) {
		entity.setId(id);
		return repo.save(entity);
	}

	public void delete(Long id) {
		repo.deleteById(id);
	}
}
