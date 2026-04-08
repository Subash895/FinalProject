package com.smartCity.Web.event;

import java.util.List;
import java.util.stream.Collectors;

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
import com.smartCity.Web.event.EventDtos;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class EventController {
  private final EventService service;
  private final ApiDtoMapper apiDtoMapper;

  public EventController(EventService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public EventDtos.EventResponse create(@RequestBody EventDtos.EventRequest entity) {
    return apiDtoMapper.toEventResponse(service.save(apiDtoMapper.toEvent(entity)));
  }

  @GetMapping
  public List<EventDtos.EventResponse> getAll() {
    return service.getAll().stream()
        .map(apiDtoMapper::toEventResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public org.springframework.http.ResponseEntity<EventDtos.EventResponse> getById(
      @PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toEventResponse)
        .map(org.springframework.http.ResponseEntity::ok)
        .orElse(org.springframework.http.ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public EventDtos.EventResponse update(
      @PathVariable Long id, @RequestBody EventDtos.EventRequest entity) {
    return apiDtoMapper.toEventResponse(service.update(id, apiDtoMapper.toEvent(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
