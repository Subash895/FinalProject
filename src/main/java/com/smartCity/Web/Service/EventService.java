package com.smartCity.Web.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Event;
import com.smartCity.Web.Repository.EventRepository;

@Service
public class EventService {

    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

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
        entity.setId(id);   // ✅ now works
        return repo.save(entity);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}