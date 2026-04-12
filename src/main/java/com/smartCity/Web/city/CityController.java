package com.smartCity.Web.city;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CityController {
  private final CityService service;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping
  public CityDtos.CityResponse create(@RequestBody CityDtos.CityRequest entity) {
    return apiDtoMapper.toCityResponse(service.save(apiDtoMapper.toCity(entity)));
  }

  @GetMapping
  public List<CityDtos.CityResponse> getAll() {
    return service.getAll().stream().map(apiDtoMapper::toCityResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CityDtos.CityResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toCityResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public CityDtos.CityResponse update(
      @PathVariable Long id, @RequestBody CityDtos.CityRequest entity) {
    return apiDtoMapper.toCityResponse(service.update(id, apiDtoMapper.toCity(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
