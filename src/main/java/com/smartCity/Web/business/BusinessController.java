package com.smartCity.Web.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.business.BusinessDtos;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin("*")
public class BusinessController {
  private final BusinessService service;
  private final ApiDtoMapper apiDtoMapper;

  public BusinessController(BusinessService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public BusinessDtos.BusinessResponse create(
      @RequestBody BusinessDtos.BusinessRequest entity,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }

    try {
      return apiDtoMapper.toBusinessResponse(
          service.save(apiDtoMapper.toBusiness(entity), principal.id()));
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
    }
  }

  @GetMapping
  public List<BusinessDtos.BusinessResponse> getAll(@RequestParam(required = false) String q) {
    return service.getAll(q).stream()
        .map(apiDtoMapper::toBusinessResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BusinessDtos.BusinessResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toBusinessResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public BusinessDtos.BusinessResponse update(
      @PathVariable Long id, @RequestBody BusinessDtos.BusinessRequest entity) {
    return apiDtoMapper.toBusinessResponse(service.update(id, apiDtoMapper.toBusiness(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
