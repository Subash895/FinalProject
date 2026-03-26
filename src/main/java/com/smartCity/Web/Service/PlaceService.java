package com.smartCity.Web.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Place;
import com.smartCity.Web.Repository.PlaceRepository;

@Service
public class PlaceService {
    @Autowired private PlaceRepository repo;
    public Place save(Place entity) { return repo.save(entity); }
    public List<Place> getAll() { return repo.findAll(); }
    public Optional<Place> getById(Long id) { return repo.findById(id); }
    public Place update(Long id, Place entity) {
        entity.setId(id);
        return repo.save(entity);
    }
    public void delete(Long id) { repo.deleteById(id); }
}