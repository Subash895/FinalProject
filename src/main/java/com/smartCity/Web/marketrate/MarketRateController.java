package com.smartCity.Web.marketrate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.marketrate.MarketRate;
import com.smartCity.Web.marketrate.MarketRateService;

@RestController
@RequestMapping("/api/marketrates")
@CrossOrigin("*")
public class MarketRateController {

	@Autowired
	private MarketRateService service;

	@PostMapping
	public MarketRate create(@RequestBody MarketRate rate) {
		return service.createMarketRate(rate);
	}

	@PutMapping
	public MarketRate update(@PathVariable int id, @RequestBody MarketRate rate) {

		return service.updateMarketRate(id, rate);
	}

	@GetMapping
	public List<MarketRate> getAll() {
		return service.getAllMarketRates();
	}
}

