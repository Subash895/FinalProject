package com.smartCity.Web.marketrate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.marketrate.MarketRate;
import com.smartCity.Web.marketrate.MarketRateRepository;

/**
 * Coordinates the business rules for Market Rate features before data is stored or returned.
 */
@Service
public class MarketRateService {

  @Autowired private MarketRateRepository repository;

  public MarketRate createMarketRate(MarketRate rate) {
    return repository.save(rate);
  }

  public MarketRate updateMarketRate(int id, MarketRate rate) {

    MarketRate existing =
        repository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("MarketRate not found with id: " + id));
    existing.setProductName(rate.getProductName());
    existing.setPrice(rate.getPrice());
    existing.setUnit(rate.getUnit());
    if (rate.getPriceDate() != null) {
      existing.setPriceDate(rate.getPriceDate());
    }
    return repository.save(existing);
  }

  public List<MarketRate> getAllMarketRates() {
    return repository.findAll();
  }
}
