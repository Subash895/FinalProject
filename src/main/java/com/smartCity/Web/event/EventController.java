package com.smartCity.Web.event;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.event.EventService;
import com.smartCity.Web.event.Event;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class EventController {
	@Autowired
	private EventService service;

	@PostMapping
	public Event create(@RequestBody Event entity) {
		return service.save(entity);
	}

	@GetMapping
	public List<Event> getAll() {
		return service.getAll();
	}

	@GetMapping("/{id}")
	public Optional<Event> getById(@PathVariable Long id) {
		return service.getById(id);
	}

	@PutMapping("/{id}")
	public Event update(@PathVariable Long id, @RequestBody Event entity) {
		return service.update(id, entity);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}

