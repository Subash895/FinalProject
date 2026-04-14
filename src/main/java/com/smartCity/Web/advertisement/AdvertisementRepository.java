package com.smartCity.Web.advertisement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.advertisement.Advertisement;

/**
 * Provides database access methods for Advertisement records.
 */
@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {}
