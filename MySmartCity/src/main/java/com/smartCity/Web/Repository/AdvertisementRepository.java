package com.smartCity.Web.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartCity.Web.Model.Advertisement;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query("""
        SELECT a FROM Advertisement a
        WHERE a.isActive = true
        AND a.startDate <= CURRENT_DATE
        AND a.endDate >= CURRENT_DATE
    """)
    List<Advertisement> findActiveAds();
}