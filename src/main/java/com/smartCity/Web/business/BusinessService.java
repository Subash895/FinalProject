package com.smartCity.Web.business;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

@Service
public class BusinessService {
    private final BusinessRepository repo;
    private final UserRepository userRepository;

    public BusinessService(BusinessRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    public Business save(Business entity, Long authenticatedUserId) {
        User owner = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (owner.getRole() != Role.BUSINESS) {
            throw new RuntimeException("Only business users can add businesses");
        }

        entity.setId(null);
        entity.setOwner(owner);
        return repo.save(entity);
    }

    public List<Business> getAll(String query) {
        if (!StringUtils.hasText(query)) {
            return repo.findAll();
        }

        String normalizedQuery = query.trim();
        return repo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrAddressContainingIgnoreCase(
                normalizedQuery,
                normalizedQuery,
                normalizedQuery);
    }

    public Optional<Business> getById(Long id) { return repo.findById(id); }

    public Business update(Long id, Business entity) {
        Business existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        existing.setName(entity.getName());
        existing.setDescription(entity.getDescription());
        existing.setAddress(entity.getAddress());
        existing.setIsFeatured(entity.getIsFeatured());
        return repo.save(existing);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
