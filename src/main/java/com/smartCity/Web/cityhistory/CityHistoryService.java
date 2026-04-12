package com.smartCity.Web.cityhistory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Coordinates the business rules for City History features before data is stored or returned.
 */
@Service
public class CityHistoryService {

  @Autowired private CityHistoryRepository repository;

  public CityHistory createCityHistory(CityHistory history) {
    return repository.save(history);
  }

  public List<CityHistory> getAllCityHistory() {
    return repository.findAll();
  }

  public List<CityHistory> getCityHistoryByCity(Long cityId, String cityName) {
    return repository.findByCityIdAndCityNameIgnoreCaseOrderByIdAsc(cityId, cityName);
  }

  public CityHistory updateCityHistory(Long id, CityHistory history) {

    CityHistory existing =
        repository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("CityHistory not found with id: " + id));
    existing.setTitle(history.getTitle());
    existing.setContent(history.getContent());
    existing.setCity(history.getCity());

    return repository.save(existing);
  }

  public void deleteCityHistory(Long id) {
    repository.deleteById(id);
  }
}
