package com.smartCity.Web.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for Event records.
 */
public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findByCityId(Long cityId);

  void deleteByCityId(Long cityId);
}
