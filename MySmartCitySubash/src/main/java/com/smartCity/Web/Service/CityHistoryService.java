package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.CityHistory;
import com.smartCity.Web.Repository.CityHistoryRepository;

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
}