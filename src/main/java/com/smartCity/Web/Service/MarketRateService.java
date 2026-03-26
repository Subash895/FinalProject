package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.MarketRate;
import com.smartCity.Web.Repository.MarketRateRepository;

@Service
public class MarketRateService {

	@Autowired
	private MarketRateRepository repository;

	public MarketRate createMarketRate(MarketRate rate) {
		return repository.save(rate);
	}

	public MarketRate updateMarketRate(Long id, MarketRate rate) {

		MarketRate existing = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("MarketRate not found with id: " + id));
		return repository.save(existing);
	}

	public List<MarketRate> getAllMarketRates() {
		return repository.findAll();
	}
}