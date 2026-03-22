package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.CityPremiumListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CityPremiumListingRepository extends JpaRepository<CityPremiumListing, Long> {

    List<CityPremiumListing> findByCityId(Long cityId);

    @Query("""
        SELECT c FROM CityPremiumListing c
        WHERE c.isActive = true
        AND c.startDate <= CURRENT_DATE
        AND c.endDate >= CURRENT_DATE
    """)
    List<CityPremiumListing> findActiveListings();
}