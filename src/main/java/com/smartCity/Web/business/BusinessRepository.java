package com.smartCity.Web.business;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.business.Business;

/**
 * Provides database access methods for Business records.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
  List<Business> findByOwnerId(Long ownerId);

  List<Business>
      findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrAddressContainingIgnoreCase(
          String nameQuery, String descriptionQuery, String addressQuery);

  List<Business>
      findByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndDescriptionContainingIgnoreCaseOrOwnerIdAndAddressContainingIgnoreCase(
          Long ownerIdForName,
          String nameQuery,
          Long ownerIdForDescription,
          String descriptionQuery,
          Long ownerIdForAddress,
          String addressQuery);
}
