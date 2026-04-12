package com.smartCity.Web.cityhistory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.cityhistory.CityHistoryDtos;

/**
 * Exposes REST endpoints for City History operations.
 */
@RestController
@RequestMapping("/api/cityhistory")
@CrossOrigin("*")
public class CityHistoryController {

  private final CityHistoryService service;
  private final ApiDtoMapper apiDtoMapper;

  public CityHistoryController(CityHistoryService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public CityHistoryDtos.CityHistoryResponse createCityHistory(
      @RequestBody CityHistoryDtos.CityHistoryRequest history) {
    return apiDtoMapper.toCityHistoryResponse(
        service.createCityHistory(apiDtoMapper.toCityHistory(history)));
  }

  @PutMapping("/{id}")
  public CityHistoryDtos.CityHistoryResponse update(
      @PathVariable Long id, @RequestBody CityHistoryDtos.CityHistoryRequest history) {
    return apiDtoMapper.toCityHistoryResponse(
        service.updateCityHistory(id, apiDtoMapper.toCityHistory(history)));
  }

  @GetMapping
  public List<CityHistoryDtos.CityHistoryResponse> getAllCityHistory() {
    return service.getAllCityHistory().stream()
        .map(apiDtoMapper::toCityHistoryResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/city/{cityId}")
  public List<CityHistoryDtos.CityHistoryResponse> getCityHistoryByCity(
      @PathVariable Long cityId, @RequestParam String cityName) {
    return service.getCityHistoryByCity(cityId, cityName).stream()
        .map(apiDtoMapper::toCityHistoryResponse)
        .collect(Collectors.toList());
  }
}
