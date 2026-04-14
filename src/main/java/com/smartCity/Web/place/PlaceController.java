package com.smartCity.Web.place;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PlaceController {
  private final PlaceService service;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping
  public PlaceDtos.PlaceResponse create(@RequestBody PlaceDtos.PlaceRequest entity) {
    return apiDtoMapper.toPlaceResponse(service.save(apiDtoMapper.toPlace(entity)));
  }

  @GetMapping
  public List<PlaceDtos.PlaceResponse> getAll(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String location) {
    return service.getAll(category, location).stream()
        .map(apiDtoMapper::toPlaceResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PlaceDtos.PlaceResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toPlaceResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public PlaceDtos.PlaceResponse update(
      @PathVariable Long id, @RequestBody PlaceDtos.PlaceRequest entity) {
    return apiDtoMapper.toPlaceResponse(service.update(id, apiDtoMapper.toPlace(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
