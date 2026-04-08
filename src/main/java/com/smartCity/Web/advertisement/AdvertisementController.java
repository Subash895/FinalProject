package com.smartCity.Web.advertisement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.advertisement.AdvertisementDtos;

@RestController
@RequestMapping("/api/advertisements")
@CrossOrigin("*")
public class AdvertisementController {

  private final AdvertisementService service;
  private final ApiDtoMapper apiDtoMapper;

  public AdvertisementController(AdvertisementService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public AdvertisementDtos.AdvertisementResponse createAdvertisement(
      @RequestBody AdvertisementDtos.AdvertisementRequest ad) {
    return apiDtoMapper.toAdvertisementResponse(
        service.createAdvertisement(apiDtoMapper.toAdvertisement(ad)));
  }

  //   @GetMapping("/{id}")
  @GetMapping
  public List<AdvertisementDtos.AdvertisementResponse> getAllAdvertisements() {
    return service.getAllAdvertisements().stream()
        .map(apiDtoMapper::toAdvertisementResponse)
        .collect(Collectors.toList());
  }
}
