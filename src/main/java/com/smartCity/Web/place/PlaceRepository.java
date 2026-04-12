package com.smartCity.Web.place;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access methods for Place records.
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
  List<Place> findByCityId(Long cityId);
  void deleteByCityId(Long cityId);

  List<Place> findByCategoryContainingIgnoreCase(String category);

  List<Place> findByLocationContainingIgnoreCase(String location);
}
