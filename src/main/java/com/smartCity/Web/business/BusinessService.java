package com.smartCity.Web.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessRepository;

@Service
public class BusinessService {
    @Autowired private BusinessRepository repo;
    public Business save(Business entity) { return repo.save(entity); }
    public List<Business> getAll() { return repo.findAll(); }
    public Optional<Business> getById(Long id) { return repo.findById(id); }
    public Business update(Long id, Business entity) {
        entity.setId(id);
        return repo.save(entity);
    }
    public void delete(Long id) { repo.deleteById(id); }
}
