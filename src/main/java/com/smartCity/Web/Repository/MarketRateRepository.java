package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.MarketRate;

@Repository
public interface MarketRateRepository extends JpaRepository<MarketRate, Integer> {
}
