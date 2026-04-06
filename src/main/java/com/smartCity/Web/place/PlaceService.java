package com.smartCity.Web.place;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.place.Place;
import com.smartCity.Web.place.PlaceRepository;

@Service
public class PlaceService {
    @Autowired
    private PlaceRepository repo;

    public Place save(Place entity) {
        return repo.save(entity);
    }

    public List<Place> getAll(String category, String location) {
        String normalizedCategory = normalizeFilter(category);
        String normalizedLocation = normalizeFilter(location);

        if (normalizedCategory != null && normalizedLocation != null) {
            return repo.findByCategoryContainingIgnoreCase(normalizedCategory).stream()
                    .filter(place -> containsIgnoreCase(place.getLocation(), normalizedLocation))
                    .toList();
        }

        if (normalizedCategory != null) {
            return repo.findByCategoryContainingIgnoreCase(normalizedCategory);
        }

        if (normalizedLocation != null) {
            return repo.findByLocationContainingIgnoreCase(normalizedLocation);
        }

        return repo.findAll();
    }

    public Optional<Place> getById(Long id) {
        return repo.findById(id);
    }

    public Place update(Long id, Place entity) {
        Place existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Place not found with id: " + id));

        existing.setName(entity.getName());
        existing.setCategory(entity.getCategory());
        existing.setLocation(entity.getLocation());

        if (entity.getDescription() != null) {
            existing.setDescription(entity.getDescription());
        }
        if (entity.getCity() != null) {
            existing.setCity(entity.getCity());
        }
        if (entity.getLatitude() != null) {
            existing.setLatitude(entity.getLatitude());
        }
        if (entity.getLongitude() != null) {
            existing.setLongitude(entity.getLongitude());
        }

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target.toLowerCase());
    }
}

