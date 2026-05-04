package com.smartCity.Web.business;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

/**
 * Coordinates the business rules for Business features before data is stored or returned.
 */
@Service
public class BusinessService {
  private static final int MAX_BUSINESS_IMAGE_LENGTH = 3_000_000;

  private final BusinessRepository repo;
  private final UserRepository userRepository;

  public BusinessService(BusinessRepository repo, UserRepository userRepository) {
    this.repo = repo;
    this.userRepository = userRepository;
  }

  public Business save(Business entity, Long authenticatedUserId) {
    User owner =
        userRepository
            .findById(authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (owner.getRole() != Role.BUSINESS && owner.getRole() != Role.ADMIN) {
      throw new RuntimeException("Only business users or admins can add businesses");
    }

    entity.setId(null);
    entity.setOwner(owner);
    return repo.save(entity);
  }

  public List<Business> getAll(String query, Long authenticatedUserId, String role) {
    if (Role.BUSINESS.name().equals(role) && authenticatedUserId != null) {
      if (!StringUtils.hasText(query)) {
        return repo.findByOwnerId(authenticatedUserId);
      }

      String normalizedQuery = query.trim();
      return repo
          .findByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndDescriptionContainingIgnoreCaseOrOwnerIdAndAddressContainingIgnoreCase(
              authenticatedUserId,
              normalizedQuery,
              authenticatedUserId,
              normalizedQuery,
              authenticatedUserId,
              normalizedQuery);
    }

    if (!StringUtils.hasText(query)) {
      return repo.findAll();
    }

    String normalizedQuery = query.trim();
    return repo
        .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrAddressContainingIgnoreCase(
            normalizedQuery, normalizedQuery, normalizedQuery);
  }

  public Optional<Business> getById(Long id) {
    return repo.findById(id);
  }

  public Business getByIdForUser(Long id, Long authenticatedUserId, String role) {
    Business business =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Business not found"));
    validateBusinessAccess(business, authenticatedUserId, role);
    return business;
  }

  public Business update(Long id, Business entity, Long authenticatedUserId, String role) {
    Business existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Business not found"));
    validateBusinessAccess(existing, authenticatedUserId, role);

    existing.setName(entity.getName());
    existing.setDescription(entity.getDescription());
    existing.setAddress(entity.getAddress());
    if (Role.ADMIN.name().equals(role)) {
      existing.setIsFeatured(entity.getIsFeatured());
    }
    return repo.save(existing);
  }

  public void delete(Long id, Long authenticatedUserId, String role) {
    Business existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Business not found"));
    validateBusinessAccess(existing, authenticatedUserId, role);
    repo.deleteById(id);
  }

  public Business updateImage(
      Long id, byte[] imageData, String contentType, Long authenticatedUserId, String role) {
    Business existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Business not found"));
    validateBusinessAccess(existing, authenticatedUserId, role);
    if (imageData == null || imageData.length == 0) {
      throw new RuntimeException("Business image is required");
    }
    if (imageData.length > MAX_BUSINESS_IMAGE_LENGTH) {
      throw new RuntimeException("Business image is too large");
    }
    existing.setImageData(imageData);
    existing.setImageContentType(contentType);
    return repo.save(existing);
  }

  private void validateBusinessAccess(Business business, Long authenticatedUserId, String role) {
    if (Role.ADMIN.name().equals(role)) {
      return;
    }

    if (Role.BUSINESS.name().equals(role)
        && authenticatedUserId != null
        && business.getOwner() != null
        && authenticatedUserId.equals(business.getOwner().getId())) {
      return;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own business");
  }
}
