package com.smartCity.Web.city;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access methods for City records.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {}
