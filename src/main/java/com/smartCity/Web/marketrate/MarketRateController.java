package com.smartCity.Web.marketrate;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.marketrate.MarketRateDtos;

@RestController
@RequestMapping("/api/marketrates")
@CrossOrigin("*")
public class MarketRateController {

  private final MarketRateService service;
  private final ApiDtoMapper apiDtoMapper;

  public MarketRateController(MarketRateService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public MarketRateDtos.MarketRateResponse create(
      @RequestBody MarketRateDtos.MarketRateRequest rate) {
    return apiDtoMapper.toMarketRateResponse(
        service.createMarketRate(apiDtoMapper.toMarketRate(rate)));
  }

  @PutMapping("/{id}")
  public MarketRateDtos.MarketRateResponse update(
      @PathVariable int id, @RequestBody MarketRateDtos.MarketRateRequest rate) {

    return apiDtoMapper.toMarketRateResponse(
        service.updateMarketRate(id, apiDtoMapper.toMarketRate(rate)));
  }

  @GetMapping
  public List<MarketRateDtos.MarketRateResponse> getAll() {
    return service.getAllMarketRates().stream()
        .map(apiDtoMapper::toMarketRateResponse)
        .collect(Collectors.toList());
  }
}
