package com.smartCity.Web.cityhistory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityHistoryService {

    @Autowired
    private CityHistoryRepository repository;

    public CityHistory createCityHistory(CityHistory history) {
        return repository.save(history);
    }

    public List<CityHistory> getAllCityHistory() {
        return repository.findAll();
    }

    public CityHistory updateCityHistory(Long id, CityHistory history) {

        CityHistory existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("CityHistory not found with id: " + id));
        existing.setTitle(history.getTitle());
        existing.setContent(history.getContent());
        existing.setCity(history.getCity());

        return repository.save(existing);
    }
	
}
