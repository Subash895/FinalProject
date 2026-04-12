package com.smartCity.Web.marketrate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.marketrate.MarketRate;

/**
 * Provides database access methods for Market Rate records.
 */
@Repository
public interface MarketRateRepository extends JpaRepository<MarketRate, Integer> {}
