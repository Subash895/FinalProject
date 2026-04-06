package com.smartCity.Web.marketrate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.marketrate.MarketRate;
import com.smartCity.Web.marketrate.MarketRateRepository;

@Service
public class MarketRateService {

	@Autowired
	private MarketRateRepository repository;

	public MarketRate createMarketRate(MarketRate rate) {
		return repository.save(rate);
	}

	public MarketRate updateMarketRate(int id, MarketRate rate) {

		MarketRate existing = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("MarketRate not found with id: " + id));
		return repository.save(existing);
	}

	public List<MarketRate> getAllMarketRates() {
		return repository.findAll();
	}
}

