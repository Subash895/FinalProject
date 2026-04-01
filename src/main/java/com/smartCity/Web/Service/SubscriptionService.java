package com.smartCity.Web.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Subscription;
import com.smartCity.Web.Repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    @Autowired private SubscriptionRepository repo;

    public Subscription save(Subscription entity) {
        populateEmail(entity);
        return repo.save(entity);
    }

    public List<Subscription> getAll() { return repo.findAll(); }

    public List<Subscription> getByEmail(String email) { return repo.findByEmail(email); }

    public Optional<Subscription> getById(Long id) { return repo.findById(id); }

    public Subscription update(Long id, Subscription entity) {
        entity.setId(id);
        populateEmail(entity);
        return repo.save(entity);
    }

    public void delete(Long id) { repo.deleteById(id); }

    private void populateEmail(Subscription entity) {
        if ((entity.getEmail() == null || entity.getEmail().isBlank()) && entity.getUser() != null) {
            entity.setEmail(entity.getUser().getEmail());
        }
    }
}
