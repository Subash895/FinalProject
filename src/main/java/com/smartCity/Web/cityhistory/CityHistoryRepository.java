package com.smartCity.Web.cityhistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access methods for City History records.
 */
@Repository
public interface CityHistoryRepository extends JpaRepository<CityHistory, Long> {
    List<CityHistory> findByCityIdAndCityNameIgnoreCaseOrderByIdAsc(Long cityId, String cityName);
    void deleteByCityId(Long cityId);
}
