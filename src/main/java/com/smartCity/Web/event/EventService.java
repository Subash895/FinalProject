package com.smartCity.Web.event;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository repo;

  public Event save(Event entity) {
    return repo.save(entity);
  }

  public List<Event> getAll() {
    return repo.findAll();
  }

  public Optional<Event> getById(Long id) {
    return repo.findById(id);
  }

  public Event update(Long id, Event entity) {
    Event existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

    existing.setTitle(entity.getTitle());
    existing.setDescription(entity.getDescription());
    existing.setEventDate(entity.getEventDate());
    if (entity.getCity() != null) {
      existing.setCity(entity.getCity());
    }

    return repo.save(existing);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }
}
